package hammock
package hi

import java.util.Base64
import cats._

sealed trait Auth
object Auth {
  case class BasicAuth(user: String, pass: String) extends Auth
  case class OAuth2Bearer(token: String) extends Auth
  case class OAuth2Token(token: String) extends Auth

  implicit val authShow = new Show[Auth] {
    def show(a: Auth): String = a match {
      case BasicAuth(user, pass) =>
        val toEncode = s"$user:$pass".getBytes
        val encoded = Base64.getEncoder().encode(toEncode)
        s"Basic ${new String(encoded)}"
      case OAuth2Bearer(token) => s"Bearer $token"
      case OAuth2Token(token) => s"token $token"
    }
  }
}
