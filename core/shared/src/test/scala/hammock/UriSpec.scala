package hammock

import org.scalatest._
import cats.implicits._
import atto._
import Atto._

class UriSpec extends WordSpec with Matchers {
  import Uri._

  val ipv4 = "192.168.1.1"
  val ipv6 = "[2001:0db8:0000:0042:0000:8a2e:0370:7334]"
  def others = "google.com" :: "test" :: "test.google.com.asdf.qwer" :: Nil
  val users = "pepe" :: "pepe:pass" :: Nil
  val ports: List[Long] = List(1L, 65536L)

  def group(str: String): Host.IPv6Group =
    Host.IPv6Group(str.sliding(2, 2).toVector.map(Integer.parseInt(_, 16).toByte))

  val ipv6Host = Host.IPv6(group("2001"), group("0db8"), group("0000"), group("0042"), group("0000"), group("8a2e"), group("0370"), group("7334"))

  val `potato.com` = Authority(None, Host.Other("potato.com"), None)
  val `user:pass@potato.com:443` = Authority("user:pass".some, Host.Other("potato.com"), 443L.some)
  val `user:pass@potato.com` = Authority("user:pass".some, Host.Other("potato.com"), None)

  "Host" should {
    "parse ipv4 correctly" in {
      Host.parseHost.parseOnly(ipv4).either.right.get shouldEqual Host.IPv4(192, 168, 1,1)
    }

    "parse ipv6 correctly" in {
      Host.parseHost.parseOnly(ipv6).either.right.get shouldEqual ipv6Host
    }

    "parse localhost correctly" in {
      Host.parseHost.parseOnly("localhost").either.right.get shouldEqual Host.Localhost
    }

    "parse others correctly" in {
      others foreach { t =>
        Host.parseHost.parseOnly(t).either.right.get shouldEqual Host.Other(t)
      }
    }
  }

  "Authority" should {
    "parse authorities without user nor port correctly" in {
      (Authority.authorityParser.parseOnly(ipv6).either.right.get shouldEqual Authority(None, ipv6Host, None))
      (Authority.authorityParser.parseOnly(ipv4).either.right.get shouldEqual Authority(None, Host.IPv4(192, 168, 1,1), None))
      (Authority.authorityParser.parseOnly("localhost").either.right.get shouldEqual Authority(None, Host.Localhost, None))

      others foreach { t =>
        Authority.authorityParser.parseOnly(t).either.right.get shouldEqual Authority(None, Host.Other(t), None)
      }
    }

    "parse authorities with an user" in {

      users foreach { user =>
        (Authority.authorityParser.parseOnly(s"$user@$ipv6").either.right.get shouldEqual Authority(user.some, ipv6Host, none))
        (Authority.authorityParser.parseOnly(s"$user@$ipv4").either.right.get shouldEqual Authority(user.some, Host.IPv4(192, 168, 1,1), None))
        (Authority.authorityParser.parseOnly(s"$user@localhost").either.right.get shouldEqual Authority(user.some, Host.Localhost, None))

        others foreach { t =>
          Authority.authorityParser.parseOnly(s"$user@$t").either.right.get shouldEqual Authority(user.some, Host.Other(t), None)
        }
      }
    }

    "parse authorities with an user and a port" in {
      ports foreach { port =>
        users foreach { user =>
          (Authority.authorityParser.parseOnly(s"$user@$ipv6:$port").either.right.get shouldEqual Authority(user.some, ipv6Host, port.some))
          (Authority.authorityParser.parseOnly(s"$user@$ipv4:$port").either.right.get shouldEqual Authority(user.some, Host.IPv4(192, 168, 1,1), port.some))
          (Authority.authorityParser.parseOnly(s"$user@localhost:$port").either.right.get shouldEqual Authority(user.some, Host.Localhost, port.some))

          others foreach { t =>
            Authority.authorityParser.parseOnly(s"$user@$t:$port").either.right.get shouldEqual Authority(user.some, Host.Other(t), port.some)
          }
        }
      }
    }
  }


  "Uri.fromString" should {
    "parse uris with only a path" in {
      Uri.fromString("/test") shouldEqual Right(Uri(path = "/test"))
    }

    "parse uris with a scheme" in {
      Uri.fromString("http://potato.com") shouldEqual Right(Uri(scheme = Option("http"), authority = `potato.com`.some, path = ""))
    }

    "parse uris with an authority" in {
      Uri.fromString("http://user:pass@potato.com") shouldEqual Right(
        Uri(
          scheme = "http".some,
          authority = `user:pass@potato.com`.some,
          path = ""
        ))
    }

    "parse uris with query params" in {
      Uri.fromString("http://user:pass@potato.com?test=3&anotherTest=asdlfkj8") shouldEqual Right(
        Uri(
          scheme = Option("http"),
          authority = `user:pass@potato.com`.some,
          path = "",
          query = Map("test" -> "3", "anotherTest" -> "asdlfkj8")
        ))
    }

    "parse uris with fragment" in {
      Uri.fromString("http://user:pass@potato.com:443?test=3&anotherTest=asdlfkj8#122") shouldEqual Right(
        Uri(
          scheme = Option("http"),
          authority = `user:pass@potato.com:443`.some,
          path = "",
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
      Uri(scheme = "http".some, authority = `potato.com`.some, path = "").show shouldEqual "http://potato.com"
    }

    "create valid URI when there is an authority" in {
      Uri(authority = `user:pass@potato.com`.some, scheme = "ftp".some, path = "").show shouldEqual "ftp://user:pass@potato.com"
    }

    "create valid URI when therer are query params" in {
      Uri(
        authority = `user:pass@potato.com`.some,
        scheme = "ftp".some,
        path = "",
        query = Map("page" -> "33", "sauce" -> "bbq")).show shouldEqual "ftp://user:pass@potato.com?page=33&sauce=bbq"
    }

    "create valid URI when there is a fragment" in {
      Uri(
        authority = `user:pass@potato.com`.some,
        scheme = "ftp".some,
        path = "",
        query = Map("page" -> "33", "sauce" -> "bbq"),
        fragment = "index".some).show shouldEqual "ftp://user:pass@potato.com?page=33&sauce=bbq#index"
    }
  }

  "Uri" should {
    "/ method appends to the path" in {
      Uri(path = "") / "segment" shouldEqual Uri(path = "/segment")
      Uri(path = "/nonemptypath") / "segment" shouldEqual Uri(path = "/nonemptypath/segment")
    }
  }

}
