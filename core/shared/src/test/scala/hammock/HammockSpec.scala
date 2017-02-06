package hammock

import free.algebra._
import org.scalatest._
import cats._
import hi.{Opts, Cookie}

class HammockSpec extends WordSpec with Matchers {
  val methods = Seq(
     Method.OPTIONS,
     Method.GET,
     Method.HEAD,
     Method.POST,
     Method.PUT,
     Method.DELETE,
     Method.TRACE)

  "Hammock.request" should {

    implicit val stringCodec = new Codec[String] {
      def encode(s: String) = s
      def decode(s: String) = Right(s)
    }

    /**
      * This is really dirty... but I cannot come up with a better solution right now...
      */
    def test(assertions: HttpRequestF[_] => Any) = new (HttpRequestF ~> Id) {
      def apply[A](h: HttpRequestF[A]): A = {
        assertions(h)

        null.asInstanceOf[A]
      }
    }

    methods.map { method =>
      s"create a valid $method request without a body" in {
        Hammock.request(method, "http://pepegar.com", Map()) foldMap test { r =>
          r.url shouldEqual "http://pepegar.com"
          r.headers shouldEqual Map()
        }
      }

      s"create a valid $method request with a body" in {
        val body = Some("body!!")
        Hammock.request(method, "http://pepegar.com", Map(), body) foldMap test { r =>
          r.url shouldEqual "http://pepegar.com"
          r.headers shouldEqual Map()
          r.body shouldEqual Some("body!!")
        }
      }
    }

    "work with the options variant" in {
      val opts = Opts(None, Map("header" -> "3"), Map("urlParam" -> "44"), Some(List(Cookie("thisisacookie", "thisisthevalue"))))

      Hammock.getWithOpts("http://pepegar.com", opts) foldMap test { r =>
        r.url shouldEqual "http://pepegar.com?urlParam=44"
        r.headers shouldEqual Map("header" -> "3", "Set-Cookie" -> "thisisacookie=thisisthevalue")
      }

    }
  }
}
