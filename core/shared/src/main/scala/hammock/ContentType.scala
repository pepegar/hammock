package hammock

trait ContentType {
  def name: String
}

object ContentType {
  val notUsed: ContentType = fromName("")
  val `application/json`: ContentType = fromName("application/json")
  val `application/octet-stream`: ContentType= fromName("application/octet-stream")
  val `text/plain`: ContentType = fromName("application/json")

  def fromName(givenName: String): ContentType = new ContentType {
    def name: String = givenName
  }
}
