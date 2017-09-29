package hammock

import free.algebra._
import org.scalatest._
import cats._
import cats.syntax.show._
import hi.{Cookie, Opts}

class HammockSpec extends WordSpec with Matchers {
  val methods = Seq(Method.OPTIONS, Method.GET, Method.HEAD, Method.POST, Method.PUT, Method.DELETE, Method.TRACE)

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

  val uri = Uri.fromString("http://pepegar.com").right.get

  "Hammock.request" should {

    methods.map { method =>
      s"create a valid $method request without a body" in {
        Hammock.request(method, uri, Map()) foldMap test { r =>
          r.uri shouldEqual Uri.fromString("http://pepegar.com").right.get
          r.headers shouldEqual Map()
        }
      }

      s"create a valid $method request with a body" in {
        val body = None
        Hammock.request(method, uri, Map(), body) foldMap test { r =>
          r.uri shouldEqual Uri.fromString("http://pepegar.com").right.get
          r.headers shouldEqual Map()
          r.body shouldEqual None
        }
      }
    }

    "work with the options variant" in {
      val opts = Opts(None, Map("header" -> "3"), Some(List(Cookie("thisisacookie", "thisisthevalue"))))

      Uri.fromString("http://pepegar.com") match {
        case Right(uri) =>
          Hammock.getWithOpts(uri, opts) foldMap test { r =>
            r.uri shouldEqual Uri.fromString("http://pepegar.com").right.get
            r.headers shouldEqual Map("header" -> "3", "Set-Cookie" -> "thisisacookie=thisisthevalue")
          }
        case Left(err) => fail(s"failed with $err")
      }
    }
  }
}
