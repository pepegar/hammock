import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.client.HttpClient

import cats._
import cats.implicits._

package object hammock {
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
}
