package hammock

import hammock.Entity._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class EntitySpec extends AnyWordSpec with Matchers {

  "EmptyEntity" should {

    "valid instance" in {
      assert(EmptyEntity.contentLength == 0)
      assert(EmptyEntity.contentType == ContentType.notUsed)
      assert(!EmptyEntity.chunked)
      assert(EmptyEntity.repeatable)
      assert(!EmptyEntity.streaming)
    }
  }

  "StringEntity" should {

    "valid instance" in {
      val instance = StringEntity("body")
      assert(instance.contentLength == 4)
      assert(!EmptyEntity.chunked)
      assert(EmptyEntity.repeatable)
      assert(!EmptyEntity.streaming)
    }
  }

  "ByteArrayEntity" should {

    "valid instance" in {
      val instance = ByteArrayEntity("body".getBytes)
      assert(instance.contentLength == 4)
      assert(!EmptyEntity.chunked)
      assert(EmptyEntity.repeatable)
      assert(!EmptyEntity.streaming)
    }
  }

  "Eq[Entity]" should {

    "equal instance" in {
      val body        = "body".getBytes
      val instanceOne = ByteArrayEntity(body)
      val instanceTwo = ByteArrayEntity(body)
      assert(instanceOne == instanceTwo)
    }
  }
}
