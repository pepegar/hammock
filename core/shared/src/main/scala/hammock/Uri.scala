package hammock


import atto._
import atto.compat.cats._
import Atto._

import cats._
import cats.implicits._

import Uri._

case class Uri(
  scheme: Option[Scheme] = None,
  authority: Option[Authority] = None,
  path: String = "",
  query: Map[String, String] = Map(),
  fragment: Option[Fragment] = None
)

object Uri {
  type Error = String
  type Authority = String
  type Scheme = String
  type Fragment = String

  implicit val semigroup = new Semigroup[Uri] {
    def combine(x: Uri,y: Uri): Uri = ???
  }

  implicit val show = new Show[Uri] {
    override def show(u: Uri): String = u.scheme.fold("")(_ ++ "://") ++
      u.authority.fold("")(_ ++ "@") ++
      u.path ++ "?" ++
      u.query.map(kv => s"${kv._1}=${kv._2}").mkString("&") ++
      u.fragment.fold("")("#" ++ _)
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

  def fromString(str: String): Either[Error, Uri] = (parser parseOnly str).either
}
