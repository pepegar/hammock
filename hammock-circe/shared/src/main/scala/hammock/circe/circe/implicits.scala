package hammock
package circe

import io.circe._
import io.circe.generic.auto._
import io.circe.parser.{decode => circeDecode, _}
import io.circe.syntax._

object implicits {

  implicit def circeCodecAuto[A : Encoder : Decoder]: Codec[A] = new CirceCodec[A]
}
