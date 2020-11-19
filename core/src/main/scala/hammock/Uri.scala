package hammock

import atto._
import Atto._
import cats._
import cats.implicits._
import Uri._
import cats.data.NonEmptyList
import Function.const

/**
 * Represents a [[HttpRequest]] URI.
 *
 * You have several different options for constructing [[Uri]]:
 *
 * {{{
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
  def /(str: String): Uri =
    copy(path = s"$path/$str")

  /**
   * Append query parameter to [[query]]
   *
   * @param key   - parameter name
   * @param value - the value
   * @return updated [[Uri]]
    **/
  def param(key: String, value: String): Uri = copy(query = this.query + (key -> value))

  /**
   * Appends multiple query parameters to [[query]]
   *
   * @param ps - parameters
   * @return updated [[Uri]]
    **/
  def params(ps: (String, String)*): Uri = ps match {
    case Seq() => this
    case _     => ps.foldLeft(this) { case (uri, (k, v)) => uri.copy(query = uri.query + (k -> v)) }
  }

  /**
   * Produces the same result as [[params]]
   * but provides syntax as you are writing URI query in browser
   * Usage example:
   * {{{
   *   uri"example.com" ? (("a" -> "b") & ("c" -> "d") & ("e" -> "f"))
   * }}}
   *
   * @param ps - parameters
   * @return updated [[Uri]]
    **/
  def ?(ps: NonEmptyList[(String, String)]): Uri = params(ps.toList: _*)
}

object Uri {

  sealed trait Host

  object Host {
    case class IPv4(a: Int, b: Int, c: Int, d: Int) extends Host
    object IPv4 {
      def parse: Parser[Host] =
        for {
          a <- ubyte <~ char('.')
          b <- ubyte <~ char('.')
          c <- ubyte <~ char('.')
          d <- ubyte
        } yield IPv4(a, b, c, d)
    }

    case class IPv6(
        a: IPv6Group,
        b: IPv6Group,
        c: IPv6Group,
        d: IPv6Group,
        e: IPv6Group,
        f: IPv6Group,
        g: IPv6Group,
        h: IPv6Group)
        extends Host

    object IPv6 {
      def parse: Parser[Host] =
        for {
          a <- IPv6Group.parse <~ char(':')
          b <- IPv6Group.parse <~ char(':')
          c <- IPv6Group.parse <~ char(':')
          d <- IPv6Group.parse <~ char(':')
          m <- moreGroups
        } yield IPv6(a, b, c, d, m._1, m._2, m._3, m._4)

      private def noMoreGroups: Parser[(IPv6Group, IPv6Group, IPv6Group, IPv6Group)] =
        char(':')
          .map(const((IPv6Group.empty, IPv6Group.empty, IPv6Group.empty, IPv6Group.empty)))

      private def fourMoreGroups: Parser[(IPv6Group, IPv6Group, IPv6Group, IPv6Group)] =
        for {
          e <- IPv6Group.parse <~ char(':')
          f <- IPv6Group.parse <~ char(':')
          g <- IPv6Group.parse <~ char(':')
          h <- IPv6Group.parse
        } yield (e, f, g, h)

      private def moreGroups: Parser[(IPv6Group, IPv6Group, IPv6Group, IPv6Group)] = noMoreGroups | fourMoreGroups
    }

    case class IPv6Group(value: Short)

    object IPv6Group {
      val empty = IPv6Group(0)

      implicit val showIpv6Group: Show[IPv6Group] = new Show[IPv6Group] {
        def show(group: IPv6Group): String = "%04X" format group.value
      }

      def parse: Parser[IPv6Group] = (manyN(4, hexDigit) | manyN(2, hexDigit)).map { chars =>
        IPv6Group(java.lang.Integer.parseInt(chars.mkString, 16).toShort)
      }
    }

    case object Localhost extends Host {
      def parse: Parser[Host] = string("localhost").map(const(Localhost))
    }

    case class Other(repr: String) extends Host

    object Other {
      def parse: Parser[Host] = many1(noneOf(":/?")).map(chars => Other(chars.toList.mkString))
    }

