package hammock
package hi

import java.text.SimpleDateFormat
import java.util.Date
import monocle.macros.Lenses

import cats._
import cats.implicits._
import cats.syntax.flatMap._

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
  expires: Option[Date],
  maxAge: Option[Int],
  domain: Option[String],
  path: Option[String],
  secure: Option[Boolean],
  httpOnly: Option[Boolean],
  sameSite: Option[SameSite]
)

object Cookie {

  /**
    * simple constructor for cookies with just name and value
    */
  def apply(name: String, value: String): Cookie = Cookie(name, value, None, None, None, None, None, None, None)

  implicit val showCookie = new Show[Cookie] {
    def show(cookie: Cookie): String = render(cookie)
  }

  /**
    * renders a cookie in the Set-Cookie header format
    */
  def render(cookie: Cookie): String = {
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

    (s"${renderPair(cookie.name)(cookie.value)}" :: maybes).mkString("; ")
  }

  private val fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z")
}
