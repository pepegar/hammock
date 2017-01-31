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
      ("Options", Ops.options _),
      ("Get", Ops.get _),
      ("Head", Ops.head _),
      ("Post", Ops.post _),
      ("Put", Ops.put _),
      ("Delete", Ops.delete _),
      ("Trace", Ops.trace _)
    ) map {
      case (method, operation)=>
      s"have the same result as transK.run(client) with $method requests" in {
        when(client.execute(any())).thenReturn(httpResponse)

        val op = operation("", Map(), None)

        val k = op foldMap[Kleisli[Try, HttpClient, ?]] interp.transK[Try]

        assert(Eq[HttpResponse].eqv(k.run(client).get, (op foldMap interp.trans[Try]).get))
      }

    }

  }

  private[this] def httpResponse: ApacheHttpResponse = {
    val resp = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, null))
    val entity = new StringEntity("")

    resp.setEntity(entity)

    resp
  }

}
