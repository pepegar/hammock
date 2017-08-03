package hammock

import cats.Eq

case class HttpResponseToDecode[T](response: HttpResponse)

object HttpResponseToDecode {
  implicit def eqHttpResponse[T] = new Eq[HttpResponseToDecode[T]] {
    def eqv(x: HttpResponseToDecode[T], y: HttpResponseToDecode[T]): Boolean =
      Eq[HttpResponse].eqv(x.response, y.response)
  }
}
