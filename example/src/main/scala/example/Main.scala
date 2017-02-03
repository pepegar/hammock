import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.Future

import cats.implicits._

import hammock._
import hammock.free._
import hammock.jvm.free._
import hammock.circe.implicits._

import scala.util.{ Failure, Success }

import io.circe._
import io.circe.generic.auto._

object Main extends App {

  implicit val interpTrans = Interpreter()

  val request = Hammock
    .request(Method.GET, "https://api.fidesmo.com/apps", Map())
    .exec[Future]
    .as[List[String]]

  request.onComplete {
    case Success(x) => println(s"$x")
    case Failure(ex) => ex.printStackTrace
  }
}
