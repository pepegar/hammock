---
layout: docs
---


# Low level DSL

Imagine you were already using free monads for desiginng your
application:

As always, start with some imports

```tut:silent
import cats._
import cats.free._
import cats.data._
import cats.effect.Sync
```

## Algebras

Then, we need to start defining our algebras. Here's is the algebra
related to logging

### Log

```tut:book
object Log {
  sealed trait LogF[A]
  case class Info(msg: String) extends LogF[Unit]
  case class Error(msg: String) extends LogF[Unit]

  class LogC[F[_]](implicit I: InjectK[LogF, F]) {
    def info(msg: String): Free[F, Unit] = Free.inject(Info(msg))
    def error(msg: String): Free[F, Unit] = Free.inject(Error(msg))
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

```tut:silent
object IOEff {

sealed trait IOF[A]
  case object Read extends IOF[String]
  case class Write(msg: String) extends IOF[Unit]

  class IOC[F[_]](implicit I: InjectK[IOF, F]) {
    def read: Free[F, String] = Free.inject(Read)
    def write(str: String): Free[F, Unit] = Free.inject(Write(str))
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

## Combining our effects

And finally, we should need to build everything together. For that
purpose, we will need a `EitherK`. This datatype basically tells the
typesystem about our effects, saying that our `Eff` type can be either
a `Log` or a `IO` value.

```tut:silent
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

```tut
import cats.effect.IO
App.program foldMap App.interp[IO]
```

## Interleaving Hammock in a Free program

Normally, extending this kind of programs is a bit cumbersome because
you need to write all the boilerplate to embed a library into a
free-based architecture, and then use it yourself.  However, with
Hammock, you can import `hammock.free._` and enjoy:

```tut:silent
object App {
  import hammock.Uri
  import IOEff._
  import Log._
  import cats._
  import cats.effect.IO
  import hammock.free.algebra._
  import hammock.jvm.free._

  type Eff1[A] = EitherK[LogF, IOF, A]
  type Eff[A] = EitherK[HttpRequestF, Eff1, A]

  def program(implicit
    Log: LogC[Eff],
    IO: IOC[Eff],
    Hammock: HttpRequestC[Eff]
  ) = for {
    _ <- IO.write("What's the ID?")
    id = "4" // for the sake of docs, lets hardcode this... It should be `id <- IO.read`
    _ <- Log.info(s"id was $id")
    response <- Hammock.get(Uri.unsafeParse(s"https://jsonplaceholder.typicode.com/users?id=${id.toString}"), Map())
  } yield response

