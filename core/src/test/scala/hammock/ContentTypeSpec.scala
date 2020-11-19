package hammock

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ContentTypeSpec extends AnyWordSpec with Matchers {

  "Eq[ContentType]" should {

    "equal ContentType instances" in {
      val applicationJsonOne = ContentType.`application/json`
      val applicationJsonTwo = ContentType.`application/json`

      assert(applicationJsonOne == applicationJsonTwo)
    }

    "not equal ContentType instances" in {
      val applicationJson = ContentType.`application/json`
      val textPlain       = ContentType.`text/plain`

      assert(applicationJson != textPlain)
    }

    "equal ContentType from string instances" in {
      val applicationJsonOne = ContentType.fromName("application/json")
      val applicationJsonTwo = ContentType.fromName("application/json")

      applicationJsonOne.name shouldEqual applicationJsonTwo.name
    }
  }
}
