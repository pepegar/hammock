package hammock
package hi

import org.scalatest.{ Matchers, WordSpec }
import java.util.Date
import cats._


class CookieSpec extends WordSpec with Matchers {

  "Show[Cookie].show" should {
    "render a simple cookie in the correct format" in {
      val cookie = Cookie("name", "value")

      Show[Cookie].show(cookie) shouldEqual "name=value"
    }

    "render a complex cookie in the correct format" in {
      val cookie = Cookie("name", "value", Some(new Date(234234234)), Some(123), Some("pepegar.com"), Some("/blog"), Some(false), Some(true), Some(SameSite.Strict))

      Show[Cookie].show(cookie) shouldEqual "name=value; Expires=Sat, 03 Jan 1970 17:03:54 +0000; MaxAge=123; Domain=pepegar.com; Path=/blog; Secure=false; HttpOnly=true; SameSite=Strict"
    }

    "render a cookie with custom values in the correct format" in {
      val cookie = Cookie("hello", "dolly", custom = Some(Map("potatoes" -> "22")))

      Show[Cookie].show(cookie) shouldEqual "hello=dolly; potatoes=22"
    }
  }

}
