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

  case class ByteArrayEntity(body: Array[Byte], contentType: ContentType = ContentType.`application/octet-stream`) extends Entity {
    type Content = Array[Byte]
    def chunked: Boolean = false
    def content: Array[Byte] = body
    def contentLength: Long = body.length
    def repeatable: Boolean = true
    def streaming: Boolean = false
  }
}
