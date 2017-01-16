package hammock

trait Codec[A] {
  class CodecException(val message: String) extends Throwable

  def encode(a: A): String
  def decode(a: String): Either[CodecException, A]

  implicit val stringCodec = new Codec[String] {
    def encode(a: String): String = a
    def decode(a: String): Either[CodecException, String] = Right(a)
  }
}

object Codec {
  def apply[A](implicit c: Codec[A]): Codec[A] = c
}
