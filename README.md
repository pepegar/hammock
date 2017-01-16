# Hammock

Hammock is yet another HTTP client for Scala.  Under the hood it's a purely functional wrapper over [Apache Http Commons][httpcommons]

Hammock tries to differentiate from other libraries with the following:

1. It's easy to use, has a high level API
2. ~It's library agnostic~ Will be library agnostic, current implementation is [cats][cats]-based.
3. It's typeful, tries to represent effects at type level.
4. It follows the _Bring Your Own Concurrency Monad_ pattern.
5. It has good documentation.

[httpcommons]: http://apache.org/httpcommons
[cats]: http://typelevel/cats

# How does Hammock look in action?

The following is purely an elucubration... but I really hope it can look like this at some point!

```scala
import scala.concurrent.ExecutionContext.Implicits.global
import cats._
import cats.implicits._
import circe._
import circe.auto._
import hammock._


case class App(id: String)
type Apps = List[App]

val request = SimpleHttpClient.request(Method.GET, "http://api.fidesmo.com/apps") // HttpIO[HttpResponse]
	.run[Future] // Future[HttpResponse]
	.as[Apps] // Future[Apps]
```

