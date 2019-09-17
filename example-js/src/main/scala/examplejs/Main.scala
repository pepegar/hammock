package examplejs

import scala.scalajs.js.annotation.JSExportTopLevel
import scala.util._
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalajs.jquery.jQuery
import cats.effect._
import hammock._
import hammock.marshalling._
import hammock.js._
import hammock.circe.implicits._
import io.circe.generic.auto._
import Interpreter._

object Main {
  implicit val cs = IO.contextShift(global)

  case class Resp(headers: Map[String, String], origin: String, url: String)
  case class Req(name: String, number: Int)

  @JSExportTopLevel("examplejs.Main.main")
  def main(): Unit = {

    val uri = uri"http://httpbin.org/post"

    val request: IO[Resp] = Hammock
      .request(Method.POST, uri, Map(), Some(Req("name", 4)))
      .as[Resp]
      .exec[IO]

    request.unsafeToFuture.onComplete {
      case Success(resp) =>
        jQuery("#result").append(s"""
<h3>Req you sent to the server:</h3>
<pre>$resp</pre>
""")
      case Failure(ex) => ex.printStackTrace()
    }
  }
}
