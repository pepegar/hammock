package hammock
package free

import cats._
import cats.free._

object algebra {

  sealed abstract class HttpRequestF[A] extends Product with Serializable {
    def url: String
    def headers: Map[String, String]
    def body: Option[String]
  }
  final case class Options(url: String, headers: Map[String, String], body: Option[String]) extends HttpRequestF[HttpResponse]
  final case class Get(url: String, headers: Map[String, String], body: Option[String]) extends HttpRequestF[HttpResponse]
  final case class Head(url: String, headers: Map[String, String], body: Option[String]) extends HttpRequestF[HttpResponse]
  final case class Post(url: String, headers: Map[String, String], body: Option[String]) extends HttpRequestF[HttpResponse]
  final case class Put(url: String, headers: Map[String, String], body: Option[String]) extends HttpRequestF[HttpResponse]
  final case class Delete(url: String, headers: Map[String, String], body: Option[String]) extends HttpRequestF[HttpResponse]
  final case class Trace(url: String, headers: Map[String, String], body: Option[String]) extends HttpRequestF[HttpResponse]

  type HttpRequestIO[A] = Free[HttpRequestF, A]

  object Ops {
    def options(url: String, headers: Map[String, String], body: Option[String]): HttpRequestIO[HttpResponse] = Free.liftF(Options(url, headers, body))
    def get(url: String, headers: Map[String, String], body: Option[String]): HttpRequestIO[HttpResponse] = Free.liftF(Get(url, headers, body))
    def head(url: String, headers: Map[String, String], body: Option[String]): HttpRequestIO[HttpResponse] = Free.liftF(Head(url, headers, body))
    def post(url: String, headers: Map[String, String], body: Option[String]): HttpRequestIO[HttpResponse] = Free.liftF(Post(url, headers, body))
    def put(url: String, headers: Map[String, String], body: Option[String]): HttpRequestIO[HttpResponse] = Free.liftF(Put(url, headers, body))
    def delete(url: String, headers: Map[String, String], body: Option[String]): HttpRequestIO[HttpResponse] = Free.liftF(Delete(url, headers, body))
    def trace(url: String, headers: Map[String, String], body: Option[String]): HttpRequestIO[HttpResponse] = Free.liftF(Trace(url, headers, body))
  }

  class HttpRequestC[F[_]](implicit I: Inject[HttpRequestF, F]) {
    def options(url: String, headers: Map[String, String], body: Option[String]): Free[F, HttpResponse] = Free.inject(Options(url, headers, body))
    def get(url: String, headers: Map[String, String], body: Option[String]): Free[F, HttpResponse] = Free.inject(Get(url, headers, body))
    def head(url: String, headers: Map[String, String], body: Option[String]): Free[F, HttpResponse] = Free.inject(Head(url, headers, body))
    def post(url: String, headers: Map[String, String], body: Option[String]): Free[F, HttpResponse] = Free.inject(Post(url, headers, body))
    def put(url: String, headers: Map[String, String], body: Option[String]): Free[F, HttpResponse] = Free.inject(Put(url, headers, body))
    def delete(url: String, headers: Map[String, String], body: Option[String]): Free[F, HttpResponse] = Free.inject(Delete(url, headers, body))
    def trace(url: String, headers: Map[String, String], body: Option[String]): Free[F, HttpResponse] = Free.inject(Trace(url, headers, body))
  }

  object HttpRequestC {
    implicit def httpRequestC[F[_]](implicit I: Inject[HttpRequestF, F]): HttpRequestC[F] = new HttpRequestC[F]
  }

}
