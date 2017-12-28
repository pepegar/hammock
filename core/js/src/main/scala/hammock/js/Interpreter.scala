package hammock.js

import cats._
import cats.effect.Sync
import cats.syntax.show._
import hammock.{
  Delete,
  Entity,
  Get,
  Head,
  HttpF,
  HttpRequest,
  HttpResponse,
  InterpTrans,
  Method,
  Options,
  Post,
  Put,
  Status,
  Trace,
  Uri
}
import org.scalajs.dom

class Interpreter[F[_]] extends InterpTrans[F] {

  import Uri._

  override def trans(implicit S: Sync[F]): HttpF ~> F =
    λ[HttpF ~> F] {
      case req: Options => doReq(req, Method.OPTIONS)
      case req: Get     => doReq(req, Method.GET)
      case req: Head    => doReq(req, Method.HEAD)
      case req: Post    => doReq(req, Method.POST)
      case req: Put     => doReq(req, Method.PUT)
      case req: Delete  => doReq(req, Method.DELETE)
      case req: Trace   => doReq(req, Method.TRACE)
    }

  private def doReq(reqF: HttpF[HttpResponse], method: Method)(implicit S: Sync[F]): F[HttpResponse] = S.delay {
    val xhr   = new dom.XMLHttpRequest()
    val async = true // async = false is deprecated in JS

    xhr.open(method.name, reqF.req.uri.show, async)
    reqF.req.headers foreach {
      case (k, v) => xhr.setRequestHeader(k, v)
    }
    val data = reqF.req match {
      case HttpRequest(_, _, Some(Entity.StringEntity(data, contentType))) =>
        data
      case _ =>
        ""
    }
    xhr.send(data)

    val status          = Status.get(xhr.status)
    val responseHeaders = parseHeaders(xhr.getAllResponseHeaders)
    val body            = xhr.responseText

    HttpResponse(status, responseHeaders, Entity.StringEntity(body))
  }

  private def parseHeaders(str: String): Map[String, String] =
    str
      .split("\n")
      .map { line =>
        val Array(k, v) = line.split(": ")
      (k, v)
    } toMap
}

object Interpreter {
  def apply[F[_]]: Interpreter[F] = new Interpreter[F]
}
