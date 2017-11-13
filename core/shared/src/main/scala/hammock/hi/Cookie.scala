package hammock
package hi

import java.util.Date

import monocle.macros.GenLens
import cats._
import cats.implicits._
import hammock.hi.Cookie.SameSite
import monocle.{Lens, Optional}

case class Cookie(
    name: String,
    value: String,
    expires: Option[Date] = None,
    maxAge: Option[Int] = None,
    domain: Option[String] = None,
    path: Option[String] = None,
    secure: Option[Boolean] = None,
    httpOnly: Option[Boolean] = None,
    sameSite: Option[SameSite] = None,
    custom: Option[Map[String, String]] = None
)

object Cookie {

  val name: Lens[Cookie, String]  = GenLens[Cookie](_.name)
  val value: Lens[Cookie, String] = GenLens[Cookie](_.value)
  val expires: Optional[Cookie, Date] = Optional[Cookie, Date] {
    case Cookie(_, _, x @ Some(date), _, _, _, _, _, _, _) => x
    case _                                                 => None
  } { date =>
    {
      case cookie @ Cookie(_, _, None, _, _, _, _, _, _, _) => cookie
      case cookie @ _                                       => cookie.copy(expires = Some(date))
    }
  }
  val maxAge: Optional[Cookie, Int] = Optional[Cookie, Int] {
    case Cookie(_, _, _, x @ Some(maxAge), _, _, _, _, _, _) => x
    case _                                                   => None
  } { age =>
    {
      case cookie @ Cookie(_, _, _, None, _, _, _, _, _, _) => cookie
      case cookie @ _                                       => cookie.copy(maxAge = Some(age))
    }
  }
  val domain: Optional[Cookie, String] = Optional[Cookie, String] {
    case Cookie(_, _, _, _, x @ Some(domain), _, _, _, _, _) => x
    case _                                                   => None
  } { domain =>
    {
      case cookie @ Cookie(_, _, _, _, None, _, _, _, _, _) => cookie
      case cookie @ _                                       => cookie.copy(domain = Some(domain))
    }
  }
  val path: Optional[Cookie, String] = Optional[Cookie, String] {
    case Cookie(_, _, _, _, _, x @ Some(path), _, _, _, _) => x
    case _                                                 => None
  } { path =>
    {
      case cookie @ Cookie(_, _, _, _, _, None, _, _, _, _) => cookie
      case cookie @ _                                       => cookie.copy(path = Some(path))
    }
  }
  val secure: Optional[Cookie, Boolean] = Optional[Cookie, Boolean] {
    case Cookie(_, _, _, _, _, _, x @ Some(secure), _, _, _) => x
    case _                                                   => None
  } { secure =>
    {
      case cookie @ Cookie(_, _, _, _, _, _, None, _, _, _) => cookie
      case cookie @ _                                       => cookie.copy(secure = Some(secure))
    }
  }
  val httpOnly: Optional[Cookie, Boolean] = Optional[Cookie, Boolean] {
    case Cookie(_, _, _, _, _, _, _, x @ Some(httpOnly), _, _) => x
    case _                                                     => None
  } { httpOnly =>
    {
      case cookie @ Cookie(_, _, _, _, _, _, _, None, _, _) => cookie
      case cookie @ _                                       => cookie.copy(httpOnly = Some(httpOnly))
    }
  }
  val sameSite: Optional[Cookie, SameSite] = Optional[Cookie, SameSite] {
    case Cookie(_, _, _, _, _, _, _, _, x @ Some(sameSite), _) => x
    case _                                                     => None
  } { sameSite =>
    {
      case cookie @ Cookie(_, _, _, _, _, _, _, _, None, _) => cookie
      case cookie @ _                                       => cookie.copy(sameSite = Some(sameSite))
    }
  }
  val custom: Optional[Cookie, Map[String, String]] = Optional[Cookie, Map[String, String]] {
    case Cookie(_, _, _, _, _, _, _, _, _, x @ Some(custom)) => x
    case _                                                   => None
  } { custom =>
    {
      case cookie @ Cookie(_, _, _, _, _, _, _, _, _, None) => cookie
      case cookie @ _                                       => cookie.copy(custom = Some(custom))
    }
  }

  sealed trait SameSite
  object SameSite {
    case object Strict extends SameSite
    case object Lax    extends SameSite

    implicit val show = new Show[SameSite] {
      def show(s: SameSite): String = s match {
        case Strict => "Strict"
        case Lax    => "Lax"
      }
    }
  }

  implicit val showCookie = new Show[Cookie] {
    def show(cookie: Cookie): String = render(cookie)
  }

  /**
   * renders a cookie in the Set-Cookie header format
   */
  def render(cookie: Cookie)(implicit fmt: DateFormatter): String = {
    def renderPair[S: Show](k: String)(v: S)              = k ++ "=" ++ Show[S].show(v)
    def maybeShowDate(date: Option[Date]): Option[String] = date map (date => fmt.format(date))
    def expires                                           = maybeShowDate(cookie.expires) map renderPair("Expires")
    def maxAge                                            = cookie.maxAge map renderPair("MaxAge")
    def domain                                            = cookie.domain map renderPair("Domain")
    def path                                              = cookie.path map renderPair("Path")
    def secure                                            = cookie.secure map renderPair("Secure")
    def httpOnly                                          = cookie.httpOnly map renderPair("HttpOnly")
    def sameSite                                          = cookie.sameSite map renderPair("SameSite")

    val maybes = List(expires, maxAge, domain, path, secure, httpOnly, sameSite)
      .filter(_.nonEmpty)
      .map(_.get)

    val custom: List[String] = cookie.custom match {
      case None        => Nil
      case Some(elems) => elems.map { case (k, v) => renderPair(k)(v) } toList
    }

    (s"${renderPair(cookie.name)(cookie.value)}" :: maybes ::: custom).mkString("; ")
  }
}
