package hammock

trait ContentType {
  def name: String
}

object ContentType {
  val notUsed = register("")
  val `application/json` = register("application/json")
  val `text/plain` = register("application/json")

  private[this] def register(givenName: String): ContentType = new ContentType {
    def name: String = givenName
  }
}
