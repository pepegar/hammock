package hammock
package circe

import io.circe._
import io.circe.generic.auto._

import org.scalatest._

class CirceCodecSpec extends WordSpec with Matchers {
  case class Dummy(a: Int, b: String)
  import implicits._

  val dummyValue = Dummy(1, "patata")
  val json       = """{"a":1,"b":"patata"}"""

  "Codec.encode" should {
    "return the string representation of a type" in {
      Codec[Dummy].encode(dummyValue) shouldEqual json
    }
  }

  "Codec.decode" should {
    "parse a valid value" in {
      Codec[Dummy].decode(json) shouldEqual Right(dummyValue)
    }

    "fail to parse an invalid value" in {
      Codec[Dummy].decode("this is of course not valid") shouldBe a[Left[String, Dummy]]
    }
  }

}
