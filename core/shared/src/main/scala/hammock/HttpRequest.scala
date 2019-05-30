package hammock

import cats.{Show, Eq}
import monocle.macros.Lenses

@Lenses case class HttpRequest(uri: Uri, headers: Map[String, String], entity: Option[Entity])

object HttpRequest {

  implicit val show: Show[HttpRequest] = Show.fromToString
  implicit val eq: Eq[HttpRequest] = Eq.fromUniversalEquals

}
