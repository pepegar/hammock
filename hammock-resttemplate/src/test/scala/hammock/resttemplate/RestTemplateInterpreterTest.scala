package hammock
package resttemplate

import cats.implicits._
import cats.effect._
import cats.effect.unsafe.implicits.global
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito._
import RestTemplateInterpreter._
import org.springframework.http.{HttpHeaders, HttpStatus, ResponseEntity}
import org.springframework.web.client.RestTemplate
import scala.jdk.CollectionConverters._

class RestTemplateInterpreterTest extends AnyWordSpec with Matchers with MockitoSugar {

  implicit val client: RestTemplate = new RestTemplate()

  "asynchttpclient" should {

    "map requests correctly" in {
      val hreq1 = Get(HttpRequest(uri"http://google.com", Map.empty[String, String], None))
      val req1  = mapRequest[IO](hreq1).unsafeRunSync()

      val hreq2 = Post(HttpRequest(uri"http://google.com", Map("header1" -> "value1"), None))
      val req2  = mapRequest[IO](hreq2).unsafeRunSync()

      val hreq3 = Put(
        HttpRequest(
          uri"http://google.com",
          Map("header1" -> "value1", "header2" -> "value2"),
          Some(Entity.StringEntity("the body"))
        )
      )
      val req3 = mapRequest[IO](hreq3).unsafeRunSync()

      req1.getUrl.toString shouldEqual hreq1.req.uri.show
      req1.getMethod.name shouldEqual "GET"
      req1.getHeaders.toSingleValueMap.asScala shouldBe empty

      req2.getUrl.toString shouldEqual hreq2.req.uri.show
      req2.getMethod.name shouldEqual "POST"
      req2.getHeaders.toSingleValueMap.asScala.size shouldEqual hreq2.req.headers.size
      req2.getHeaders.toSingleValueMap.asScala
        .find { case (key, _) => key == "header1" }
        .map { case (_, value) => value } shouldEqual Some("value1")

      req3.getUrl.toString shouldEqual hreq3.req.uri.show
      req3.getMethod.name shouldEqual "PUT"
      req3.getHeaders.toSingleValueMap.asScala.size shouldEqual hreq3.req.headers.size
      req3.getHeaders.toSingleValueMap.asScala
        .find { case (key, _) => key == "header1" }
        .map { case (_, value) => value } shouldEqual Some("value1")
      req3.getHeaders.toSingleValueMap.asScala
        .find { case (key, _) => key == "header2" }
        .map { case (_, value) => value } shouldEqual Some("value2")
      req3.getBody shouldEqual "the body"
    }

    "map responses correctly" in {

      def httpHeaders(values: List[(String, String)]) = {
        val httpHeaders = new HttpHeaders()
        values.foreach { case (key, value) => httpHeaders.add(key, value) }
        httpHeaders
      }

      val rtResponse1 =
        new ResponseEntity[String]("this is the body", httpHeaders(List("header" -> "value")), HttpStatus.OK)
      val hammockResponse1 = HttpResponse(Status.OK, Map("header" -> "value"), Entity.StringEntity("this is the body"))

      val rtResponse2 =
        new ResponseEntity[String]("[1,2,3,4]", httpHeaders(List("Content-type" -> "application/json")), HttpStatus.OK)
      val hammockResponse2 =
        HttpResponse(Status.OK, Map("Content-type" -> "application/json"), Entity.StringEntity("[1,2,3,4]"))

      val rtResponse3 = new ResponseEntity[String](
        "[1,2,3,4]",
        httpHeaders(List("Content-type" -> "application/octet-stream")),
        HttpStatus.OK
      )
      val hammockResponse3 =
        HttpResponse(
          Status.OK,
          Map("Content-type" -> "application/octet-stream"),
          Entity.ByteArrayEntity("[1,2,3,4]".toCharArray.map(_.toByte))
        )

      val tests = List(
        rtResponse1 -> hammockResponse1,
        rtResponse2 -> hammockResponse2,
        rtResponse3 -> hammockResponse3
      )

      tests foreach {
        case (a, h) =>
          (mapResponse[IO](a).unsafeRunSync(), h) match {
            case (HttpResponse(s1, h1, e1), HttpResponse(s2, h2, e2)) =>
              s1 shouldEqual s2
              h1 shouldEqual h2
              e1.cata(showStr, showByt, showEmpty) shouldEqual e2.cata(showStr, showByt, showEmpty)
          }
      }
    }
  }

  def showStr(s: Entity.StringEntity)    = s.content
  def showByt(b: Entity.ByteArrayEntity) = b.content.mkString("[", ",", "]")
  val showEmpty                          = (_: Entity.EmptyEntity.type) => ""
}
