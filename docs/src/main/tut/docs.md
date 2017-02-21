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

## Combining our effects

And finally, we should need to build everything together. For that
purpose, we will need a `Coproduct`. This datatype basically tells the
typesystem about our effects, saying that our `Eff` type can be either
a `Log` or a `IO` value.

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

## Interleaving Hammock in a Free program

Normally, extending this kind of programs is a bit cumbersome because
you need to write all the boilerplate to embed a library into a
free-based architecture, and then use it yourself.  However, with
Hammock, you can import `hammock.free._` and enjoy:

```tut:silent
object App {
  import IO._
  import Log._
  import cats._
  import hammock.free.algebra._
  import hammock.jvm.free._

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
    response <- Hammock.get(s"https://jsonplaceholder.typicode.com/users?id=$id", Map())
  } yield response

  def interp1[F[_]](implicit ME: MonadError[F, Throwable]): Eff1 ~> F = Log.interp(ME) or IO.interp(ME)
  def interp[F[_]](implicit ME: MonadError[F, Throwable]): Eff ~> F = Interpreter().trans(ME) or interp1(ME) // interpret Hammock's effects
}
```

### Result

```tut
import scala.util.Try
import cats.implicits._

App.program foldMap App.interp[Try]
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

implicit val interp = Interpreter()

val opts = (header("user" -> "pepegar") &> param("pageId" -> "3"))(Opts.default)

val response = Hammock.getWithOpts("http://httpbin.org/get", opts).exec[Try]
```

## Opts

The high level DSL uses a `Opts` datatype for describing the request.
This `Opts` type is later compiled by the `withOpts` methods to the
`Free` representation of the request.

```tut:silent
case class Opts(
  auth: Option[Auth],
  headers: Map[String, String],
  params: Map[String, String],
  cookies: Option[List[Cookie]])
```

All the combinators for manipulating the `Opts` type return a value of
the type `Opts => Opts`, so you can combine directly via the `andThen`
combinator of `Function1`.  Also, Hammock provides a helper combinator
`&>` for composing `Opts => Opts` functions.

### Combinators that operate on `Opts`

Here's an example of how can you use the high level DSL:


```tut:book
val req = {
  auth(Auth.BasicAuth("pepegar", "p4ssw0rd")) &>
    cookie(Cookie("track", "A lot")) &>
    header("user" -> "") &>
    param("page" -> "33")
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

You can manipulate Authentication headers with:

`auth(a: Auth): Opts => Opts`

Sets the `auth` field of the given opts to `a`.


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

The combinators that hammock provides for handling cookies are:

`cookies_!(cookies: List[Cookie]): Opts => Opts`

Substitutes the current value of `cookies` in the given `Opts` by its
param.

`cookies(cookies: List[Cookie]): Opts => Opts`

Appends the given cookies to the current value of `cookies`.

`cookie(cookie: Cookie): Opts => Opts`

Adds the given `Cookie` to the `Opts` value.


#### Headers

`headers_!(headers: Map[String, String]): Opts => Opts`

Sets the `headers`.

`headers(headers: Map[String, String]): Opts => Opts`

Appends the given `headers` to the former ones.

`header(header: (String, String)): Opts => Opts`

Appends current `header` (a `(String, String)` value) to the headers
map.


#### Params (query params)

Hammock provides helper functions for handling query params in URLs:

`params_!(params: Map[String, String]): Opts => Opts`

Sets the query string part of the url to the given params.

`params(params: Map[String, String]): Opts => Opts`

Appends params to the query string.

`param(param: (String, String)): Opts => Opts`

Adds the given param to the query string.


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
libraryDependencies += "hammock" %% "hammock-circe" % "0.1"
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
