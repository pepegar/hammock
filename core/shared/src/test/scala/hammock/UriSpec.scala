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
      Uri.fromString("http://user:pass@pepegar.com") shouldEqual Right(
        Uri(
          scheme = Option("http"),
          authority = Option("user:pass"),
          path = "pepegar.com"
        ))
    }

    "parse uris with query params" in {
      Uri.fromString("http://user:pass@pepegar.com?test=3&anotherTest=asdlfkj8") shouldEqual Right(
        Uri(
          scheme = Option("http"),
          authority = Option("user:pass"),
          path = "pepegar.com",
          query = Map("test" -> "3", "anotherTest" -> "asdlfkj8")
        ))
    }

    "parse uris with fragment" in {
      Uri.fromString("http://user:pass@pepegar.com?test=3&anotherTest=asdlfkj8#122") shouldEqual Right(
        Uri(
          scheme = Option("http"),
          authority = Option("user:pass"),
          path = "pepegar.com",
          query = Map("test" -> "3", "anotherTest" -> "asdlfkj8"),
          fragment = Some("122")
        ))
    }
  }

  "Uri.toString" should {
    "create valid URI when only the path is present" in {
      Uri(path = "/asdf").show shouldEqual "/asdf"
    }

    "create valid URI when there is a scheme" in {
      Uri(scheme = "http".some, path = "potato.com").show shouldEqual "http://potato.com"
    }

    "create valid URI when there is an authority" in {
      Uri(authority = "user:pass".some, scheme = "ftp".some, path = "patata.com").show shouldEqual "ftp://user:pass@patata.com"
    }

    "create valid URI when therer are query params" in {
      Uri(
        authority = "user:pass".some,
        scheme = "ftp".some,
        path = "patata.com",
        query = Map("page" -> "33", "sauce" -> "bbq")).show shouldEqual "ftp://user:pass@patata.com?page=33&sauce=bbq"
    }

    "create valid URI when there is a fragment" in {
      Uri(
        authority = "user:pass".some,
        scheme = "ftp".some,
        path = "patata.com",
        query = Map("page" -> "33", "sauce" -> "bbq"),
        fragment = "index".some).show shouldEqual "ftp://user:pass@patata.com?page=33&sauce=bbq#index"
    }
  }
}
