# Hammock

Hammock is yet another HTTP client for Scala. It tries to differentiate from other libraries with the following:

1. It's easy to use, has a high level API
2. ~It's library agnostic~ Will be library agnostic.
3. It tries to follow the _Bring Your Own Concurrency Monad_ pattern.
4. It has good documentation.

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

