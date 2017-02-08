package hammock
package free

import cats._
import cats.free._

object algebra {

  sealed trait HttpRequestF[A] extends Product with Serializable {
    def method: Method
    def url: String
    def headers: Map[String, String]
    def body: Option[String]
  }

  final case class Options(url: String, headers: Map[String, String]) extends HttpRequestF[HttpResponse] {
    def body = None
    def method = Method.OPTIONS
  }
  final case class Get(url: String, headers: Map[String, String]) extends HttpRequestF[HttpResponse] {
    def body = None
    def method = Method.GET
  }
  final case class Head(url: String, headers: Map[String, String]) extends HttpRequestF[HttpResponse] {
    def body = None
    def method = Method.HEAD
  }
  final case class Post(url: String, headers: Map[String, String], body: Option[String]) extends HttpRequestF[HttpResponse] {
    def method = Method.POST
  }
  final case class Put(url: String, headers: Map[String, String], body: Option[String]) extends HttpRequestF[HttpResponse] {
    def method = Method.PUT
  }
  final case class Delete(url: String, headers: Map[String, String]) extends HttpRequestF[HttpResponse] {
    def body = None
    def method = Method.DELETE
  }
  final case class Trace(url: String, headers: Map[String, String]) extends HttpRequestF[HttpResponse] {
    def body = None
    def method = Method.TRACE
  }

  type HttpRequestIO[A] = Free[HttpRequestF, A]

  object Ops {
    def options(url: String, headers: Map[String, String]): HttpRequestIO[HttpResponse] = Free.liftF(Options(url, headers))
    def get(url: String, headers: Map[String, String]): HttpRequestIO[HttpResponse] = Free.liftF(Get(url, headers))
    def head(url: String, headers: Map[String, String]): HttpRequestIO[HttpResponse] = Free.liftF(Head(url, headers))
    def post(url: String, headers: Map[String, String], body: Option[String]): HttpRequestIO[HttpResponse] = Free.liftF(Post(url, headers, body))
    def put(url: String, headers: Map[String, String], body: Option[String]): HttpRequestIO[HttpResponse] = Free.liftF(Put(url, headers, body))
    def delete(url: String, headers: Map[String, String]): HttpRequestIO[HttpResponse] = Free.liftF(Delete(url, headers))
    def trace(url: String, headers: Map[String, String]): HttpRequestIO[HttpResponse] = Free.liftF(Trace(url, headers))
  }

  class HttpRequestC[F[_]](implicit I: Inject[HttpRequestF, F]) {
    def options(url: String, headers: Map[String, String]): Free[F, HttpResponse] = Free.inject(Options(url, headers))
    def get(url: String, headers: Map[String, String]): Free[F, HttpResponse] = Free.inject(Get(url, headers))
    def head(url: String, headers: Map[String, String]): Free[F, HttpResponse] = Free.inject(Head(url, headers))
    def post(url: String, headers: Map[String, String], body: Option[String]): Free[F, HttpResponse] = Free.inject(Post(url, headers, body))
    def put(url: String, headers: Map[String, String], body: Option[String]): Free[F, HttpResponse] = Free.inject(Put(url, headers, body))
    def delete(url: String, headers: Map[String, String]): Free[F, HttpResponse] = Free.inject(Delete(url, headers))
    def trace(url: String, headers: Map[String, String]): Free[F, HttpResponse] = Free.inject(Trace(url, headers))
  }

  object HttpRequestC {
    implicit def httpRequestC[F[_]](implicit I: Inject[HttpRequestF, F]): HttpRequestC[F] = new HttpRequestC[F]
  }

}
