import cats._
import cats.free._
import cats.implicits._
import cats.effect.Sync

package object hammock {
  import hammock.free.algebra.HttpRequestF
  import hammock.free.InterpTrans

  implicit class HttpRequestIOSyntax[A](fa: Free[HttpRequestF, A]) {
    def exec[F[_]: Sync](implicit interp: InterpTrans[F]) =
      fa foldMap interp.trans
  }

  implicit class HttpResponseSyncAs[F[_]: Sync](fa: F[HttpResponse]) {
    def as[T: Codec]: F[T] = fa >>= { f =>
      Sync[F].delay {
        Codec[T].decode(f.entity) match {
          case Right(x) => x
          case Left(ex) => throw ex
        }
      }
    }
  }

  implicit class HttpResponseToDecodeIOSyntax[T : Codec](fa: Free[HttpRequestF, HttpResponseToDecode[T]]) {
    def exec[F[_]](implicit interp: InterpTrans, ME: MonadError[F, Throwable]): F[T] =
      fa.foldMap(interp.trans(ME)).map(_.response).as[T]
  }

  implicit class HttpResponseAs[A](fa: Free[HttpRequestF, HttpResponse]) {
    def as[T : Codec]: Free[HttpRequestF, HttpResponseToDecode[T]] = fa.map(HttpResponseToDecode.apply[T])
  }
}
