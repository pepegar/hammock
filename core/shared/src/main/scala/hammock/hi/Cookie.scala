package hammock
package hi

import java.util.Date
import monocle.macros.Lenses

import cats._
import cats.implicits._

sealed trait SameSite
object SameSite {
  implicit val show = new Show[SameSite] {
    def show(s: SameSite): String = s match {
      case Strict => "Strict"
      case Lax => "Lax"
    }
  }

  case object Strict extends SameSite
  case object Lax extends SameSite
}

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

  implicit val showCookie = new Show[Cookie] {
    def show(cookie: Cookie): String = render(cookie)
  }

  /**
    * renders a cookie in the Set-Cookie header format
    */
  def render(cookie: Cookie)(implicit fmt: DateFormatter): String = {
    def renderPair[S : Show](k: String)(v: S) = k ++ "=" ++ Show[S].show(v)
    def maybeShowDate(date: Option[Date]): Option[String] = date map (date => fmt.format(date))
    def expires = maybeShowDate(cookie.expires) map renderPair("Expires")
    def maxAge = cookie.maxAge map renderPair("MaxAge")
    def domain = cookie.domain map renderPair("Domain")
    def path = cookie.path map renderPair("Path")
    def secure = cookie.secure map renderPair("Secure")
    def httpOnly = cookie.httpOnly map renderPair("HttpOnly")
    def sameSite = cookie.sameSite map renderPair("SameSite")

    val maybes = List(expires, maxAge, domain, path, secure, httpOnly, sameSite)
      .filter(_.nonEmpty).map(_.get)

    val custom: List[String] = cookie.custom match {
      case None => Nil
      case Some(elems) => elems.map { case (k, v) => renderPair(k)(v) } toList
    }

    (s"${renderPair(cookie.name)(cookie.value)}" :: maybes ::: custom).mkString("; ")
  }
}
