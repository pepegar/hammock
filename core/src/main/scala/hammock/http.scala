package hammock

import cats._
import cats.free._

sealed trait HttpF[A] {
  def req: HttpRequest
}

final case class Options(req: HttpRequest) extends HttpF[HttpResponse]
final case class Get(req: HttpRequest)     extends HttpF[HttpResponse]
final case class Head(req: HttpRequest)    extends HttpF[HttpResponse]
final case class Post(req: HttpRequest)    extends HttpF[HttpResponse]
final case class Put(req: HttpRequest)     extends HttpF[HttpResponse]
final case class Delete(req: HttpRequest)  extends HttpF[HttpResponse]
final case class Trace(req: HttpRequest)   extends HttpF[HttpResponse]
final case class Patch(req: HttpRequest)   extends HttpF[HttpResponse]

object Ops {
  def options(uri: Uri, headers: Map[String, String]): Free[HttpF, HttpResponse] =
    Free.liftF(Options(HttpRequest(uri, headers, None)))
  def get(uri: Uri, headers: Map[String, String]): Free[HttpF, HttpResponse] =
    Free.liftF(Get(HttpRequest(uri, headers, None)))
  def head(uri: Uri, headers: Map[String, String]): Free[HttpF, HttpResponse] =
    Free.liftF(Head(HttpRequest(uri, headers, None)))
  def post(uri: Uri, headers: Map[String, String], entity: Option[Entity]): Free[HttpF, HttpResponse] =
    Free.liftF(Post(HttpRequest(uri, headers, entity)))
  def put(uri: Uri, headers: Map[String, String], entity: Option[Entity]): Free[HttpF, HttpResponse] =
    Free.liftF(Put(HttpRequest(uri, headers, entity)))
  def delete(uri: Uri, headers: Map[String, String]): Free[HttpF, HttpResponse] =
    Free.liftF(Delete(HttpRequest(uri, headers, None)))
  def trace(uri: Uri, headers: Map[String, String]): Free[HttpF, HttpResponse] =
    Free.liftF(Trace(HttpRequest(uri, headers, None)))
  def patch(uri: Uri, headers: Map[String, String], entity: Option[Entity]): Free[HttpF, HttpResponse] =
    Free.liftF(Patch(HttpRequest(uri, headers, entity)))
}

class HttpRequestC[F[_]](implicit I: InjectK[HttpF, F]) {
  def options(uri: Uri, headers: Map[String, String]): Free[F, HttpResponse] =
    Free.liftInject(Options(HttpRequest(uri, headers, None)))
  def get(uri: Uri, headers: Map[String, String]): Free[F, HttpResponse] =
    Free.liftInject(Get(HttpRequest(uri, headers, None)))
  def head(uri: Uri, headers: Map[String, String]): Free[F, HttpResponse] =
    Free.liftInject(Head(HttpRequest(uri, headers, None)))
  def post(uri: Uri, headers: Map[String, String], entity: Option[Entity]): Free[F, HttpResponse] =
    Free.liftInject(Post(HttpRequest(uri, headers, entity)))
  def put(uri: Uri, headers: Map[String, String], entity: Option[Entity]): Free[F, HttpResponse] =
    Free.liftInject(Put(HttpRequest(uri, headers, entity)))
  def delete(uri: Uri, headers: Map[String, String]): Free[F, HttpResponse] =
    Free.liftInject(Delete(HttpRequest(uri, headers, None)))
  def trace(uri: Uri, headers: Map[String, String]): Free[F, HttpResponse] =
    Free.liftInject(Trace(HttpRequest(uri, headers, None)))
  def patch(uri: Uri, headers: Map[String, String], entity: Option[Entity]): Free[F, HttpResponse] =
    Free.liftInject(Patch(HttpRequest(uri, headers, entity)))
}

object HttpRequestC {
  implicit def httpRequestC[F[_]](implicit I: InjectK[HttpF, F]): HttpRequestC[F] = new HttpRequestC[F]
}
