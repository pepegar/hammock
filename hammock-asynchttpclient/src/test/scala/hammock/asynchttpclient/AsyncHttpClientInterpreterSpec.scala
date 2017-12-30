package hammock
package asynchttpclient

import cats.implicits._
import cats.effect._
import org.scalatest._
import org.asynchttpclient._
import io.netty.handler.codec.http.{DefaultHttpHeaders, HttpHeaders}
import io.netty.handler.codec.http.cookie.Cookie

import scala.collection.JavaConverters._

class AsyncHttpClientInterpreterTest extends WordSpec with Matchers {

  val interpreter = new AsyncHttpClientInterpreter[IO]

  "asynchttpclient" should {

    "map requests correctly" in {
      val hreq1 = Get(HttpRequest(uri"http://google.com", Map.empty[String, String], None))
      val req1  = interpreter.mapRequest(hreq1).unsafeRunSync.build()

      val hreq2 = Post(HttpRequest(uri"http://google.com", Map("header1" -> "value1"), None))
      val req2  = interpreter.mapRequest(hreq2).unsafeRunSync.build()

      val hreq3 = Put(
        HttpRequest(
          uri"http://google.com",
          Map("header1" -> "value1", "header2" -> "value2"),
          Some(Entity.StringEntity("the body"))))
      val req3 = interpreter.mapRequest(hreq3).unsafeRunSync.build()

      req1.getUrl shouldEqual hreq1.req.uri.show
      req1.getMethod shouldEqual "GET"
      req1.getHeaders shouldBe empty

      req2.getUrl shouldEqual hreq2.req.uri.show
      req2.getMethod shouldEqual "POST"
      req2.getHeaders.asScala.size shouldEqual hreq2.req.headers.size
      req2.getHeaders.asScala.find(_.getKey == "header1").map(_.getValue) shouldEqual Some("value1")

      req3.getUrl shouldEqual hreq3.req.uri.show
      req3.getMethod shouldEqual "PUT"
      req3.getHeaders.asScala.size shouldEqual hreq3.req.headers.size
      req3.getHeaders.asScala.find(_.getKey == "header1").map(_.getValue) shouldEqual Some("value1")
      req3.getHeaders.asScala.find(_.getKey == "header2").map(_.getValue) shouldEqual Some("value2")
      req3.getStringData shouldEqual "the body"
    }

    "map responses correctly" in {

      def genAHCResponse(contentType: String, headers: HttpHeaders, body: String, statusCode: Int) = new Response {
        def getContentType(): String                               = contentType
        def getCookies(): java.util.List[Cookie]                   = ???
        def getHeader(x$1: CharSequence): String                   = ???
        def getHeaders(): HttpHeaders                              = headers
        def getHeaders(x$1: CharSequence): java.util.List[String]  = ???
        def getLocalAddress(): java.net.SocketAddress              = ???
        def getRemoteAddress(): java.net.SocketAddress             = ???
        def getResponseBody(): String                              = body
        def getResponseBody(x$1: java.nio.charset.Charset): String = ???
        def getResponseBodyAsByteBuffer(): java.nio.ByteBuffer     = ???
        def getResponseBodyAsBytes(): Array[Byte]                  = ???
        def getResponseBodyAsStream(): java.io.InputStream         = ???
        def getStatusCode(): Int                                   = statusCode
        def getStatusText(): String                                = ???
        def getUri(): org.asynchttpclient.uri.Uri                  = ???
        def hasResponseBody(): Boolean                             = true
        def hasResponseHeaders(): Boolean                          = ???
        def hasResponseStatus(): Boolean                           = ???
        def isRedirected(): Boolean                                = ???
      }

      val ahcResponse1 =
        genAHCResponse("text/plain", new DefaultHttpHeaders().add("header", "value"), "this is the body", 200)
      val hammockResponse1 = HttpResponse(Status.OK, Map("header" -> "value"), Entity.StringEntity("this is the body"))
      val ahcResponse2 =
        genAHCResponse(
          "application/json",
          new DefaultHttpHeaders().add("Content-type", "application/json"),
          "[1,2,3,4]",
          200)
      val hammockResponse2 =
        HttpResponse(Status.OK, Map("Content-type" -> "application/json"), Entity.StringEntity("[1,2,3,4]"))
      val ahcResponse3 =
        genAHCResponse(
          "application/octet-stream",
          new DefaultHttpHeaders().add("Content-type", "application/octet-stream"),
          "[1,2,3,4]",
          200)
      val hammockResponse3 =
        HttpResponse(
          Status.OK,
          Map("Content-type" -> "application/octet-stream"),
          Entity.ByteArrayEntity("[1,2,3,4]".toCharArray.map(_.toByte)))

      val tests = List(
        ahcResponse1 -> hammockResponse1,
        ahcResponse2 -> hammockResponse2,
        ahcResponse3 -> hammockResponse3
      )

      tests foreach {
        case (a, h) =>
          (
            interpreter
              .mapResponse(a)
              .unsafeRunSync,
            h) match {
            case (HttpResponse(s1, h1, e1), HttpResponse(s2, h2, e2)) =>
              s1 shouldEqual s2
              h1 shouldEqual h2
              e1.cata(showStr, showByt) shouldEqual e2.cata(showStr, showByt)
          }
      }
    }
  }

  def showStr(s: Entity.StringEntity)    = s.content
  def showByt(b: Entity.ByteArrayEntity) = b.content.mkString("[", ",", "]")
}
