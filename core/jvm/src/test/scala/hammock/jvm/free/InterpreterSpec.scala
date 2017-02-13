package hammock
package jvm
package free

import cats.data.Kleisli

import hammock.free._
import org.apache.http.ProtocolVersion
import org.apache.http.client.HttpClient
import org.apache.http.entity.StringEntity
import org.apache.http.{ HttpResponse => ApacheHttpResponse }
import org.apache.http.message.{ BasicHttpResponse, BasicStatusLine }
import org.scalatest._
import org.scalatest.mockito._
import org.mockito.{Matchers => MM, _}
import org.mockito.Mockito._
import scala.util.Try
import cats._
import cats.implicits._

class InterpreterSpec extends WordSpec with MockitoSugar with BeforeAndAfter {
  import HttpResponse._

  import algebra._
  import MM._

  val client = mock[HttpClient]
  val interp = new Interpreter(client)

  after {
    reset(client)
  }

  "Interpreter.trans" should {
    val methods = Seq(
      ("Options", (url: String, headers: Map[String, String]) => Ops.options(url, headers)),
      ("Get", (url: String, headers: Map[String, String]) => Ops.get(url, headers)),
      ("Head", (url: String, headers: Map[String, String]) => Ops.head(url, headers)),
      ("Post", (url: String, headers: Map[String, String]) => Ops.post(url, headers, None)),
      ("Put", (url: String, headers: Map[String, String]) => Ops.put(url, headers, None)),
      ("Delete", (url: String, headers: Map[String, String]) => Ops.delete(url, headers)),
      ("Trace", (url: String, headers: Map[String, String]) => Ops.trace(url, headers))
    ) map {
      case (method, operation)=>
      s"have the same result as transK.run(client) with $method requests" in {
        when(client.execute(any())).thenReturn(httpResponse)

        val op = operation("", Map())

        val k = op foldMap[Kleisli[Try, HttpClient, ?]] interp.transK[Try]

        val transkResult = k.run(client).get
        val transResult = (op foldMap interp.trans[Try]).get

        assert(Eq[HttpResponse].eqv(transkResult, transResult))
      }

    }

    "create a correct HttpResponse from Apache's HTTP response" in {
      when(client.execute(any())).thenReturn(httpResponse)

      val op = Ops.get("", Map())

      val k = op foldMap[Kleisli[Try, HttpClient, ?]] interp.transK[Try]

      val result = (op foldMap interp.trans[Try]).get

      assert(result.status == Status.OK)
      assert(result.headers == Map())
      assert(result.content == "content")
    }

  }

  private[this] def httpResponse: ApacheHttpResponse = {
    val resp = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, null))
    val entity = new StringEntity("content")

    resp.setEntity(entity)

    resp
  }

}
