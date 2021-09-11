package hammock
package apache

import java.net.URI
import cats._
import cats.data.Kleisli
import cats.effect._
import cats.effect.unsafe.implicits.global
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.entity.StringEntity
import org.apache.http.message.{BasicHttpResponse, BasicStatusLine}
import org.apache.http.{ProtocolVersion, HttpResponse => ApacheHttpResponse}
import org.mockito.Mockito._
import org.mockito.{Matchers => MM}
import org.scalatest.BeforeAndAfter
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito._
import ApacheInterpreter._

class ApacheInterpreterSpec extends AnyWordSpec with MockitoSugar with BeforeAndAfter {
  import MM._

  implicit val client: HttpClient = mock[HttpClient]
  val httpResponse: ApacheHttpResponse = {
    val resp   = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, null))
    val entity = new StringEntity("content")
    resp.setEntity(entity)
    resp
  }

  after {
    reset(client)
  }

  "Interpreter.trans" should {
    Seq(
      ("Options", (uri: Uri, headers: Map[String, String]) => Ops.options(uri, headers)),
      ("Get", (uri: Uri, headers: Map[String, String]) => Ops.get(uri, headers)),
      ("Head", (uri: Uri, headers: Map[String, String]) => Ops.head(uri, headers)),
      ("Post", (uri: Uri, headers: Map[String, String]) => Ops.post(uri, headers, None)),
      ("Put", (uri: Uri, headers: Map[String, String]) => Ops.put(uri, headers, None)),
      ("Delete", (uri: Uri, headers: Map[String, String]) => Ops.delete(uri, headers)),
      ("Trace", (uri: Uri, headers: Map[String, String]) => Ops.trace(uri, headers)),
      ("Patch", (uri: Uri, headers: Map[String, String]) => Ops.patch(uri, headers, None))
    ) foreach {
      case (method, operation) =>
        s"have the same result as transK.run(client) with $method requests" in {
          when(client.execute(any[HttpUriRequest])).thenReturn(httpResponse)

          val op           = operation(Uri(), Map())
          val k            = op.foldMap[Kleisli[IO, HttpClient, *]](transK)
          val transKResult = k.run(client).unsafeRunSync()
          val transResult  = (op foldMap ApacheInterpreter[IO].trans).unsafeRunSync()

          assert(Eq[HttpResponse].eqv(transKResult, transResult))
        }
    }

    "create a correct Apache's HTTP request from HttpF" in {
      val req = Get(
        HttpRequest(
          uri"http://localhost:8080",
          Map(
            "header1" -> "value1",
            "header2" -> "value2"
          ),
          None))

      val apacheReq = mapRequest[IO](req).unsafeRunSync()
      assert(apacheReq.getURI == new URI("http://localhost:8080"))
      assert(apacheReq.getAllHeaders.length == 2)
      assert(
        apacheReq
          .getHeaders("header1")(0)
          .getValue == "value1")
      assert(
        apacheReq
          .getHeaders("header2")(0)
          .getValue == "value2")
    }

    "create a correct HttpResponse from Apache's HTTP response" in {
      when(client.execute(any[HttpUriRequest])).thenReturn(httpResponse)

      val op     = Ops.get(Uri(), Map())
      val result = (op foldMap ApacheInterpreter[IO].trans).unsafeRunSync()

      assert(result.status == Status.OK)
      assert(result.headers == Map())
      assert(result.entity.content == "content")
    }

    "create a correct response when Apache's HttpResponse.getEntity is null" in {
      val resp = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 204, null))
      when(client.execute(any[HttpUriRequest])).thenReturn(resp)

      val op     = Ops.get(Uri(), Map())
      val result = (op foldMap ApacheInterpreter[IO].trans).unsafeRunSync()

      assert(result.status == Status.NoContent)
      assert(result.entity == Entity.EmptyEntity)
    }
  }

}
