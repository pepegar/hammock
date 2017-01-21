import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.Future

import cats.implicits._

import hammock._
import hammock.implicits._
import hammock.circe.implicits._

import scala.util.{ Failure, Success }

import io.circe._
import io.circe.generic.auto._

object Main extends App {

  val request = Hammock
    .request(Method.GET, "https://api.fidesmo.com/apps", Map())
    .run[Future]
    .as[List[String]]

  request.onComplete {
    case Success(x) => println(s"$x")
    case Failure(ex) => ex.printStackTrace
  }
}
