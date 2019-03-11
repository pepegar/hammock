package examplenode

import hammock._
import hammock.marshalling._
import hammock.circe.implicits._
import io.circe.generic.auto._
import cats.effect.IO
import scala.util.{Failure, Success}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import hammock.fetch.Interpreter._

object Main {
  def main(args: Array[String]): Unit = {

    val endpoint = uri"http://www.mocky.io/v2/5185415ba171ea3a00704eed"
    case class Req(name: String, number: Int)
    case class Resp(hello: String)
    val request = Hammock
      .request(Method.POST, endpoint, Map(), Some(Req("name", 4)))
      .as[Resp]
      .exec[IO]
    request.unsafeToFuture.onComplete {
      case Success(resp) => println("hello: " + resp)
      case Failure(ex)   => println(ex)
    }
  }
}
