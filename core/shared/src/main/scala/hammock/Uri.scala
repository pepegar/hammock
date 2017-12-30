package hammock

import atto._
import Atto._
import cats._
import Uri._
import Function.const
import cats.syntax.show._

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
  sealed trait Host
  object Host {
    case class IPv4(a: Int, b: Int, c: Int, d: Int) extends Host
    case class IPv6(a: IPv6Group, b: IPv6Group, c: IPv6Group, d: IPv6Group, e: IPv6Group, f: IPv6Group, g: IPv6Group, h: IPv6Group) extends Host
    case object Localhost extends Host
    case class Other(repr: String) extends Host

    case class IPv6Group(bytes: Vector[Byte])

    implicit val showIpv6Group: Show[IPv6Group] = new Show[IPv6Group] {
      def show(group: IPv6Group): String = group.bytes.map("%02X" format _).mkString
    }

    object IPv6Group {
      val empty = IPv6Group(Vector.empty[Byte])
    }

    def ipv6Group: Parser[IPv6Group] = many(hexDigit).map { chars =>
      IPv6Group(
        chars.mkString
          .sliding(2,2)
          .toVector.map(Integer.parseInt(_, 16).toByte)
      )
    }

    /**
      * Adapted from http://tpolecat.github.io/atto/docs/next-steps.html
      */
    val ubyte: Parser[Int] = {
      int.filter(n => n >= 0 && n < 256) // ensure value is in [0 .. 256)
         .namedOpaque("UByte")           // give our parser a name
    }

    def ipv4: Parser[Host] = for {
      a <- ubyte <~ char('.')
      b <- ubyte <~ char('.')
      c <- ubyte <~ char('.')
      d <- ubyte
    } yield IPv4(a, b, c, d)

    def noMoreGroups: Parser[(IPv6Group, IPv6Group, IPv6Group, IPv6Group)] = char(':')
      .map(const((IPv6Group.empty,IPv6Group.empty,IPv6Group.empty,IPv6Group.empty)))

    def fourMoreGroups: Parser[(IPv6Group, IPv6Group, IPv6Group, IPv6Group)] = for {
      e <- ipv6Group <~ char(':')
      f <- ipv6Group <~ char(':')
      g <- ipv6Group <~ char(':')
      h <- ipv6Group
    } yield (e, f, g, h)

    def moreGroups: Parser[(IPv6Group, IPv6Group, IPv6Group, IPv6Group)] = noMoreGroups | fourMoreGroups

    def ipv6: Parser[Host] = for {
      a <- ipv6Group <~ char(':')
      b <- ipv6Group <~ char(':')
      c <- ipv6Group <~ char(':')
      d <- ipv6Group <~ char(':')
      m <- moreGroups
    } yield IPv6(a,b,c,d,m._1,m._2,m._3,m._4)

    def parseHost: Parser[Host] = ipv4 |
      squareBrackets(ipv6) |
      string("localhost").map(const(Localhost: Host)) |
      many1(noneOf(":/?")).map(chars => Other(chars.toList.mkString))
  }

    implicit val showHost: Show[Host] = new Show[Host] {
      def show(host: Host): String = host match {
        case Host.IPv4(a,b,c,d) => s"$a.$b.$c.$d"
        case Host.IPv6(a,b,c,d,e,f,g,h) =>
          def reprLastGroups: String =
            if (
              e.bytes.isEmpty &&
              f.bytes.isEmpty &&
              g.bytes.isEmpty &&
              h.bytes.isEmpty) ":" // just append another colon
            else
              e.show ++ ":" ++
              f.show ++ ":" ++
              g.show ++ ":" ++
              h.show

          "[" ++ a.show ++ ":" ++ b.show ++ ":" ++ c.show ++ ":" ++ d.show  ++ ":" ++ reprLastGroups ++ "]"
        case Host.Localhost => "localhost"
        case Host.Other(repr) => repr
      }
    }

  final case class Authority(user: Option[String], host: Host, port: Option[Long])

  implicit val showAuthority: Show[Authority] = new Show[Authority] {
    def show(auth: Authority): String =
      auth.user.fold("")(_ ++ "@") ++ auth.host.show ++ auth.port.fold("")(p => s":${p.toString}")
  }

  object Authority {
    def userParser: Parser[String] =
      many(noneOf("@,/?&=")).map(_.mkString) <~ char('@')

    def authorityParser: Parser[Authority] = for {
      user <- opt(userParser)
      host <- Host.parseHost
      port <- opt(char(':') ~> long)
    } yield Authority(user, host, port)
  }

  type Scheme    = String
  type Fragment  = String

  implicit val show = new Show[Uri] {
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

  def queryParamParser: Parser[(String, String)] =
    (stringOf(notChar('=')) <~ char('=')) ~ takeWhile(x => x != '&' && x != '#')

  def queryParamsParser: Parser[Map[String, String]] = sepBy(queryParamParser, char('&')).map(_.toMap)

  def schemeParser: Parser[String] = takeWhile(_ != ':') <~ char(':') <~ opt(string("//"))

  def path: Parser[String] = char('/') ~> takeWhile(x => x != '?' && x != '#') map(p => "/" ++ p)

  def parser: Parser[Uri] =
    for {
      scheme      <- opt(schemeParser)
      authority   <- opt(Authority.authorityParser)
      path        <- path | ok("")
      queryParams <- opt(char('?') ~> queryParamsParser)
      fragment    <- opt(char('#') ~> stringOf(anyChar))
    } yield Uri(scheme, authority, path, queryParams.getOrElse(Map()), fragment)

  def fromString(str: String): Either[String, Uri] = (parser parseOnly str).either

  def unsafeParse(str: String): Uri = fromString(str).right.get

  def isValid(str: String): Boolean = fromString(str).isRight
}
