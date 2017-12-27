---
layout: home
title: "Home"
section: "Home"
---


## Installation

Just add the following to your `libraryDependencies` in SBT:

```
libraryDependencies += "com.pepegar" %% "hammock" % "0.7.1" // for JVM
libraryDependencies += "com.pepegar" %%% "hammock" % "0.7.1" // for JS
```

## Modules

There are already some companion modules implemented to ease the
experience of using hammock.

```
libraryDependencies += "com.pepegar" %% "hammock-circe" % "0.7.1" // for JVM
libraryDependencies += "com.pepegar" %%% "hammock-circe" % "0.7.1" // for JS
libraryDependencies += "com.pepegar" %% "hammock-akka-http" % "0.7.1" // only for JVM
```

## Functional programming

Hammock tries to be as functional as possible.  For example, the
environment in wich all the actions will be executed is
called [`Monad`](http://typelevel.org/cats/typeclasses/monad.html),
but fear not! Lots of types you were using before are already monads,
you know how to use them!


## Modules

| Module name          | Description                                | Version |
| -------------------- | ------------------------------------------ | ------- |
| `hammock-core`      | the core functionality of hammock, using [Apache HTTP commons][httpcommons] for HTTP in JVM and [XHR][xhr] in JS | `0.7.1` |
| `hammock-circe`      | encode and decode HTTP entities with [Circe][circe] | `0.7.1` |
| `hammock-akka-http`  | run your HTTP requests with [akka-http][akka-http] | `0.7.1` |

[httpcommons]: http://hc.apache.org/
[xhr]: https://developer.mozilla.org/en-US/docs/Web/API/XMLHttpRequest
[circe]: http://circe.io
[akka-http]: https://doc.akka.io/docs/akka-http/current/scala/http/


## HTTP

With Hammock you can do HTTP operations in a typeful and functional way.

```tut:silent
import cats.effect.IO
import io.circe._
import io.circe.generic.auto._
import hammock._
import hammock.Uri._
import hammock.hi._
import hammock.marshalling._
import hammock.jvm.free.Interpreter
import hammock.circe.implicits._


object HttpClient {
  implicit val interp = Interpreter[IO]
  
  val response = Hammock
    .getWithOpts(Uri.unsafeParse("https://api.fidesmo.com/apps"), Opts.empty)
    .as[List[String]]
    .exec[IO]
}
```

```tut
HttpClient.response.unsafeRunSync
```

## Target Monad

Hammock uses cats-effect's `Sync` under the hood to provide a safe way
of capturing effects when interpreting your programs.

You can use as a target monad any type `F` that has an instance of
`Sync`.  There are already several types you can
use out of the box, for example:

* `cats.effect.IO`: An `IO` type for the cats ecosystem.
* `scala.concurrent.Future`: There are lots of applications out there that express
  their `IO` effects with `Future`, you can still use it!
* `scala.util.Try`: You don't care of blocking current thread? go ahead, use it!
* `monix/Task`: Great implementation of a concurrency monad.  You can
  learn more about it [here](https://monix.io/)
