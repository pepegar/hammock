package hammock
package hi

import org.scalatest._

import monocle.syntax.all._

class DslSpec extends WordSpec with Matchers {

  import dsl._

  "hi.dsl" should {
    "allow concatenation of operations" in {
      val req = Opts.default &~> (
        auth(Auth.BasicAuth("pepegar", "h4rdp4ssw0rd")) andThen
        param("page" -> "43") andThen
        header("X-Forwarded-Proto" -> "https"))

      req shouldEqual Opts(Some(Auth.BasicAuth("pepegar", "h4rdp4ssw0rd")),Map("X-Forwarded-Proto" -> "https"), Map("page" -> "43"),None)
    }
  }

}
