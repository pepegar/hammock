import cats._
import cats.data.EitherK
import cats.free.Free
import cats.effect.Sync

package object hammock {
  import hammock.marshalling._
  import hammock.InterpTrans

  type HammockF[A] = EitherK[HttpF, MarshallF, A]

  implicit class HttpRequestIOSyntax[A](fa: Free[HttpF, A]) {
    def exec[F[_]: Sync](implicit interp: InterpTrans[F]): F[A] =
      fa foldMap interp.trans
  }

  implicit class HammockFSyntax[A](fa: Free[HammockF, A]) {
    def exec[F[_]: Sync](implicit NT: HammockF ~> F): F[A] =
      fa foldMap NT
  }


  implicit class AsSyntaxOnHttpF[A](fa: Free[HttpF, HttpResponse]) {
    def as[B](implicit D: Decoder[B], M: MarshallC[HammockF]): Free[HammockF, B] =
      fa.inject[HammockF] flatMap { response =>
         M.unmarshall(response.entity)
      }
  }

  implicit def hammockNT[F[_]: Sync](
    implicit H: InterpTrans[F],
    M: MarshallF ~> F
  ): HammockF ~> F = H.trans or M
}
