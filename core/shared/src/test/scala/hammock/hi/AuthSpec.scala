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

  "Eq[Auth]" should {

    "equal BasicAuth instances" in {
      val authAladdinOne = Auth.BasicAuth("Aladdin", "OpenSesame")
      val authAladdinTwo = Auth.BasicAuth("Aladdin", "OpenSesame")

      assert(authAladdinOne == authAladdinTwo)
    }

    "not equal BasicAuth instances" in {

      val authAladdin = Auth.BasicAuth("Aladdin", "OpenSesame")
      val authSud = Auth.BasicAuth("Sud", "Sabin")

      assert(authAladdin != authSud)
    }

    "equal OAuth2Bearer instances" in {

      val authAladdinOne = Auth.OAuth2Bearer("tokenAladdin")
      val authAladdinTwo = Auth.OAuth2Bearer("tokenAladdin")

      assert(authAladdinOne == authAladdinTwo)
    }

    "not equal OAuth2Bearer instances" in {

      val authAladdin = Auth.OAuth2Bearer("tokenAladdin")
      val authSud = Auth.OAuth2Bearer("tokenSud")

      assert(authAladdin != authSud)
    }

    "equal OAuth2Token instances" in {

      val authAladdinOne = Auth.OAuth2Token("tokenAladdin")
      val authAladdinTwo = Auth.OAuth2Token("tokenAladdin")

      assert(authAladdinOne == authAladdinTwo)
    }

    "not equal OAuth2Token instances" in {

      val authAladdin = Auth.OAuth2Token("tokenAladdin")
      val authSud = Auth.OAuth2Token("tokenSud")

      assert(authAladdin != authSud)
    }
  }
}