  def interp1[F[_]: Sync]: Eff1 ~> F = Log.interp or IOEff.interp
  def interp[F[_]: Sync]: Eff ~> F = Interpreter[F].trans or interp1 // interpret Hammock's effects
}
```

### Result

```tut
val result = App.program foldMap App.interp[IO]

result.unsafeRunSync
```


# High level DSL

This package provides a high level DSL to use Hammock without the
hassle of dealing with Free monads, interpreters, and so on.

Of course, you're still able to use lo level API if needed to create
your requests.

## Example of use


```tut:book
import hammock._
import hammock.jvm.free.Interpreter
import hammock.hi._
import hammock.hi.dsl._

import cats._
import cats.implicits._
import cats.effect.IO

implicit val interp = Interpreter[IO]

val opts = (header("user" -> "pepegar") &> cookie(Cookie("track", "a lot")))(Opts.default)

val response = Hammock.getWithOpts(Uri.unsafeParse("http://httpbin.org/get"), opts).exec[IO]
```

## Opts

The high level DSL uses a `Opts` datatype for describing the request.
This `Opts` type is later compiled by the `withOpts` methods to the
`Free` representation of the request.

```tut:silent
case class Opts(
  auth: Option[Auth],
  headers: Map[String, String],
  cookies: Option[List[Cookie]])
```

All the combinators for manipulating the `Opts` type return a value of
the type `Opts => Opts`, so you can combine directly via the `andThen`
combinator of `Function1`.  Also, Hammock provides a helper combinator
`&>` for composing `Opts => Opts` functions.

### Combinators that operate on `Opts`

| signature                                               | description                                                                  |
|---------------------------------------------------------+------------------------------------------------------------------------------|
| `auth(a: Auth): Opts => Opts`                           | Sets the `auth` field in opts                                                |
| `cookies_!(cookies: List[Cookie]): Opts => Opts`        | Substitutes the current value of `cookies` in the given `Opts` by its param. |
| `cookies(cookies: List[Cookie]): Opts => Opts`          | Appends the given cookies to the current value of `cookies`.                 |
| `cookie(cookie: Cookie): Opts => Opts`                  | Adds the given `Cookie` to the `Opts` value.                                 |
| `headers_!(headers: Map[String, String]): Opts => Opts` | Sets the `headers`.                                                          |
| `headers(headers: Map[String, String]): Opts => Opts`   | Appends the given `headers` to the former ones.                              |
| `header(header: (String, String)): Opts => Opts`        | Appends current `header` (a `(String, String)` value) to the headers map.    |

Here's an example of how can you use the high level DSL:


```tut:book
val req = {
  auth(Auth.BasicAuth("pepegar", "p4ssw0rd")) &>
    cookie(Cookie("track", "A lot")) &>
    header("user" -> "potatoman")
}
```

#### Authentication

There are a number of authentication headers already implemented in
Hammock:

* **Basic auth** (`Auth.BasicAuth(user: String, pass: String)`): Authenticate with user and password.
* **OAuth2 Bearer token** (`Auth.OAuth2Bearer(token: String)`): Authenticate with an OAuth2 Bearer
  token. This is treated by many services like a user/password pair
* **OAuth2 token** (`Auth.OAuth2Token(token: String)`): This is a _not really standard_ bearer token.
  Will be treated by services as user/password.

#### Cookies

Cookies in Hammock are represented by the `Cookie` type:

```scala
@Lenses case class Cookie(
  name: String,
  value: String,
  expires: Option[Date] = None,
  maxAge: Option[Int] = None,
  domain: Option[String] = None,
  path: Option[String] = None,
  secure: Option[Boolean] = None,
  httpOnly: Option[Boolean] = None,
  sameSite: Option[SameSite] = None,
  custom: Option[Map[String, String]] = None)
```

The `@Lenses` annotation (from Monocle) provides lenses for all the
fields in a case class.

As you can see most of the behaviour of the cookie can be handled by
the type itself.  For example, adding a `MaxAge` setting to a cookie
is just matter of doing:

```tut:book
val cookie = Cookie("_ga", "werwer")

Cookie.maxAge.set(Some(234))(cookie)
```

#### Headers

Headers in the `Opts` type are represented by a `Map[String, String]`.
In this field, you normally want to put all the headers that are not
strictly cookies or authentication header.

# Codecs

Hammock uses a typeclass `Codec[A]` for encoding request bodies and
decoding response contents.  Its signature is the following:

```
import hammock.CodecException

trait Codec[A] {
  def encode(a: A): String
  def decode(str: String): Either[CodecException, A]
}
```

Currently, this interface is implemented for `circe` codecs, so you
can just grab `hammock-circe`:

```
libraryDependencies += "hammock" %% "hammock-circe" % "0.7.0"
```

And use it directly:

```tut:book
import hammock._
import hammock.circe._
import hammock.circe.implicits._

import io.circe._
import io.circe.generic.auto._

case class MyClass(stringField: String, intField: Int)

Codec[MyClass].decode("""{"stringField": "This is Hammock!", "intField": 33}""")
Codec[MyClass].decode("this is not a valid json")
Codec[MyClass].encode(MyClass("hello dolly", 99))

// Also, you can use Codec's syntax as follows:

import Codec._

"""{"stringField": "This is Hammock!", "intField": 33}""".decode[MyClass]
"this is not a valid json".decode[MyClass]
MyClass("hello dolly", 99).encode
```
