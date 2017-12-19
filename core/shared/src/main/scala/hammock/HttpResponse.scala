package hammock

import cats.Eq
import monocle.macros.Lenses

@Lenses case class HttpResponse(status: Status, headers: Map[String, String], entity: Entity)

object HttpResponse {

  implicit val eqHttpResponse = new Eq[HttpResponse] {
    def eqv(x: HttpResponse, y: HttpResponse): Boolean =
      x.status == y.status && x.headers == y.headers && x.entity.equals(y.entity)
  }
}
