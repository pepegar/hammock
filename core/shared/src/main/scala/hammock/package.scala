import cats._
import cats.data.EitherK
import cats.free.Free
import cats.effect.Sync
import contextual._

package object hammock {
  import Uri._
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


  object UriContext extends Context

  object UriInterpolator extends Interpolator {
    type Output = Uri
    type Context = UriContext.type
    type Input = String
    def contextualize(interpolation: StaticInterpolation) = {
      val lit@Literal(_, uriString) = interpolation.parts.head

      if(!isValid(uriString))
        interpolation.abort(lit, 0, "not a valid URL")

      Nil
    }

    def evaluate(interpolation: RuntimeInterpolation): Uri =
      Uri.fromString(interpolation.literals.head).right.get
  }

  implicit val embedString = UriInterpolator.embed[String](Case(UriContext, UriContext){x => x})

  /**
    * Unsafe string interpolator allowing uri parsing.  It's unsafe
    * because in case of any error happen (a Left is returned by the
    * `fromString` method), it will throw an exception.
    *
    * {{{
    * scala> uri"http://user:pass@pepegar.com/path?page=4#index"
    * res1: hammock.Uri = Uri(Some(http),Some(user:pass),pepegar.com/path,Map(page -> 4),Some(index))
    * }}}
    */
  implicit class UriStringContext(sc: StringContext) {
    val uri = Prefix(UriInterpolator, sc)
  }
}
