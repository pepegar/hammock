---
layout: docs
title: DSL
position: 5
---

# High level DSL

This package provides a high level DSL to use Hammock without the
hassle of dealing with Free monads, interpreters, and so on.

Of course, you're still able to use lo level API if needed to create
your requests.

## Example of use


```tut:book
import hammock._
import hammock.jvm.Interpreter
import hammock.hi._
import hammock.hi._

import cats._
import cats.implicits._
import cats.effect.IO

implicit val interp = Interpreter[IO]

val opts = (header("user" -> "pepegar") >>> cookie(Cookie("track", "a lot")))(Opts.empty)

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
combinator of `Function1`.  Also, since all these combinators are
plain `Function1[Opts, Opts]`, for which there's an instance of
`cats.arrow.Compose`, we can use the much nicer `>>>` operator.

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
  auth(Auth.BasicAuth("pepegar", "p4ssw0rd")) >>>
    cookie(Cookie("track", "A lot")) >>>
    header("user" -> "potatoman")
}
```

You can also use [`Monocle`](https://github.com/julientruffaut/monocle)'s optics to describe and modify your
requests.  Most datatypes in Hammock provide sensible optics that will
work out of the box, and you can combine them with the ones provided
in [`Monocle`](https://github.com/julientruffaut/monocle):

```tut:book
// import stuff
import hammock.hi._, hammock.hi._ , monocle._, monocle.function.all._

// imagine that we have the following Opts value
val opts = (auth(Auth.BasicAuth("pepe", "password")) >>> headers(Map("X-Correlation-Id" -> "234")) >>> cookies(List(Cookie("a", "b"))))(Opts.empty)

import Opts._
import Cookie._

// Since optics compose nicely, we can focus on
// the first value of the first cookie found in the
// cookie list, for example:

cookiesOpt composeOptional index(0) composeLens value

// also you can use the symbolic operators for that :D
cookiesOpt ^|-? index(0) ^|-> value

// and then, use the optics machinery at your will, for example for getting the focus
(cookiesOpt ^|-? index(0) ^|-> value).getOption(opts)

// or modifying it!
(cookiesOpt ^|-? index(0) ^|-> value).set("newValue")(opts)
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

Cookies in Hammock are represented by the `Cookie` data type:

```scala
case class Cookie(
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

In its companion object there are optics for all the
fields. `Cookie.name` and `Cookie.value` are `Lens`es that allow to
focus on one particular field of the structure.


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

