package hammock

import org.scalatest._

import atto._
import Atto._
import atto.compat.cats._
import cats._
import cats.implicits._
import cats.syntax.either._

class UriSpec extends WordSpec with Matchers {

  "Uri.fromString" should {
    "parse uris with only a path" in {
      Uri.fromString("/test") shouldEqual Right(Uri(path = "/test"))
    }

    "parse uris with a scheme" in {
      Uri.fromString("http://pepegar.com") shouldEqual Right(Uri(scheme = Option("http"), path = "pepegar.com"))
    }

    "parse uris with an authority" in {
      Uri.fromString("http://user:pass@pepegar.com") shouldEqual Right(Uri(
        scheme = Option("http"),
        authority = Option("user:pass"),
        path = "pepegar.com"
      ))
    }

    "parse uris with query params" in {
      Uri.fromString("http://user:pass@pepegar.com?test=3&anotherTest=asdlfkj8") shouldEqual Right(Uri(
        scheme = Option("http"),
        authority = Option("user:pass"),
        path = "pepegar.com",
        query = Map("test" -> "3", "anotherTest" -> "asdlfkj8")
      ))
    }

    "parse uris with fragment" in {
      Uri.fromString("http://user:pass@pepegar.com?test=3&anotherTest=asdlfkj8#122") shouldEqual Right(Uri(
        scheme = Option("http"),
        authority = Option("user:pass"),
        path = "pepegar.com",
        query = Map("test" -> "3", "anotherTest" -> "asdlfkj8"),
        fragment = Some("122")
      ))
    }


  }
}
