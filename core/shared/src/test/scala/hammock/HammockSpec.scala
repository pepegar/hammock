package hammock


import org.scalatest._
import cats._
import hi.{Cookie, Opts}

class HammockSpec extends WordSpec with Matchers {
  val methods = Seq(Method.OPTIONS, Method.GET, Method.HEAD, Method.POST, Method.PUT, Method.DELETE, Method.TRACE, Method.PATCH)

  implicit val stringCodec = new Codec[String] {
    def decode(a: hammock.Entity): Either[hammock.CodecException,String] = a match {
      case Entity.StringEntity(str, _) => Right(str)
      case _ => Left(CodecException.withMessage("expected string entity"))
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

  val uri = Uri.fromString("http://pepegar.com").right.get

  "Hammock.request" should {

    methods.map { method =>
      s"create a valid $method request without a body" in {
        Hammock.request(method, uri, Map()) foldMap test { r =>
          r.req.uri shouldEqual Uri.fromString("http://pepegar.com").right.get
          r.req.headers shouldEqual Map()
        }
      }

      s"create a valid $method request with a body" in {
        val body = None
        Hammock.request(method, uri, Map(), body) foldMap test { r =>
          r.req.uri shouldEqual Uri.fromString("http://pepegar.com").right.get
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
            r.req.uri shouldEqual Uri.fromString("http://pepegar.com").right.get
            r.req.headers shouldEqual Map("header" -> "3", "Set-Cookie" -> "thisisacookie=thisisthevalue")
          }
        case Left(err) => fail(s"failed with $err")
      }
    }
  }
}
