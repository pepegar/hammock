import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import cats._
import cats.implicits._

import hammock._


object Main extends App {
  case class App(id: String)
  type Apps = List[App]

  val request = Hammock
    .request(Method.GET, "https://api.fidesmo.com/apps", Map())
    .run[Future]

  request.map { x =>
    println(s"$x")
  }

}
