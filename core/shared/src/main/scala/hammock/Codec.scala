package hammock

class CodecException private (val message: String, underlying: Throwable) extends Throwable(message, underlying)

object CodecException {
  def withMessage(message: String)                            = new CodecException(message, null)
  def withMessageAndException(message: String, ex: Throwable) = new CodecException(message, ex)
}

trait Codec[A] {
  def encode(a: A): String

  def decode(a: String): Either[CodecException, A]
}

object Codec {
  def apply[A](implicit c: Codec[A]): Codec[A] = c

  implicit class EncodeOpOnA[A](a: A)(implicit C: Codec[A]) {
    def encode: String = C.encode(a)
  }

  implicit class DecodeOpOnString(str: String) {
    def decode[A: Codec]: Either[CodecException, A] = Codec[A].decode(str)
  }
}
