import cats._
import cats.data.{EitherK, NonEmptyList}
import cats.free.Free
import cats.effect.Sync
import contextual._


package object hammock {

  import hammock.marshalling._
  import hammock.InterpTrans


  type HammockF[A] = EitherK[HttpF, MarshallF, A]

  implicit class HttpRequestIOSyntax[A](fa: Free[HttpF, A]) {
    def exec[F[_] : Sync](implicit interp: InterpTrans[F]): F[A] =
      fa foldMap interp.trans
  }

  implicit class HammockFSyntax[A](fa: Free[HammockF, A]) {
    def exec[F[_] : Sync](implicit NT: HammockF ~> F): F[A] =
      fa foldMap NT
  }


  implicit class AsSyntaxOnHttpF[A](fa: Free[HttpF, HttpResponse]) {
    def as[B](implicit D: Decoder[B], M: MarshallC[HammockF]): Free[HammockF, B] =
      fa.inject[HammockF] flatMap { response =>
        M.unmarshall(response.entity)
      }
  }

  implicit def hammockNT[F[_] : Sync](
                                       implicit H: InterpTrans[F],
                                       M: MarshallF ~> F
                                     ): HammockF ~> F = H.trans or M


  object UriContext extends Context

  object UriInterpolator extends Interpolator {
    type Output = Uri
    type ContextType = UriContext.type
    type Input = String
    def contextualize(interpolation: StaticInterpolation) = {
      interpolation.parts.foldLeft(List.empty[ContextType]) {
        case (contexts, Hole(_, _)) => UriContext :: contexts
        case (contexts, _) => contexts
      }
    }

    def evaluate(interpolation: RuntimeInterpolation): Uri =
      Uri.fromString(interpolation.literals.head).right.get
  }

  implicit val embedString = UriInterpolator.embed[String](Case(UriContext, UriContext) { x => x })

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

  /**
    * Methods providing URI query parameters building syntax
    * Used in [[Uri.?]] method
    **/
  implicit class UriQueryParamsBuilder(val self: NonEmptyList[(String, String)]) extends AnyVal {
    def &(param: (String, String)): NonEmptyList[(String, String)] = param :: self
  }
  implicit class UriQueryInitBuilder(val self: (String, String)) extends AnyVal {
    def &(param: (String, String)): NonEmptyList[(String, String)] = NonEmptyList(self, param :: Nil)
  }
}
