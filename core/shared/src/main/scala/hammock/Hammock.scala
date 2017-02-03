package hammock

import cats._
import cats.arrow._

import free._
import free.algebra._

object Hammock {

  def request(method: Method, url: String, headers: Map[String, String]): HttpRequestIO[HttpResponse] = method match {
    case Method.OPTIONS => Ops.options(url, headers, None)
    case Method.GET => Ops.get(url, headers, None)
    case Method.HEAD => Ops.head(url, headers, None)
    case Method.POST => Ops.post(url, headers, None)
    case Method.PUT => Ops.put(url, headers, None)
    case Method.DELETE => Ops.delete(url, headers, None)
    case Method.TRACE => Ops.trace(url, headers, None)
  }

  def request[A : Codec](method: Method, url: String, headers: Map[String, String], body: Option[A]): HttpRequestIO[HttpResponse] = method match {
    case Method.OPTIONS => Ops.options(url, headers, body.map(Codec[A].encode))
    case Method.GET => Ops.get(url, headers, body.map(Codec[A].encode))
    case Method.HEAD => Ops.head(url, headers, body.map(Codec[A].encode))
    case Method.POST => Ops.post(url, headers, body.map(Codec[A].encode))
    case Method.PUT => Ops.put(url, headers, body.map(Codec[A].encode))
    case Method.DELETE => Ops.delete(url, headers, body.map(Codec[A].encode))
    case Method.TRACE => Ops.trace(url, headers, body.map(Codec[A].encode))
  }
}
