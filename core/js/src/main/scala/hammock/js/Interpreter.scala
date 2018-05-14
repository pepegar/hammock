package hammock
package js

import cats._
import cats.effect.{Async, IO, Sync}
import cats.implicits._
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.ext.Ajax.InputData
import java.nio.ByteBuffer

class Interpreter[F[_]: Async] extends InterpTrans[F] {

  import Uri._

  override def trans(implicit S: Sync[F]): HttpF ~> F =
    λ[HttpF ~> F] {
      case req @ (Options(_) | Get(_) | Head(_) | Post(_) | Put(_) | Delete(_) | Trace(_) | Patch(_)) => doReq(req)
    }

  private def doReq(reqF: HttpF[HttpResponse])(implicit F: Sync[F]): F[HttpResponse] = {
    val timeout = 0
    val headers = reqF.req.headers
    val data: InputData = reqF.req.entity.fold(InputData.str2ajax(""))(
      _.cata(
        string => InputData.str2ajax(string.content),
        bytes => InputData.byteBuffer2ajax(ByteBuffer.wrap(bytes.content)),
        Function.const(InputData.str2ajax("")))
    )
    val method = toMethod(reqF)

    for {
      responseFutureIO <- F.pure(IO(Ajax(method.name, reqF.req.uri.show, data, timeout, headers, false, "")))
      response         <- IO.fromFuture(responseFutureIO).to[F]
      responseHeaders  <- parseHeaders(response.getAllResponseHeaders)
      status = Status.get(response.status)
      body   = response.responseText
    } yield HttpResponse(status, responseHeaders, Entity.StringEntity(body))
  }

  private def toMethod(reqF: HttpF[HttpResponse]): Method = reqF match {
    case Options(_) => Method.OPTIONS
    case Get(_)     => Method.GET
    case Head(_)    => Method.HEAD
    case Post(_)    => Method.POST
    case Put(_)     => Method.PUT
    case Delete(_)  => Method.DELETE
    case Trace(_)   => Method.TRACE
    case Patch(_)   => Method.PATCH
  }

  private def parseHeaders(str: String)(implicit F: Sync[F]): F[Map[String, String]] = str match {
    case null => Map.empty[String, String].pure[F]
    case string =>
      F.delay(
        string
          .split("\r\n")
          .map({ line =>
            val splitted = line.split(": ")
            (splitted.head, splitted.tail.mkString("").trim)
          })
          .toMap)
  }
}

object Interpreter {
  def apply[F[_]: Async]: Interpreter[F] = new Interpreter[F]
}
