package hammock
package akka

import _root_.akka.actor.ActorSystem
import _root_.akka.http.scaladsl.HttpExt
import _root_.akka.http.scaladsl.model.headers.RawHeader
import _root_.akka.http.scaladsl.model.{
  ContentTypes,
  HttpEntity,
  HttpMethods,
  HttpRequest => AkkaRequest,
  HttpResponse => AkkaResponse,
  Uri => AkkaUri
}
import _root_.akka.stream.ActorMaterializer
import _root_.akka.util.ByteString
import cats.effect.IO
import cats.free.Free
import hammock.akka.AkkaInterpreter._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import cats.effect.unsafe.implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AkkaInterpreterSpec extends AnyWordSpec with MockitoSugar with Matchers with BeforeAndAfter {

  implicit val system: ActorSystem    = ActorSystem("test")
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext   = ExecutionContext.Implicits.global
  implicit val client: HttpExt        = mock[HttpExt]

  after {
    reset(client)
  }

  "akka http interpreter" should {

    "create correct Akka request objects with a ByteArrayentity" in {
      val hammockReq =
        Post(
          HttpRequest(
            Uri(path = "http://localhost:8080"),
            Map.empty[String, String],
            Some(Entity.ByteArrayEntity(Array[Byte]()))))

      val akkaReq = AkkaRequest(method = HttpMethods.POST, uri = AkkaUri("http://localhost:8080"))
        .withEntity(HttpEntity.Strict(ContentTypes.`application/octet-stream`, ByteString(Array[Byte]())))

      mapRequest[IO](hammockReq).unsafeRunSync() shouldEqual akkaReq

    }

    "create correct Akka request objects with a StringEntity" in {
      val hammockReq =
        Post(
          HttpRequest(
            Uri(path = "http://localhost:8080"),
            Map.empty[String, String],
            Some(Entity.StringEntity("potato"))))

      val akkaReq = AkkaRequest(method = HttpMethods.POST, uri = AkkaUri("http://localhost:8080"))
        .withEntity(HttpEntity.Strict(ContentTypes.`application/json`, ByteString.fromString("potato")))

      mapRequest[IO](hammockReq).unsafeRunSync() shouldEqual akkaReq

    }

    "create correct Akka request objects without body" in {
      val hammockReq = Post(HttpRequest(Uri(path = "http://localhost:8080"), Map.empty[String, String], None))

      val akkaReq = AkkaRequest(method = HttpMethods.POST, uri = AkkaUri("http://localhost:8080"))

      mapRequest[IO](hammockReq).unsafeRunSync() shouldEqual akkaReq

    }

    "create correct Akka HTTP request from Hammock's" in {
      val hammockReq = Post(
        HttpRequest(
          Uri(path = "http://localhost:8080"),
          Map(
            "header1" -> "value1",
            "header2" -> "value2"
          ),
          Some(Entity.ByteArrayEntity(Array[Byte]()))))

      val akkaReq = AkkaRequest(method = HttpMethods.POST, uri = AkkaUri("http://localhost:8080"))
        .withHeaders(
          List(
            RawHeader("header1", "value1"),
            RawHeader("header2", "value2")
          ))
        .withEntity(HttpEntity.Strict(ContentTypes.`application/octet-stream`, ByteString(Array[Byte]())))

      mapRequest[IO](hammockReq).unsafeRunSync() shouldEqual akkaReq
    }

    "create a correct HttpResponse from akka's Http response without body" in {
      val hammockReq = Get(
        HttpRequest(
          Uri(path = "http://localhost:8080"),
          Map(
            "header1" -> "value1",
            "header2" -> "value2"
          ),
          None))
      val akkaReq = AkkaRequest(uri = AkkaUri("http://localhost:8080")).withHeaders(
        List(
          RawHeader("header1", "value1"),
          RawHeader("header2", "value2")
        ))
      when(client.singleRequest(akkaReq)).thenReturn(Future.successful(httpResponse))
      val result = (Free.liftF(hammockReq) foldMap AkkaInterpreter[IO].trans).unsafeRunSync()

      result shouldEqual HttpResponse(Status.OK, Map(), Entity.StringEntity(""))
    }

    "create a correct HttpResponse from akka's Http response with StringEntity" in {
      val hammockReq = Get(
        HttpRequest(
          Uri(path = "http://localhost:8080"),
          Map(
            "header1" -> "value1",
            "header2" -> "value2"
          ),
          Some(Entity.StringEntity("potato"))))
      val akkaReq = AkkaRequest(uri = AkkaUri("http://localhost:8080"))
        .withHeaders(
          List(
            RawHeader("header1", "value1"),
            RawHeader("header2", "value2")
          ))
        .withEntity(HttpEntity.Strict(ContentTypes.`application/json`, ByteString.fromString("potato")))
      when(client.singleRequest(akkaReq)).thenReturn(Future.successful(httpResponse))
      val result = (Free.liftF(hammockReq) foldMap AkkaInterpreter[IO].trans).unsafeRunSync()

      result shouldEqual HttpResponse(Status.OK, Map(), Entity.StringEntity(""))
    }

    "create a correct HttpResponse from akka's Http response with ByteArrayEntity" in {
      val hammockReq = Get(
        HttpRequest(
          Uri(path = "http://localhost:8080"),
          Map(
            "header1" -> "value1",
            "header2" -> "value2"
          ),
          Some(Entity.ByteArrayEntity(Array[Byte](11, 12, 13, 14)))))
      val akkaReq = AkkaRequest(uri = AkkaUri("http://localhost:8080"))
        .withHeaders(
          List(
            RawHeader("header1", "value1"),
            RawHeader("header2", "value2")
          ))
        .withEntity(HttpEntity.Strict(ContentTypes.`application/octet-stream`, ByteString(Array[Byte](11, 12, 13, 14))))
      when(client.singleRequest(akkaReq)).thenReturn(Future.successful(httpResponse))
      val result = (Free.liftF(hammockReq) foldMap AkkaInterpreter[IO].trans).unsafeRunSync()

      result shouldEqual HttpResponse(Status.OK, Map(), Entity.StringEntity(""))
    }

  }

  private[this] def httpResponse: AkkaResponse =
    AkkaResponse()

}
