package hammock
package hi

import org.scalatest._

class DslSpec extends WordSpec with Matchers {

  import dsl._

  "`cookies`" should {
    "work when there were no cookies before" in {
      val opts = cookies(List(Cookie("a", "b"), Cookie("c", "d")))(Opts.default)

      opts shouldEqual Opts(None, Map(), Some(List(Cookie("a", "b"), Cookie("c", "d"))))
    }

    "preppend cookies when there were cookies before" in {
      val opts = cookies(List(Cookie("c", "d"), Cookie("e", "f")))(Opts(None, Map(), Some(List(Cookie("a", "b")))))

      opts shouldEqual Opts(None, Map(), Some(List(Cookie("c", "d"), Cookie("e", "f"), Cookie("a", "b"))))
    }
  }

  "hi.dsl" should {
    "allow concatenation of operations" in {
      val req = (auth(Auth.BasicAuth("pepegar", "h4rdp4ssw0rd")) &>
        header("X-Forwarded-Proto" -> "https") &>
        cookie(Cookie("track", "A lot")))(Opts.default)

      req shouldEqual Opts(
        Some(Auth.BasicAuth("pepegar", "h4rdp4ssw0rd")),
        Map("X-Forwarded-Proto" -> "https"),
        Some(List(Cookie("track", "A lot"))))
    }
  }

}
