package hammock

import cats._
import cats.arrow._
import free._
import org.apache.http.client.HttpClient

object Hammock {

  trait Request[A] {
    def freeReq: HttpRequestIO[HttpResponse]
    def run[F[_] : MonadError[?[_], Throwable]](implicit httpClient: HttpClient): F[HttpResponse] = 
      freeReq foldMap Interp.trans
  }

  class WithBodyRequest[A](val freeReq: HttpRequestIO[HttpResponse]) extends Request[A]
  class BodylessRequest(val freeReq: HttpRequestIO[HttpResponse]) extends Request[String]

  def request(method: Method, url: String, headers: Map[String, String]): Request[String] = method match {
    case Method.OPTIONS => new BodylessRequest(Ops.options(url, headers, None))
    case Method.GET => new BodylessRequest(Ops.get(url, headers, None))
    case Method.HEAD => new BodylessRequest(Ops.head(url, headers, None))
    case Method.POST => new BodylessRequest(Ops.post(url, headers, None))
    case Method.PUT => new BodylessRequest(Ops.put(url, headers, None))
    case Method.DELETE => new BodylessRequest(Ops.delete(url, headers, None))
    case Method.TRACE => new BodylessRequest(Ops.trace(url, headers, None))
    case Method.CONNECT => new BodylessRequest(Ops.connect(url, headers, None))
  }

  def request[A : Codec](method: Method, url: String, body: Option[A], headers: Map[String, String]): Request[A] = method match {
    case Method.OPTIONS => new WithBodyRequest(Ops.options(url, headers, body.map(Codec[A].encode)))
    case Method.GET => new WithBodyRequest(Ops.get(url, headers, body.map(Codec[A].encode)))
    case Method.HEAD => new WithBodyRequest(Ops.head(url, headers, body.map(Codec[A].encode)))
    case Method.POST => new WithBodyRequest(Ops.post(url, headers, body.map(Codec[A].encode)))
    case Method.PUT => new WithBodyRequest(Ops.put(url, headers, body.map(Codec[A].encode)))
    case Method.DELETE => new WithBodyRequest(Ops.delete(url, headers, body.map(Codec[A].encode)))
    case Method.TRACE => new WithBodyRequest(Ops.trace(url, headers, body.map(Codec[A].encode)))
    case Method.CONNECT => new WithBodyRequest(Ops.connect(url, headers, body.map(Codec[A].encode)))
  }
}