    /**
     * Adapted from http://tpolecat.github.io/atto/docs/next-steps.html
     */
    private val ubyte: Parser[Int] = {
      int
        .filter(n => n >= 0 && n < 256) // ensure value is in [0 .. 256)
        .namedOpaque("UByte") // give our parser a name
    }

    implicit val showHost: Show[Host] = new Show[Host] {
      def show(host: Host): String = host match {
        case Host.IPv4(a, b, c, d) => s"$a.$b.$c.$d"
        case Host.IPv6(a, b, c, d, e, f, g, h) =>
          val reprLastGroups: String =
            if (e.value.isEmpty &&
              f.value.isEmpty &&
              g.value.isEmpty &&
              h.value.isEmpty) ":" // just append another colon
            else
              e.show ++ ":" ++
                f.show ++ ":" ++
                g.show ++ ":" ++
                h.show

          "[" ++ a.show ++ ":" ++ b.show ++ ":" ++ c.show ++ ":" ++ d.show ++ ":" ++ reprLastGroups ++ "]"
        case Host.Localhost   => "localhost"
        case Host.Other(repr) => repr
      }
    }

    implicit val eqHost: Eq[Host] = Eq.fromUniversalEquals

    def parse: Parser[Host] =
      IPv4.parse |
        squareBrackets(IPv6.parse) |
        Localhost.parse |
        Other.parse
  }

  final case class Authority(user: Option[String], host: Host, port: Option[Long])

  object Authority {
    def parse: Parser[Authority] =
      for {
        user <- opt(userParser)
        host <- Host.parse
        port <- opt(char(':') ~> long)
      } yield Authority(user, host, port)

    implicit val showAuthority: Show[Authority] = new Show[Authority] {
      def show(auth: Authority): String =
        auth.user.fold("")(_ ++ "@") ++ auth.host.show ++ auth.port.fold("")(p => s":${p.toString}")
    }

    implicit val eqAuthority: Eq[Authority] = new Eq[Authority] {
      def eqv(a: Authority, b: Authority): Boolean =
        a.user === b.user && a.host === b.host && a.port === b.port
    }

    private def userParser: Parser[String] =
      many(noneOf("@,/?&=")).map(_.mkString) <~ char('@')
  }

  type Scheme   = String
  type Fragment = String

  implicit val showUri = new Show[Uri] {
    override def show(u: Uri): String = {
      val queryString = if (u.query.isEmpty) {
        ""
      } else {
        u.query.map(kv => s"${kv._1}=${kv._2}").mkString("?", "&", "")
      }

      u.scheme.fold("")(_ ++ "://") ++ u.authority.fold("")(_.show) ++ u.path ++ queryString ++ u.fragment.fold("")(
        "#" ++ _)
    }
  }

  implicit val eqUri: Eq[Uri] = Eq.fromUniversalEquals

  def queryParamParser: Parser[(String, String)] =
    (stringOf(notChar('=')) <~ char('=')) ~ takeWhile(x => x != '&' && x != '#')

  def queryParamsParser: Parser[Map[String, String]] = sepBy(queryParamParser, char('&')).map(_.toMap)

  def schemeParser: Parser[String] = takeWhile(_ != ':') <~ char(':') <~ opt(string("//"))

  def path: Parser[String] = char('/') ~> takeWhile(x => x != '?' && x != '#') map (p => "/" ++ p)

  def parser: Parser[Uri] =
    for {
      scheme      <- opt(schemeParser)
      authority   <- opt(Authority.parse)
      path        <- path | ok("")
      queryParams <- opt(char('?') ~> queryParamsParser)
      fragment    <- opt(char('#') ~> stringOf(anyChar))
    } yield Uri(scheme, authority, path, queryParams.getOrElse(Map()), fragment)

  def fromString(str: String): Either[String, Uri] = (parser parseOnly str).either

  def unsafeParse(str: String): Uri =
    fromString(str).getOrElse(throw ParseError(str))

  def isValid(str: String): Boolean = fromString(str).isRight

  final case class ParseError(uri: String) extends RuntimeException(s"unable to parse $uri")
}
