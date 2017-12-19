package hammock
package circe

import io.circe._

object implicits {
  implicit def circeCodecAuto[A: Encoder: Decoder]: Codec[A] = new CirceCodec[A]
}
