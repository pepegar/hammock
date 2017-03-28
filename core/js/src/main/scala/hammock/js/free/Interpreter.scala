package hammock
package js
package free

import hammock.free._

import org.scalajs.dom

import cats._
import cats.data._
import cats.syntax.show._

object Interpreter extends InterpTrans {

  import Uri._
  import algebra._

  override def trans[F[_]](implicit ME: MonadError[F, Throwable]): HttpRequestF ~> F = λ[HttpRequestF ~> F](_ match {
    case req@Options(url, headers) => doReq(req, Method.OPTIONS)
    case req@Get(url, headers) => doReq(req, Method.GET)
    case req@Head(url, headers) => doReq(req, Method.HEAD)
    case req@Post(url, headers, body) => doReq(req, Method.POST)
    case req@Put(url, headers, body) => doReq(req, Method.PUT)
    case req@Delete(url, headers) => doReq(req, Method.DELETE)
    case req@Trace(url, headers) => doReq(req, Method.TRACE)
  })

  private def doReq[F[_]](req: HttpRequestF[HttpResponse], method: Method)(implicit ME: MonadError[F, Throwable]): F[HttpResponse] = ME.catchNonFatal {
    val xhr = new dom.XMLHttpRequest()
    val async = false // asynchronicity should be handled by the concurrency monad `F`, not the HTTP driver

    xhr.open(method.name, req.uri.show, async)
    req.headers foreach {
      case (k, v) => xhr.setRequestHeader(k, v)
    }
    xhr.send(req.body.fold("")(identity))

    val status = Status.get(xhr.status)
    val responseHeaders = parseHeaders(xhr.getAllResponseHeaders)
    val body = xhr.responseText

    HttpResponse(status, responseHeaders, body)
  }

  private def parseHeaders(str: String): Map[String, String] = str.split("\n")
    .map { line =>
    val Array(k, v) = line.split(": ")
    (k, v)
    } toMap
} 
