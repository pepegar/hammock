package hammock


import httprequest._

trait Client {
  def request[A : Codec](method: Method, url: String, body: A, headers: Map[String, String]): HttpRequestIO[HttpResponse]
}

object Client {
  def request[A : Codec](method: Method, url: String, body: Option[A], headers: Map[String, String]): HttpRequestIO[HttpResponse] = method match {
    case Method.OPTIONS => Ops.options(url, headers, body.map(Codec[A].encode))
    case Method.GET => Ops.get(url, headers, body.map(Codec[A].encode))
    case Method.HEAD => Ops.head(url, headers, body.map(Codec[A].encode))
    case Method.POST => Ops.post(url, headers, body.map(Codec[A].encode))
    case Method.PUT => Ops.put(url, headers, body.map(Codec[A].encode))
    case Method.DELETE => Ops.delete(url, headers, body.map(Codec[A].encode))
    case Method.TRACE => Ops.trace(url, headers, body.map(Codec[A].encode))
    case Method.CONNECT => Ops.connect(url, headers, body.map(Codec[A].encode))
  }
}
