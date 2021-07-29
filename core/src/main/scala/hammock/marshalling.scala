package hammock

import cats._
import cats.free._

object marshalling {

  sealed trait MarshallF[A] {
    def dec: Decoder[A] //
  }
  object MarshallF {
    def unmarshall[A: Decoder](entity: Entity): MarshallF[A] =
      Unmarshall(entity)

    private[marshalling] final case class Unmarshall[A](entity: Entity)(implicit D: Decoder[A]) extends MarshallF[A] {
      def dec = D
    }
  }

  object Ops {
    def unmarshall[A: Decoder](entity: Entity): Free[MarshallF, A] =
      Free.liftF(MarshallF.unmarshall(entity))
  }

  class MarshallC[F[_]](implicit I: MarshallF :<: F) {
    def unmarshall[A: Decoder](entity: Entity): Free[F, A] =
      Ops.unmarshall(entity).inject
  }

  implicit def marshallC[F[_]](implicit I: InjectK[MarshallF, F]): MarshallC[F] = new MarshallC[F]

  implicit def marshallNT[F[_]: MonadThrow]: MarshallF ~> F = λ[MarshallF ~> F] {
    case um @ MarshallF.Unmarshall(entity) =>
      um.dec
        .decode(entity)
        .fold(MonadThrow[F].raiseError, MonadThrow[F].pure)
  }
}
