---
layout: home
title: "Home"
section: "Home"
---


## Installation

Just add the following to your `libraryDependencies` in SBT:

```
resolvers += Resolver.jcenterRepo

libraryDependencies += "com.pepegar" %% "hammock" % "0.1" // for JVM
libraryDependencies += "com.pepegar" %%% "hammock" % "0.1" // for JS
```

## Modules

There are already some companion modules implemented to ease the
experience of using hammock.

```
libraryDependencies += "com.pepegar" %% "hammock-circe" % "0.1" // for JVM
libraryDependencies += "com.pepegar" %%% "hammock-circe" % "0.1" // for JS
```

## Functional programming

Hammock tries to be as functional as possible.  For example, the
environment in wich all the actions will be executed is
called [`Monad`](http://typelevel.org/cats/typeclasses/monad.html),
but fear not! Lots of types you were using before are already monads,
you know how to use them!


## HTTP

With Hammock you can do HTTP operations in a typeful and functional way.

```tut:silent
import cats._
import cats.implicits._
import scala.util.{ Failure, Success, Try }
import io.circe._
import io.circe.generic.auto._
import hammock._
import hammock.Uri._
import hammock.hi._
import hammock.jvm.free.Interpreter
import hammock.circe.implicits._


object HttpClient {
  implicit val interp = Interpreter()

  val response = Hammock
    .getWithOpts(Uri.unsafeParse("https://api.fidesmo.com/apps"), Opts.default)
    .exec[Try]
    .as[List[String]]
}
```

```tut
HttpClient.response
```

## Target Monad

You can use as a target monad any type `F` that has an instance of
`MonadError[F, Throwable]`.  There are already several types you can
use out of the box, for example:

* `Future`: There are lots of applications out there that express
  their `IO` effects with `Future`, you can still use it!
* `Try`: You don't care of blocking current thread? go ahead, use it!
* `monix/Task`: Great implementation of a concurrency monad.  You can
  learn more about it [here](https://monix.io/)
