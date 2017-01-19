package hammock
package circe

import io.circe._
import io.circe.generic.auto._
import io.circe.parser.{decode => circeDecode, _}
import io.circe.syntax._


class CirceCodec[A : Encoder : Decoder] extends Codec[A] {

  override def encode(a:A): String = {
    a.asJson.noSpaces
  }

  override def decode(str: String): Either[CodecException, A] = {
    circeDecode[A](str).left.map(err => CodecException.withMessageAndException(err.getMessage, err))
  }

}
