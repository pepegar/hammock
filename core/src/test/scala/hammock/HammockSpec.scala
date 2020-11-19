package hammock

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import cats._
import hi.{Auth, Cookie, Opts}

class HammockSpec extends AnyWordSpec with Matchers {
  val methods =
    Seq(Method.OPTIONS, Method.GET, Method.HEAD, Method.POST, Method.PUT, Method.DELETE, Method.TRACE, Method.PATCH)

  implicit val stringCodec = new Codec[String] {
    def decode(a: hammock.Entity): Either[hammock.CodecException, String] = a match {
      case Entity.StringEntity(str, _) => Right(str)
      case _                           => Left(CodecException.withMessage("expected string entity"))
    }
    def encode(a: String): hammock.Entity = Entity.StringEntity(a)
  }

  /**
   * This is really dirty... but I cannot come up with a better solution right now...
   */
  def test(assertions: HttpF[_] => Any) = new (HttpF ~> Id) {
    def apply[A](h: HttpF[A]): A = {
      assertions(h)

      null.asInstanceOf[A]
    }
  }

  val uri = Uri.unsafeParse("http://pepegar.com")

  "Hammock.request" should {

    methods.foreach { method =>
      s"create a valid $method request without a body" in {
        Hammock.request(method, uri, Map()) foldMap test { r =>
          r.req.uri shouldEqual Uri.unsafeParse("http://pepegar.com")
          r.req.headers shouldEqual Map()
        }
      }

      s"create a valid $method request with a body" in {
        val body = None
        Hammock.request(method, uri, Map(), body) foldMap test { r =>
          r.req.uri shouldEqual Uri.unsafeParse("http://pepegar.com")
          r.req.headers shouldEqual Map()
          r.req.entity shouldEqual None
        }
      }
    }

    "work with the options variant" in {
      val opts = Opts(None, Map("header" -> "3"), Some(List(Cookie("thisisacookie", "thisisthevalue"))))

      Uri.fromString("http://pepegar.com") match {
        case Right(uri) =>
          Hammock.getWithOpts(uri, opts) foldMap test { r =>
            r.req.uri shouldEqual Uri.unsafeParse("http://pepegar.com")
            r.req.headers shouldEqual Map("header" -> "3", "Cookie" -> "thisisacookie=thisisthevalue")
          }
        case Left(err) => fail(s"failed with $err")
      }
    }

    "construct the correct headers" in {
      val basicAuth = Auth.BasicAuth("user", "p4ssw0rd")
      val opts      = Opts(Option(basicAuth), Map.empty, Option.empty)
      val shown     = Show[Auth].show(basicAuth)

      Uri.fromString("http://pepegar.com") match {
        case Right(uri) =>
          Hammock.getWithOpts(uri, opts) foldMap test { r =>
            r.req.uri shouldEqual Uri.unsafeParse("http://pepegar.com")
            r.req.headers shouldEqual Map("Authorization" -> shown)
          }
        case Left(err) => fail(s"failed with $err")
      }
    }
  }
}
