package hammock

import cats.Eq

trait HttpResponse {
  def status: Status
  def headers: Map[String, String]
  def content: String

  override def toString: String =
    s"""HttpResponse(status = $status, headers = $headers, content = "$content")"""

}

object HttpResponse {

  implicit val eqHttpResponse = new Eq[HttpResponse] {
    def eqv(x: HttpResponse, y: HttpResponse): Boolean = {
      x.status == y.status && x.headers == y.headers && x.content.equals(y.content)
    }
  }

  def apply(st: Status, h: Map[String, String], c: String) = new HttpResponse {
    def status = st
    def headers = h
    def content = c
  }
}
