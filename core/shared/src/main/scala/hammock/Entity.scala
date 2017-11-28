package hammock

sealed trait Entity {

  type Content

  def contentType: ContentType
  def content: Content
  def contentLength: Long
  def chunked: Boolean
  def repeatable: Boolean
  def streaming: Boolean
}

object Entity {
  case class StringEntity(body: String, contentType: ContentType = ContentType.`text/plain`) extends Entity {
    type Content = String
    def content = body
    def contentLength = body.size
    def chunked = false
    def repeatable = true
    def streaming = false
  }
}
