package hammock
package akka

import _root_.akka.http.scaladsl.HttpExt
import _root_.akka.actor.ActorSystem
import _root_.akka.stream.ActorMaterializer
import _root_.akka.http.scaladsl.model.{HttpResponse => AkkaResponse}
import cats.effect.IO
import org.scalatest._
import cats._
import cats.free.Free
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import scala.concurrent.{ExecutionContext, Future}
import hammock.free.algebra._

class InterpreterSpec extends WordSpec with MockitoSugar with Matchers with BeforeAndAfter {
  implicit val system: ActorSystem    = ActorSystem("test")
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext   = ExecutionContext.Implicits.global

  val client = mock[HttpExt]
  val interp = new AkkaInterpreter[IO](client)

  after {
    reset(client)
  }

  "akka http interpreter" should {

    "create a correct AkkaResponse from akka's Http response" in {
      val hammockReq = Get(HttpRequest(Uri(path = "http://localhost:8080"), Map(), None))
      val akkaReq    = interp.transformRequest(hammockReq)
      when(client.singleRequest(akkaReq)).thenReturn(Future.successful(httpResponse))
      val result = (Free.liftF(hammockReq) foldMap interp.trans).unsafeRunSync

      result shouldEqual HttpResponse(Status.OK, Map(), Entity.StringEntity(""))
    }

  }

  private[this] def httpResponse: AkkaResponse =
    AkkaResponse()

}
