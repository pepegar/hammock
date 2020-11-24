package hammock
package hi

import java.time.ZonedDateTime

import cats._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CookieSpec extends AnyWordSpec with Matchers {

  "Show[Cookie].show" should {
    "render a simple cookie in the correct format" in {
      val cookie = Cookie("name", "value")

      Show[Cookie].show(cookie) shouldEqual "name=value"
    }

    "render a complex cookie in the correct format" in {
      val cookie = Cookie(
        "name",
        "value",
        Some(ZonedDateTime.parse("2020-01-04T17:03:54.000Z")),
        Some(123),
        Some("pepegar.com"),
        Some("/blog"),
        Some(false),
        Some(true),
        Some(Cookie.SameSite.Strict)
      )

      Show[Cookie].show(
        cookie
      ) shouldEqual "name=value; Expires=Sat, 04 Jan 2020 17:03:54 GMT; MaxAge=123; Domain=pepegar.com; Path=/blog; Secure=false; HttpOnly=true; SameSite=Strict"
    }

    "render a cookie with custom values in the correct format" in {
      val cookie = Cookie("hello", "dolly", custom = Some(Map("potatoes" -> "22")))

      Show[Cookie].show(cookie) shouldEqual "hello=dolly; potatoes=22"
    }
  }

}
