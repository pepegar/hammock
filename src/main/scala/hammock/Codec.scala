package hammock

trait Codec[A] {
  import Codec._

  def encode(a: A): String
  def decode(a: String): Either[CodecException, A]
}

object Codec {
  class CodecException(val message: String) extends Throwable

  def apply[A](implicit c: Codec[A]): Codec[A] = c
}
