package hammock
package free

import cats._
import cats.free._
import monocle.macros.Lenses

object algebra {

  @Lenses case class HttpRequest(method: Method, uri: Uri, headers: Map[String, String], body: Option[String])

  sealed trait HttpRequestF[A] {
    def req: HttpRequest
  }

  final case class Options(req: HttpRequest) extends HttpRequestF[HttpResponse]
  final case class Get(req: HttpRequest)     extends HttpRequestF[HttpResponse]
  final case class Head(req: HttpRequest)    extends HttpRequestF[HttpResponse]
  final case class Post(req: HttpRequest)    extends HttpRequestF[HttpResponse]
  final case class Put(req: HttpRequest)     extends HttpRequestF[HttpResponse]
  final case class Delete(req: HttpRequest)  extends HttpRequestF[HttpResponse]
  final case class Trace(req: HttpRequest)   extends HttpRequestF[HttpResponse]

  type HttpRequestIO[A] = Free[HttpRequestF, A]

  object Ops {
    def options(uri: Uri, headers: Map[String, String]): HttpRequestIO[HttpResponse] =
      Free.liftF(Options(HttpRequest(Method.OPTIONS, uri, headers, None)))
    def get(uri: Uri, headers: Map[String, String]): HttpRequestIO[HttpResponse] =
      Free.liftF(Get(HttpRequest(Method.GET, uri, headers, None)))
    def head(uri: Uri, headers: Map[String, String]): HttpRequestIO[HttpResponse] =
      Free.liftF(Head(HttpRequest(Method.HEAD, uri, headers, None)))
    def post(uri: Uri, headers: Map[String, String], body: Option[String]): HttpRequestIO[HttpResponse] =
      Free.liftF(Post(HttpRequest(Method.POST, uri, headers, body)))
    def put(uri: Uri, headers: Map[String, String], body: Option[String]): HttpRequestIO[HttpResponse] =
      Free.liftF(Put(HttpRequest(Method.PUT, uri, headers, body)))
    def delete(uri: Uri, headers: Map[String, String]): HttpRequestIO[HttpResponse] =
      Free.liftF(Delete(HttpRequest(Method.DELETE, uri, headers, None)))
    def trace(uri: Uri, headers: Map[String, String]): HttpRequestIO[HttpResponse] =
      Free.liftF(Trace(HttpRequest(Method.TRACE, uri, headers, None)))
  }

  class HttpRequestC[F[_]](implicit I: InjectK[HttpRequestF, F]) {
    def options(uri: Uri, headers: Map[String, String]): Free[F, HttpResponse] =
      Free.inject(Options(HttpRequest(Method.OPTIONS, uri, headers, None)))
    def get(uri: Uri, headers: Map[String, String]): Free[F, HttpResponse] =
      Free.inject(Get(HttpRequest(Method.GET, uri, headers, None)))
    def head(uri: Uri, headers: Map[String, String]): Free[F, HttpResponse] =
      Free.inject(Head(HttpRequest(Method.HEAD, uri, headers, None)))
    def post(uri: Uri, headers: Map[String, String], body: Option[String]): Free[F, HttpResponse] =
      Free.inject(Post(HttpRequest(Method.POST, uri, headers, body)))
    def put(uri: Uri, headers: Map[String, String], body: Option[String]): Free[F, HttpResponse] =
      Free.inject(Put(HttpRequest(Method.PUT, uri, headers, body)))
    def delete(uri: Uri, headers: Map[String, String]): Free[F, HttpResponse] =
      Free.inject(Delete(HttpRequest(Method.DELETE, uri, headers, None)))
    def trace(uri: Uri, headers: Map[String, String]): Free[F, HttpResponse] =
      Free.inject(Trace(HttpRequest(Method.TRACE, uri, headers, None)))
  }

  object HttpRequestC {
    implicit def httpRequestC[F[_]](implicit I: InjectK[HttpRequestF, F]): HttpRequestC[F] = new HttpRequestC[F]
  }

}
