package hammock
package free

import cats._
import cats.free._

object algebra {

  sealed trait HttpRequestF[A] extends Product with Serializable {
    def method: Method
    def uri: Uri
    def headers: Map[String, String]
    def body: Option[String]
  }

  final case class Options(uri: Uri, headers: Map[String, String]) extends HttpRequestF[HttpResponse] {
    def body = None
    def method = Method.OPTIONS
  }
  final case class Get(uri: Uri, headers: Map[String, String]) extends HttpRequestF[HttpResponse] {
    def body = None
    def method = Method.GET
  }
  final case class Head(uri: Uri, headers: Map[String, String]) extends HttpRequestF[HttpResponse] {
    def body = None
    def method = Method.HEAD
  }
  final case class Post(uri: Uri, headers: Map[String, String], body: Option[String]) extends HttpRequestF[HttpResponse] {
    def method = Method.POST
  }
  final case class Put(uri: Uri, headers: Map[String, String], body: Option[String]) extends HttpRequestF[HttpResponse] {
    def method = Method.PUT
  }
  final case class Delete(uri: Uri, headers: Map[String, String]) extends HttpRequestF[HttpResponse] {
    def body = None
    def method = Method.DELETE
  }
  final case class Trace(uri: Uri, headers: Map[String, String]) extends HttpRequestF[HttpResponse] {
    def body = None
    def method = Method.TRACE
  }

  type HttpRequestIO[A] = Free[HttpRequestF, A]

  object Ops {
    def options(uri: Uri, headers: Map[String, String]): HttpRequestIO[HttpResponse] = Free.liftF(Options(uri, headers))
    def get(uri: Uri, headers: Map[String, String]): HttpRequestIO[HttpResponse] = Free.liftF(Get(uri, headers))
    def head(uri: Uri, headers: Map[String, String]): HttpRequestIO[HttpResponse] = Free.liftF(Head(uri, headers))
    def post(uri: Uri, headers: Map[String, String], body: Option[String]): HttpRequestIO[HttpResponse] = Free.liftF(Post(uri, headers, body))
    def put(uri: Uri, headers: Map[String, String], body: Option[String]): HttpRequestIO[HttpResponse] = Free.liftF(Put(uri, headers, body))
    def delete(uri: Uri, headers: Map[String, String]): HttpRequestIO[HttpResponse] = Free.liftF(Delete(uri, headers))
    def trace(uri: Uri, headers: Map[String, String]): HttpRequestIO[HttpResponse] = Free.liftF(Trace(uri, headers))
  }

  class HttpRequestC[F[_]](implicit I: Inject[HttpRequestF, F]) {
    def options(uri: Uri, headers: Map[String, String]): Free[F, HttpResponse] = Free.inject(Options(uri, headers))
    def get(uri: Uri, headers: Map[String, String]): Free[F, HttpResponse] = Free.inject(Get(uri, headers))
    def head(uri: Uri, headers: Map[String, String]): Free[F, HttpResponse] = Free.inject(Head(uri, headers))
    def post(uri: Uri, headers: Map[String, String], body: Option[String]): Free[F, HttpResponse] = Free.inject(Post(uri, headers, body))
    def put(uri: Uri, headers: Map[String, String], body: Option[String]): Free[F, HttpResponse] = Free.inject(Put(uri, headers, body))
    def delete(uri: Uri, headers: Map[String, String]): Free[F, HttpResponse] = Free.inject(Delete(uri, headers))
    def trace(uri: Uri, headers: Map[String, String]): Free[F, HttpResponse] = Free.inject(Trace(uri, headers))
  }

  object HttpRequestC {
    implicit def httpRequestC[F[_]](implicit I: Inject[HttpRequestF, F]): HttpRequestC[F] = new HttpRequestC[F]
  }

}
