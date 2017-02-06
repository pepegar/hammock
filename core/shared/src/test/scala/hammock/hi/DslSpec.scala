package hammock
package hi

import org.scalatest._

import monocle.syntax.all._

class DslSpec extends WordSpec with Matchers {

  import dsl._

  "hi.dsl" should {
    "allow concatenation of operations" in {
      val req = (
        auth(Auth.BasicAuth("pepegar", "h4rdp4ssw0rd")) &>
        param("page" -> "43") &>
        header("X-Forwarded-Proto" -> "https"))(Opts.default)

      req shouldEqual Opts(Some(Auth.BasicAuth("pepegar", "h4rdp4ssw0rd")),Map("X-Forwarded-Proto" -> "https"), Map("page" -> "43"),None)
    }
  }

}
