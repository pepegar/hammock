---
layout: docs
---


# Introduction

# Using Hammock's Free

Imagine you were already using free monads for desiginng your application:

As always, start with some imports

```tut:silent
import cats._
import cats.free._
import cats.data._
```

## algebras

Then, we need to start defining our algebras. Here's is the algebra related to logging

### Log

```tut:silent
object Log {
  sealed trait LogF[A]
  case class Info(msg: String) extends LogF[Unit]
  case class Error(msg: String) extends LogF[Unit]
  
  class LogC[F[_]](implicit I: Inject[LogF, F]) {
    def info(msg: String): Free[F, Unit] = Free.inject(Info(msg))
    def error(msg: String): Free[F, Unit] = Free.inject(Error(msg))
  }
  
  object LogC {
    implicit def logC[F[_]](implicit I: Inject[LogF, F]): LogC[F] = new LogC[F]
  }

  def interp[F[_]](implicit ME: MonadError[F, Throwable]): LogF ~> F = new (LogF ~> F) {
    def apply[A](logF: LogF[A]): F[A] =  logF match {
      case Info(msg) => ME.catchNonFatal(println(s"[info]: $msg"))
      case Error(msg) => ME.catchNonFatal(println(s"[error]: $msg"))
    }
  }
}
```

### IO

And this one does IO.

```tut:silent
object IO {
  sealed trait IOF[A]
  case object Read extends IOF[String]
  case class Write(msg: String) extends IOF[Unit]
  
  class IOC[F[_]](implicit I: Inject[IOF, F]) {
    def read: Free[F, String] = Free.inject(Read)
    def write(str: String): Free[F, Unit] = Free.inject(Write(str))
  }
  
  object IOC {
    implicit def ioC[F[_]](implicit I: Inject[IOF, F]): IOC[F] = new IOC[F]
  }
  
  import scala.io.StdIn._
  
  def interp[F[_]](implicit ME: MonadError[F, Throwable]): IOF ~> F = new (IOF ~> F) {
    def apply[A](ioF: IOF[A]): F[A] = ioF match {
      case Read => ME.catchNonFatal(readLine : String)
      case Write(msg) => ME.catchNonFatal(println(s"$msg"))
    }
  }
}
```

### Combining our effects

And finally, we should need to build everything together. For that purpose, we will need a `Coproduct`. This datatype
basically tells the typesystem about our effects, saying that our `Eff` type can be either a `Log` or a `IO` value.

```tut:silent
object App {
  import IO._
  import Log._
  
  type Eff[A] = Coproduct[LogF, IOF, A]
  
  val name = "pepegar"
  
  def program(implicit Log: LogC[Eff], IO: IOC[Eff]) = for {
    // this dinamically generated documentation, we cannot ask for input, but we should do `name <- IO.read`
    _ <- Log.info(s"name was $name")
    _ <- IO.write(s"hello $name")
  } yield name
  
  def interp[F[_]](implicit ME: MonadError[F, Throwable]): Eff ~> F = Log.interp or IO.interp
}
```

You could use this as follows:

```tut
import cats.implicits._
import scala.util.Try
App.program foldMap App.interp[Try]
```

### Interleaving Hammock in a Free program

Normally, extending this kind of programs is a bit cumbersome because you need to write all the boilerplate to 
embed a library into a free-based architecture, and then use it yourself.  However, with Hammock, you can import
`hammock.free._` and enjoy:

```tut:silent
object App {
  import IO._
  import Log._
  import cats._
  import hammock.implicits._
  import hammock.free._
  
  type Eff1[A] = Coproduct[LogF, IOF, A]
  type Eff[A] = Coproduct[HttpRequestF, Eff1, A]
  
  def program(implicit
    Log: LogC[Eff],
    IO: IOC[Eff],
    Hammock: HttpRequestC[Eff]
  ) = for {
    _ <- IO.write("What's the ID?")
	id = "4" // for the sake of docs, lets hardcode this... It should be `id <- IO.read`
    _ <- Log.info(s"id was $id")
    response <- Hammock.get(s"https://jsonplaceholder.typicode.com/users?id=$id", Map(), None)
  } yield response
 
  def interp1[F[_]](implicit ME: MonadError[F, Throwable]): Eff1 ~> F = Log.interp(ME) or IO.interp(ME)
  def interp[F[_]](implicit ME: MonadError[F, Throwable]): Eff ~> F = Interp.trans(client, ME) or interp1(ME) // interpret Hammock's effects
}
```

### Result

```tut
import scala.util.Try
import cats.implicits._

App.program foldMap App.interp[Try]
```

# Codecs

todo
