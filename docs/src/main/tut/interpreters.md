---
layout: docs
title: Interpreters
position: 2
---

# Interpreters

Interpreters are one of the core abstractions of Hammock.  As you
might have noticed, methods in the `Hammock` object do not
execute the request directly, but create programs that represent it.

If you want to learn more of this pattern, normally referred to as
_free monads_ or _free monads and interpreters_, read about it in
the [cats documentation][free-monad].

For executing Hammock programs we need an interpreter in place.
Interpreters in Hammock are created by instantiating the `InterpTrans`
trait:

```
trait InterpTrans[F[_]] {

  def trans(implicit S: Sync[F]): HttpF ~> F

}
```

Currently we have interpreters for several HTTP clients:

| Interpreter | Location | Artifact | Platform |
| ----------- | -------- | -------- | -------- |
| [apache HttpCommons][httpcommons] | `hammock.jvm.Interpreter` | `hammock-core` | JVM |
| [XmlHttpRequest][xhr] | `hammock.js.Interpreter` | `hammock-core` | JavaScript |
| [Akka HTTP][akka-http] | `hammock.akka.AkkaInterpreter` | `hammock-akka-http` | JVM |
| [AsyncHttpClient][asynchttpclient] | `hammock.asynchttpclient.AsyncHttpClientInterpreter` | `hammock-asynchttpclient` | JVM |

For demonstrating all the interpreters we'll use the same HTTP request:

```tut:silent
import cats.free.Free
import hammock._
import hammock.hi._

val httpReq = Hammock.getWithOpts(
  Uri.unsafeParse("http://httpbin.org/get"),
  (header("header1", "value1"))(Opts.empty))
```


## HttpCommons

HttpCommons interpreter is included in the `hammock-core` artifact.
For using it use the `Interpreter` included in `hammock-core`:

```tut
import cats.effect.IO
import hammock.jvm.Interpreter

httpReq foldMap Interpreter[IO].trans unsafeRunSync
```

## XmlHttpRequest

## Akka HTTP

```tut
import _root_.akka.actor.ActorSystem
import _root_.akka.http.scaladsl._
import _root_.akka.stream.ActorMaterializer

import cats.effect.IO
import hammock.akka.AkkaInterpreter

implicit val system = ActorSystem("hammock-actor-system")
implicit val mat = ActorMaterializer()
implicit val ec = system.dispatcher
val httpExt: HttpExt = Http()
implicit val interp = new AkkaInterpreter[IO](httpExt)

httpReq foldMap interp.trans unsafeRunSync

system.shutdown()
```

## AsyncHttpClient

```tut
import org.asynchttpclient._
import cats.effect.IO
import hammock.asynchttpclient.AsyncHttpClientInterpreter

val client: AsyncHttpClient = new DefaultAsyncHttpClient()

implicit val interp = new AsyncHttpClientInterpreter[IO](client)

httpReq foldMap interp.trans unsafeRunSync

client.close()
```


[httpcommons]: http://hc.apache.org/
[xhr]: https://developer.mozilla.org/en-US/docs/Web/API/XMLHttpRequest
[akka-http]: https://doc.akka.io/docs/akka-http/current/client-side/index.html
[asynchttpclient]: https://github.com/AsyncHttpClient/async-http-client/
[free-monad]: https://typelevel.org/cats/datatypes/freemonad.html
