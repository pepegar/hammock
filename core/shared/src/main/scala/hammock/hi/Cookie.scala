package hammock
package hi

import java.util.Date

import cats._
import cats.implicits._
import hammock.hi.Cookie.SameSite
import hammock.hi.platformspecific._
import monocle.Optional
import monocle.macros.Lenses

@Lenses case class Cookie(
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
  val expiresOpt: Optional[Cookie, Date] = Optional[Cookie, Date] {
    _.expires
  } { date =>
    {
      case cookie @ Cookie(_, _, None, _, _, _, _, _, _, _) => cookie
      case cookie @ _                                       => cookie.copy(expires = Some(date))
    }
  }
  val maxAgeOpt: Optional[Cookie, Int] = Optional[Cookie, Int] {
    _.maxAge
  } { age =>
    {
      case cookie @ Cookie(_, _, _, None, _, _, _, _, _, _) => cookie
      case cookie @ _                                       => cookie.copy(maxAge = Some(age))
    }
  }
  val domainOpt: Optional[Cookie, String] = Optional[Cookie, String] {
    _.domain
  } { domain =>
    {
      case cookie @ Cookie(_, _, _, _, None, _, _, _, _, _) => cookie
      case cookie @ _                                       => cookie.copy(domain = Some(domain))
    }
  }
  val pathOpt: Optional[Cookie, String] = Optional[Cookie, String] {
    _.path
  } { path =>
    {
      case cookie @ Cookie(_, _, _, _, _, None, _, _, _, _) => cookie
      case cookie @ _                                       => cookie.copy(path = Some(path))
    }
  }
  val secureOpt: Optional[Cookie, Boolean] = Optional[Cookie, Boolean] {
    _.secure
  } { secure =>
    {
      case cookie @ Cookie(_, _, _, _, _, _, None, _, _, _) => cookie
      case cookie @ _                                       => cookie.copy(secure = Some(secure))
    }
  }
  val httpOnlyOpt: Optional[Cookie, Boolean] = Optional[Cookie, Boolean] {
    _.httpOnly
  } { httpOnly =>
    {
      case cookie @ Cookie(_, _, _, _, _, _, _, None, _, _) => cookie
      case cookie @ _                                       => cookie.copy(httpOnly = Some(httpOnly))
    }
  }
  val sameSiteOpt: Optional[Cookie, SameSite] = Optional[Cookie, SameSite] {
    _.sameSite
  } { sameSite =>
    {
      case cookie @ Cookie(_, _, _, _, _, _, _, _, None, _) => cookie
      case cookie @ _                                       => cookie.copy(sameSite = Some(sameSite))
    }
  }
  val customOpt: Optional[Cookie, Map[String, String]] = Optional[Cookie, Map[String, String]] {
    _.custom
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

    implicit val sameSiteShow = new Show[SameSite] {
      def show(s: SameSite): String = s match {
        case Strict => "Strict"
        case Lax    => "Lax"
      }
    }

    implicit val sameSiteEq = new Eq[SameSite] {
      def eqv(a: SameSite, b: SameSite): Boolean = (a, b) match {
        case (Strict, Strict) => true
        case (Lax, Lax) => true
        case _ => false
      }
    }
  }

  implicit val cookieShow = new Show[Cookie] {
    def show(cookie: Cookie): String = render(cookie)
  }

  implicit val cookieEq: Eq[Cookie] = new Eq[Cookie] {
    def eqv(a: Cookie, b: Cookie): Boolean = {
    a.name === b.name &&
    a.value === b.value &&
    a.expires.equals(b.expires) &&
    a.maxAge === b.maxAge &&
    a.domain === b.domain &&
    a.path === b.path &&
    a.secure === b.secure &&
    a.httpOnly === b.httpOnly &&
    a.sameSite === b.sameSite &&
    a.custom === b.custom
    }
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
