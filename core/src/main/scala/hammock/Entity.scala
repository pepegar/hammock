package hammock

import cats._
import monocle._

sealed trait Entity {
  type Content

  def contentType: ContentType
  def content: Content
  def contentLength: Long
  def chunked: Boolean
  def repeatable: Boolean
  def streaming: Boolean

  def cata[X](
      onStringEntity: Entity.StringEntity => X,
      onByteArrayEntity: Entity.ByteArrayEntity => X,
      onEmptyEntity: Entity.EmptyEntity.type => X
  ): X = this match {
    case e: Entity.StringEntity    => onStringEntity(e)
    case e: Entity.ByteArrayEntity => onByteArrayEntity(e)
    case Entity.EmptyEntity        => onEmptyEntity(Entity.EmptyEntity)
  }
}

object Entity {
  case object EmptyEntity extends Entity {
    type Content = Unit
    def content       = ()
    def contentType   = ContentType.notUsed
    def contentLength = 0
    def chunked       = false
    def repeatable    = true
    def streaming     = false
  }

  case class StringEntity(body: String, contentType: ContentType = ContentType.`text/plain`) extends Entity {
    type Content = String
    def content       = body
    def contentLength = body.size.toLong
    def chunked       = false
    def repeatable    = true
    def streaming     = false
  }

  case class ByteArrayEntity(body: Array[Byte], contentType: ContentType = ContentType.`application/octet-stream`)
      extends Entity {
    type Content = Array[Byte]
    def chunked: Boolean     = false
    def content: Array[Byte] = body
    def contentLength: Long  = body.length.toLong
    def repeatable: Boolean  = true
    def streaming: Boolean   = false
  }

  implicit val eq: Eq[Entity] = Eq.fromUniversalEquals

  val string: Prism[Entity, String] =
    Prism.partial[Entity, String] { case StringEntity(body, _) => body }(StringEntity(_, ContentType.`text/plain`))

  val byteArray: Prism[Entity, Array[Byte]] =
    Prism.partial[Entity, Array[Byte]] { case ByteArrayEntity(body, _) => body }(
      ByteArrayEntity(_, ContentType.`application/octet-stream`))
}
