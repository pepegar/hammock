package hammock
package circe

import io.circe.{Encoder => CirceEncoder, Decoder => CirceDecoder}

object implicits {
  implicit def circeEncoderAuto[A: CirceEncoder]: Encoder[A]           = new HammockEncoderForCirce[A]
  implicit def circeDecoderAuto[A: CirceDecoder]: Decoder[A]           = new HammockDecoderForCirce[A]
  implicit def circeCodecAuto[A: CirceEncoder: CirceDecoder]: Codec[A] = new HammockCodecForCirce[A]
}
