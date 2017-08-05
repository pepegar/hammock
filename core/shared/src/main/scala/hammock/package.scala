import cats._
import cats.free._
import cats.implicits._
import cats.effect.Sync

package object hammock {
  import hammock.free.algebra.HttpRequestF
  import hammock.free.InterpTrans

  implicit class HttpRequestIOSyntax[A](fa: Free[HttpRequestF, A]) {
    def exec[F[_]: Sync](implicit interp: InterpTrans) =
      fa foldMap interp.trans
  }

  implicit class HttpResponseSyncAs[F[_]: Sync](fa: F[HttpResponse]) {
    def as[T : Codec]: F[T] = fa >>= { f =>
      Sync[F].delay {
        Codec[T].decode(f.content) match {
          case Right(x) => x
          case Left(ex) => throw ex
        }
      }
    }
  }
}
