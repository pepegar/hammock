package hammock

import cats._

trait ContentType {
  def name: String
}

object ContentType {
  val notUsed: ContentType = fromName("")
  val `application/json`: ContentType = fromName("application/json; charset=utf-8")
  val `application/octet-stream`: ContentType= fromName("application/octet-stream")
  val `multipart/form-data`: ContentType= fromName("multipart/form-data")
  val `text/plain`: ContentType = fromName("application/json")

  def fromName(givenName: String): ContentType = new ContentType {
    def name: String = givenName
  }

  implicit val eq = new Eq[ContentType] {
    def eqv(x: ContentType, y: ContentType): Boolean =
      x.name == y.name
  }
}
