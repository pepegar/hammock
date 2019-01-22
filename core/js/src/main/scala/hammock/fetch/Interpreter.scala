package hammock
package fetch

import cats.effect._
import cats._

import io.scalajs.npm.nodefetch._
import java.lang.String
import scala.scalajs.js.Promise
import cats.syntax.show._
import scala.scalajs.js.JSConverters._

class Interpreter[F[_]: Async](nodeFetch: NodeFetch) extends InterpTrans[F] {
  override def trans: HttpF ~> F = new (HttpF ~> F) {
    def apply[A](http: HttpF[A]): F[A] = {
      val method = http match {
        case Get(_)     => Method.GET
        case Delete(_)  => Method.DELETE
        case Head(_)    => Method.HEAD
        case Options(_) => Method.OPTIONS
        case Post(_)    => Method.POST
        case Put(_)     => Method.PUT
        case Trace(_)   => Method.TRACE
        case Patch(_)   => Method.PATCH
      }
      http match {
        case Get(_) | Options(_) | Delete(_) | Head(_) | Options(_) | Trace(_) | Post(_) | Put(_) | Patch(_) =>
          val hammockResponse = for {
            response <- IO.fromFuture(IO {
              val headers = http.req.headers.toJSDictionary
              nodeFetch(
                http.req.uri.show,
                http.req.entity
                  .flatMap(
                    _.cata(
                      string => Some(string.content),
                      bytes => Some(bytes.content.map(_.toChar).mkString),
                      empty => None
                    ))
                  .map(body => new RequestOptions(body = body, headers = headers, method = method.name))
                  .getOrElse(
                    new RequestOptions(
                      headers = headers,
                      method = method.name
                    ))
              ).toFuture
            })
            entity <- IO.fromFuture(IO(response.text().asInstanceOf[Promise[String]].toFuture))
          } yield HttpResponse(Status.Statuses(response.status), response.headers.toMap, Entity.StringEntity(entity))
          hammockResponse.to[F]
      }
    }
  }
}

object Interpreter {
  def apply[F[_]: Async](nodeFetch: NodeFetch): Interpreter[F] = new Interpreter[F](nodeFetch)
  def apply[F[_]: Async]: Interpreter[F]                       = apply(NodeFetch)

  //Select either the node dependency or the browser one
  lazy val BrowserFetch: NodeFetch = scalajs.js.Dynamic.global.window.fetch.asInstanceOf[NodeFetch]
  lazy val NodeFetch: NodeFetch    = io.scalajs.npm.nodefetch.NodeFetch
}
