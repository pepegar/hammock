---
layout: docs
title: Algebras
position: 3
---

# Algebras

Hammock relies heavily on [free monads][free-monad] for declaring its
main algebras and keeping things pure.

## HttpF algebra

The HttpF algebra is the main pillar of Hammock.  It has operations
for all the HTTP verbs, and all operations have a single member `req`
of the type `HttpRequest`.  Then all these HttpRequests are
interpreted with an `InterpTrans`.

## MarshallF algebra

Our
[marshalling algebra](http://pepegar.com/hammock/docs/marshalling.html) only
has one operation, `unmarshall`, that transforms an `Entity` into an
`A` for which a `Decoder` exist.

Also, the `marshalling` package provides a natural transformation
`MarshalF ~> F`, for arbitrary `F[_]` for which there exist an
evidence of `Sync`.

## Integrating Hammock with other algebras

Imagine you were already using free monads for the design of your
application:

As always, start with some imports

```scala mdoc:silent
import cats._
import cats.free._
import cats.data._
import cats.effect.Sync
```

Then, we need to start defining our algebras. Here's is the algebra
related to logging

### Log

```scala mdoc
object Log {
  sealed trait LogF[A]
  case class Info(msg: String) extends LogF[Unit]
  case class Error(msg: String) extends LogF[Unit]

  class LogC[F[_]](implicit I: InjectK[LogF, F]) {
    def info(msg: String): Free[F, Unit] = Free.liftInject(Info(msg))
    def error(msg: String): Free[F, Unit] = Free.liftInject(Error(msg))
  }

  object LogC {
    implicit def logC[F[_]](implicit I: InjectK[LogF, F]): LogC[F] = new LogC[F]
  }

  def interp[F[_]: Sync]: LogF ~> F = new (LogF ~> F) {
    def apply[A](logF: LogF[A]): F[A] =  logF match {
      case Info(msg) => Sync[F].delay(println(s"[info]: $msg"))
      case Error(msg) => Sync[F].delay(println(s"[error]: $msg"))
    }
  }
}
```

### Console IO

And this one does IO.

```scala mdoc:silent
object IOEff {

  sealed trait IOF[A]
  case object Read extends IOF[String]
  case class Write(msg: String) extends IOF[Unit]

  class IOC[F[_]](implicit I: InjectK[IOF, F]) {
    def read: Free[F, String] = Free.liftInject(Read)
    def write(str: String): Free[F, Unit] = Free.liftInject(Write(str))
  }

  object IOC {
    implicit def ioC[F[_]](implicit I: InjectK[IOF, F]): IOC[F] = new IOC[F]
  }

  def interp[F[_]: Sync]: IOF ~> F = new (IOF ~> F) {
    // this could be implemented using scala.io.StdIn, for example
    def readline: String = "line read!"

    def apply[A](ioF: IOF[A]): F[A] = ioF match {
      case Read => Sync[F].delay(readline)
      case Write(msg) => Sync[F].delay(println(s"$msg"))
    }
  }
}
```

### Combining our effects

And finally, we should need to build everything together. For that
purpose, we will need a `EitherK`. This datatype basically tells the
typesystem about our effects, saying that our `Eff` type can be either
a `Log` or a `IOF` value.

```scala mdoc:silent
object App {
  import IOEff._
  import Log._

  type Eff[A] = EitherK[LogF, IOF, A]

  val name = "pepegar"

  def program(implicit Log: LogC[Eff], IO: IOC[Eff]) = for {
    // this dinamically generated documentation, we cannot ask for input, but we should do `name <- IO.read`
    _ <- Log.info(s"name was $name")
    _ <- IO.write(s"hello $name")
  } yield name

  def interp[F[_]: Sync]: Eff ~> F = Log.interp or IOEff.interp
}
```

You could use this as follows:

```scala mdoc
import cats.effect.IO
App.program foldMap App.interp[IO]
```

### Interleaving Hammock algebras in a Free program

```scala mdoc:silent
object AppWithFree {
  import hammock.Uri
  import IOEff._
  import Log._
  import cats._
  import cats.implicits._
  import cats.effect.IO
  import hammock._
  import hammock.marshalling._
  import hammock.apache._
  import hammock.apache.ApacheInterpreter
  import hammock.apache.ApacheInterpreter._

  type Eff1[A] = EitherK[LogF, IOF, A]
  type Eff2[A] = EitherK[HttpF, Eff1, A]
  type Eff[A] = EitherK[MarshallF, Eff2, A]

  implicit val dummyDecoder: Decoder[String] = new Decoder[String] {
    def decode(a: Entity) = a.toString.asRight
  }

  def program(implicit
    Log: LogC[Eff],
    IO: IOC[Eff],
    Hammock: HttpRequestC[Eff],
    Marshall: MarshallC[Eff]
  ) = for {
    _ <- IO.write("What's the ID?")
    id = "4" // for the sake of docs, lets hardcode this... It should be `id <- IO.read`
    _ <- Log.info(s"id was $id")
    response <- Hammock.get(uri"https://jsonplaceholder.typicode.com/users?id=${id.toString}", Map())
    parsed <- Marshall.unmarshall[String](response.entity)
  } yield response

  def interp1[F[_]: Sync]: Eff1 ~> F = Log.interp or IOEff.interp
  def interp2[F[_]: Sync]: Eff2 ~> F = ApacheInterpreter[F].trans or interp1 // interpret HttpF's effects
  def interp[F[_]: Sync]: Eff ~> F = marshallNT[F] or interp2[F] // interpret MarshallF's effects
}
```

### Result

```scala mdoc
val result = AppWithFree.program foldMap AppWithFree.interp[IO]

result.unsafeRunSync()
```




[free-monad]: https://typelevel.org/cats/datatypes/freemonad.html
