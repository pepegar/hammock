# Hammock

[![Build Status](https://travis-ci.org/pepegar/hammock.svg?branch=master)](https://travis-ci.org/pepegar/hammock)
[ ![Download](https://api.bintray.com/packages/pepegar/hammock/hammock-core/images/download.svg) ](https://bintray.com/pepegar/hammock/hammock-core/_latestVersion) 

Hammock is yet another HTTP client for Scala.  Under the hood it's a purely functional wrapper over [Apache Http Commons][httpcommons]

Hammock tries to differentiate from other libraries with the following:

1. It's easy to use, has a high level API
2. ~~It's library agnostic~~ Will be library agnostic, current implementation is [cats][cats]-based.
3. It's typeful, tries to represent effects at type level.
4. It follows the _Bring Your Own Concurrency Monad_ pattern.
5. It has good documentation.

[httpcommons]: http://hc.apache.org/
[cats]: http://typelevel.org/cats

# How does Hammock look in action?

```scala
import cats._
import cats.implicits._
import scala.util.{ Failure, Success, Try }
import io.circe._
import io.circe.generic.auto._
import hammock._
import hammock.implicits._
import hammock.circe.implicits._


object HttpClient {
  val response = Hammock
    .request(Method.GET, "https://api.fidesmo.com/apps", Map()) // In the `request` method, you describe your HTTP request
    .run[Try]
    .as[List[String]]
}
```

