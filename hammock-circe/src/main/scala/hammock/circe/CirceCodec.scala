package hammock
package circe

import cats.implicits._
import io.circe.{Encoder => CirceEncoder, Decoder => CirceDecoder}
import io.circe.parser.{decode => circeDecode}
import io.circe.syntax._

class HammockEncoderForCirce[A: CirceEncoder] extends Encoder[A] {
  override def encode(a: A): Entity =
    Entity.StringEntity(a.asJson.noSpaces, ContentType.`application/json`)
}

class HammockDecoderForCirce[A: CirceDecoder] extends Decoder[A] {
  override def decode(entity: Entity): Either[CodecException, A] = entity match {
    case Entity.StringEntity(str, _) =>
      circeDecode[A](str).left.map(err => CodecException.withMessageAndException(err.getMessage, err))
    case _: Entity.ByteArrayEntity =>
      CodecException.withMessage("unable to decode a ByteArrayEntity. Only StringEntity is supported").asLeft
  }
}

class HammockCodecForCirce[A: CirceEncoder: CirceDecoder] extends Codec[A] {
  override def encode(a: A): Entity =
    new HammockEncoderForCirce[A].encode(a)
  override def decode(entity: Entity): Either[CodecException, A] =
    new HammockDecoderForCirce[A].decode(entity)
}
