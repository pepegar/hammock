package hammock
package hi

import cats.implicits._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class DslSpec extends AnyWordSpec with Matchers {

  "`cookies`" should {
    "work when there were no cookies before" in {
      val opts = cookies(List(Cookie("a", "b"), Cookie("c", "d")))(Opts.empty)

      opts shouldEqual Opts(None, Map(), Some(List(Cookie("a", "b"), Cookie("c", "d"))))
    }

    "preppend cookies when there were cookies before" in {
      val opts = cookies(List(Cookie("c", "d"), Cookie("e", "f")))(Opts(None, Map(), Some(List(Cookie("a", "b")))))

      opts shouldEqual Opts(None, Map(), Some(List(Cookie("c", "d"), Cookie("e", "f"), Cookie("a", "b"))))
    }
  }

  "high level dsl" should {
    "allow concatenation of operations" in {
      val req = (auth(Auth.BasicAuth("pepegar", "h4rdp4ssw0rd")) >>>
        header("X-Forwarded-Proto" -> "https") >>>
        cookie(Cookie("track", "A lot")))(Opts.empty)

      req shouldEqual Opts(
        Some(Auth.BasicAuth("pepegar", "h4rdp4ssw0rd")),
        Map("X-Forwarded-Proto" -> "https"),
        Some(List(Cookie("track", "A lot"))))
    }
  }

}
