import cats._
import cats.free._
import cats.implicits._

package object hammock {
  import hammock.free.algebra.HttpRequestF
  import hammock.free.InterpTrans

  implicit class HttpRequestIOSyntax[A](fa: Free[HttpRequestF, A]) {
    def exec[F[_]](implicit interp: InterpTrans, ME: MonadError[F, Throwable]) =
      fa foldMap interp.trans(ME)
  }

  implicit class HttpResponseMonadErrorAs[F[_]: MonadError[?[_], Throwable]](fa: F[HttpResponse]) {
    def as[T : Codec]: F[T] = fa >>= { f =>
      MonadError[F, Throwable].catchNonFatal {
        Codec[T].decode(f.content) match {
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
