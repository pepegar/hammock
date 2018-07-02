package hammock

import org.scalatest.{Matchers, WordSpec}

class StatusSpec extends WordSpec with Matchers {
  val status: Int => Status = (code: Int) => Status(code, "", "")

  "Status.isInformational" when {
    "status code is 1xx" should {
      "return true" in {
        (100 to 102).foreach(code => assert(status(code).isInformational))
      }
    }

    "status code is not 1xx" should {
      "return false" in {
        assert(!status(200).isInformational)
        assert(!status(300).isInformational)
        assert(!status(400).isInformational)
        assert(!status(500).isInformational)
      }
    }
  }

  "Status.isSuccess" when {
    "status code is 2xx" should {
      "return true" in {
        (200 to 208).foreach(code => assert(status(code).isSuccess))
      }
    }

    "status code is not 2xx" should {
      "return false" in {
        assert(!status(100).isSuccess)
        assert(!status(300).isSuccess)
        assert(!status(400).isSuccess)
        assert(!status(500).isSuccess)
      }
    }
  }

  "Status.isRedirection" when {
    "status code is 3xx" should {
      "return true" in {
        (300 to 308).foreach(code => assert(status(code).isRedirection))
      }
    }

    "status code is not 3xx" should {
      "return false" in {
        assert(!status(100).isRedirection)
        assert(!status(200).isRedirection)
        assert(!status(400).isRedirection)
        assert(!status(500).isRedirection)
      }
    }
  }

  "Status.isClientError" when {
    "status code is 4xx" should {
      "return true" in {
        (400 to 451).foreach(code => assert(status(code).isClientError))
      }
    }

    "status code is not 4xx" should {
      "return false" in {
        assert(!status(100).isClientError)
        assert(!status(200).isClientError)
        assert(!status(300).isClientError)
        assert(!status(500).isClientError)
      }
    }
  }

  "Status.isServerError" when {
    "status code is 5xx" should {
      "return true" in {
        (500 to 599).foreach(code => assert(status(code).isServerError))
      }
    }

    "status code is not 5xx" should {
      "return false" in {
        assert(!status(100).isServerError)
        assert(!status(200).isServerError)
        assert(!status(300).isServerError)
        assert(!status(400).isServerError)
      }
    }
  }
}
