package hammock

import cats._
import simulacrum.typeclass

class CodecException private (val message: String, underlying: Throwable) extends Throwable(message, underlying)

object CodecException {
  def withMessage(message: String)                            = new CodecException(message, null)
  def withMessageAndException(message: String, ex: Throwable) = new CodecException(message, ex)
}

@typeclass trait Encoder[A] {
  def encode(a: A): Entity
}

object Encoder {
  implicit val encoderContravariant: Contravariant[Encoder] = new Contravariant[Encoder] {
    def contramap[A, B](fa: Encoder[A])(fn: B => A): Encoder[B] = new Encoder[B] {
      def encode(b: B): Entity = fa.encode(fn(b))
    }
  }
}

@typeclass trait Decoder[A] {
  def decode(a: Entity): Either[CodecException, A]
}

@typeclass trait Codec[A] extends Encoder[A] with Decoder[A]
