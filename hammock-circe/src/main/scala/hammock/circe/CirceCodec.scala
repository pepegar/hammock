package hammock
package circe

import io.circe._
import io.circe.parser.{decode => circeDecode}
import io.circe.syntax._

class CirceCodec[A: Encoder: Decoder] extends Codec[A] {

  override def encode(a: A): Entity =
    Entity.StringEntity(a.asJson.noSpaces)

  override def decode(entity: Entity): Either[CodecException, A] = entity match {
    case Entity.StringEntity(str, _) =>
      circeDecode[A](str).left.map(err => CodecException.withMessageAndException(err.getMessage, err))
  }
}
