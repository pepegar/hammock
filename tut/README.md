# Hammock

[![Join the chat at https://gitter.im/pepegar/hammock](https://badges.gitter.im/pepegar/hammock.svg)](https://gitter.im/pepegar/hammock?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/pepegar/hammock.svg?branch=master)](https://travis-ci.org/pepegar/hammock)
[![codecov](https://codecov.io/gh/pepegar/hammock/branch/master/graph/badge.svg)](https://codecov.io/gh/pepegar/hammock)
[![Download](https://api.bintray.com/packages/pepegar/com.pepegar/hammock-core/images/download.svg)](https://bintray.com/pepegar/com.pepegar/hammock-core/_latestVersion)

Hammock is yet another HTTP client for Scala.  Under the hood it's a purely functional wrapper over [Apache Http Commons][httpcommons] for JVM and [XmlHTTPRequest][xhr] for JS.

## Installation

Add the following to your `build.sbt`.

```scala
resolvers += Resolver.jcenterRepo

// For Scala 2.11 or 2.12
libraryDependencies += "com.pepegar" %% "hammock-core" % "0.2"

// For ScalaJS
libraryDependencies += "com.pepegar" %%% "hammock-core" % "0.2"
```


Hammock tries to differentiate from other libraries with the following:

1. It's easy to use, has a high level API
2. ~~It's library agnostic~~ Will be library agnostic, current implementation is [cats][cats]-based.
3. It's typeful, tries to represent effects at type level.
4. It does not force a specific target context. You can run your computations in `monix.Task`, `Future`, `Try`...
5. It has good documentation.

[httpcommons]: http://hc.apache.org/
[xhr]: https://developer.mozilla.org/en-US/docs/Web/API/XMLHttpRequest
[cats]: http://typelevel.org/cats

## How does Hammock look in action?

```tut:silent
import cats._
import cats.implicits._
import scala.util.{ Failure, Success, Try }
import io.circe._
import io.circe.generic.auto._
import hammock._
import hammock.Uri._
import hammock.jvm.free.Interpreter
import hammock.circe.implicits._


object HttpClient {
  implicit val interpreter = Interpreter()

  val response = Hammock
    .request(Method.GET, uri"https://api.fidesmo.com/apps", Map()) // In the `request` method, you describe your HTTP request
    .exec[Try]
    .as[List[String]]
}
```

## Code of conduct

People are expected to follow the [Typelevel Code of Conduct](http://typelevel.org/conduct.html) when discussing Hammock on the Github page, Gitter channel, or other venues.
