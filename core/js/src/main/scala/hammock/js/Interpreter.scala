package hammock
package js

import cats._
import cats.effect.{Async, ContextShift}
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.show._
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.ext.Ajax.InputData
import java.nio.ByteBuffer

object Interpreter {

  def apply[F[_]](implicit F: InterpTrans[F]): InterpTrans[F] = F

  implicit def instance[F[_]: Async: ContextShift]: InterpTrans[F] = new InterpTrans[F] {
    def trans: HttpF ~> F = {

      def doReq(reqF: HttpF[HttpResponse]): F[HttpResponse] = {
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
          response <- Async.fromFuture(
            Async[F].delay(Ajax(method.name, reqF.req.uri.show, data, timeout, headers, false, "")))
          responseHeaders <- parseHeaders(response.getAllResponseHeaders)
          status = Status.get(response.status)
          body   = response.responseText
        } yield HttpResponse(status, responseHeaders, Entity.StringEntity(body))
      }

      def toMethod(reqF: HttpF[HttpResponse]): Method = reqF match {
        case Options(_) => Method.OPTIONS
        case Get(_)     => Method.GET
        case Head(_)    => Method.HEAD
        case Post(_)    => Method.POST
        case Put(_)     => Method.PUT
        case Delete(_)  => Method.DELETE
        case Trace(_)   => Method.TRACE
        case Patch(_)   => Method.PATCH
      }

      def parseHeaders(str: String): F[Map[String, String]] = str match {
        case null => Map.empty[String, String].pure[F]
        case string =>
          Async[F].delay(
            string
              .split("\r\n")
              .map({ line =>
                val splitted = line.split(": ")
                (splitted.head, splitted.tail.mkString("").trim)
              })
              .toMap)
      }

      λ[HttpF ~> F] {
        case req @ (Options(_) | Get(_) | Head(_) | Post(_) | Put(_) | Delete(_) | Trace(_) | Patch(_)) => doReq(req)
      }
    }
  }
}
