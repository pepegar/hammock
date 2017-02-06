package hammock
package hi

import cats._
import org.scalatest._

class AuthSpec extends WordSpec with Matchers {
  import Auth._

  "Show[Auth]" should {

    "encode basic auth correctly" in {
      val user = "Aladdin"
      val pass = "OpenSesame"

      val auth = Auth.BasicAuth(user, pass)

      Show[Auth].show(auth) shouldEqual "Basic QWxhZGRpbjpPcGVuU2VzYW1l"
    }

    "encode Oauth2 bearer auth correctly" in {
      val auth = Auth.OAuth2Bearer("token")

      Show[Auth].show(auth) shouldEqual "Bearer token"
    }

    "encode Oauth2 token auth correctly" in {
      val auth = Auth.OAuth2Token("token")

      Show[Auth].show(auth) shouldEqual "token token"
    }
  }
}
