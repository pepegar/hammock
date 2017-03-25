package hammock

import atto._
import atto.compat.cats._
import Atto._

import cats._
import cats.implicits._

import contextual._

import Uri._

case class Uri(
  scheme: Option[Scheme] = None,
  authority: Option[Authority] = None,
  path: String = "",
  query: Map[String, String] = Map(),
  fragment: Option[Fragment] = None
)

object Uri {
  type Authority = String
  type Scheme = String
  type Fragment = String

  implicit val show = new Show[Uri] {
    override def show(u: Uri): String = {
      val queryString = if (u.query.isEmpty) {
        ""
      } else {
        u.query.map(kv => s"${kv._1}=${kv._2}").mkString("?", "&", "")
      }

      u.scheme.fold("")(_ ++ "://") ++ u.authority.fold("")(_ ++ "@") ++ u.path ++ queryString ++ u.fragment.fold("")("#" ++ _)
    }
  }

  def queryParam: Parser[(String, String)] = (stringOf(notChar('=')) <~ char('=')) ~ takeWhile(x => x != '&' && x != '#')

  def queryParams: Parser[Map[String, String]] = sepBy(queryParam, char('&')).map(_.toMap)

  def scheme: Parser[String] = takeWhile(_ != ':') <~ string("://")

  def authority: Parser[String] = takeWhile(_ != '@') <~ char('@')

  def parser: Parser[Uri] = for {
    scheme <- opt(scheme)
    authority <- opt(authority)
    path <- takeWhile(x => x != '?' && x != '#')
    queryParams <- opt(char('?') ~> queryParams)
    fragment <- opt(char('#') ~> stringOf(anyChar))
  } yield Uri(scheme, authority, path, queryParams.getOrElse(Map()), fragment)

  def fromString(str: String): Either[String, Uri] = (parser parseOnly str).either

  def isValid(str: String): Boolean = fromString(str).isRight

  object UriInterpolator extends Interpolator {
    def contextualize(interpolation: StaticInterpolation) = {
      val lit@Literal(_, uriString) = interpolation.parts.head
      if(!isValid(uriString))
        interpolation.abort(lit, 0, "not a valid URL")

      Nil
    }

    def evaluate(interpolation: RuntimeInterpolation): Uri =
      Uri.fromString(interpolation.literals.head).right.get
  }

  /**
    * String context allowing compile-time uri parsing.
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
