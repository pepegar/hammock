package hammock

import atto._
import Atto._
import cats._
import Uri._


/**
  * Represents a [[HttpRequest]] URI.
  *
  *  You have several different options for constructing [[Uri]]:
  *
  *  {{{
  * scala> val uri1 = uri"http://google.com"
  * uri1: hammock.Uri = Uri(Some(http),None,google.com,Map(),None)
  *
  * scala> val uri2 = Uri(None, None, "path", Map(), None)
  * uri2: hammock.Uri = Uri(None,None,path,Map(),None)
  *
  * scala> val uri3 = Uri.fromString("http://google.com")
  * uri3: Either[String,hammock.Uri] = Right(Uri(Some(http),None,google.com,Map(),None))
  * }}}
  *
  * @param scheme    scheme of the uri. For example https
  * @param authority authority of the uri. For example: user:pass@google.com:443
  * @param path      path of the uri. For example /books/234
  * @param query     query string of the uri. For example ?page=3&utm_source=campaign
  * @param fragment  fragment of the uri. For example #header1
  */
case class Uri(
    scheme: Option[Scheme] = None,
    authority: Option[Authority] = None,
    path: String = "",
    query: Map[String, String] = Map(),
  fragment: Option[Fragment] = None) {

  /** Append a string to the path of the [[Uri]]
    */
  def /(str: String): Uri = {
    copy(path = s"$path/$str")
  }
}

object Uri {
  type Authority = String
  type Scheme    = String
  type Fragment  = String

  implicit val show = new Show[Uri] {
    override def show(u: Uri): String = {
      val queryString = if (u.query.isEmpty) {
        ""
      } else {
        u.query.map(kv => s"${kv._1}=${kv._2}").mkString("?", "&", "")
      }

      u.scheme.fold("")(_ ++ "://") ++ u.authority.fold("")(_ ++ "@") ++ u.path ++ queryString ++ u.fragment.fold("")(
        "#" ++ _)
    }
  }

  def queryParam: Parser[(String, String)] =
    (stringOf(notChar('=')) <~ char('=')) ~ takeWhile(x => x != '&' && x != '#')

  def queryParams: Parser[Map[String, String]] = sepBy(queryParam, char('&')).map(_.toMap)

  def scheme: Parser[String] = takeWhile(_ != ':') <~ string("://")

  def authority: Parser[String] = takeWhile(_ != '@') <~ char('@')

  def parser: Parser[Uri] =
    for {
      scheme      <- opt(scheme)
      authority   <- opt(authority)
      path        <- takeWhile(x => x != '?' && x != '#')
      queryParams <- opt(char('?') ~> queryParams)
      fragment    <- opt(char('#') ~> stringOf(anyChar))
    } yield Uri(scheme, authority, path, queryParams.getOrElse(Map()), fragment)

  def fromString(str: String): Either[String, Uri] = (parser parseOnly str).either

  def unsafeParse(str: String): Uri = fromString(str).right.get

  def isValid(str: String): Boolean = fromString(str).isRight
}
