package example

import cats.effect._
import hammock._
import hammock.apache._
import hammock.marshalling._
import hammock.circe.implicits._
import io.circe.generic.auto._
import ApacheInterpreter._

object Main extends App {

  case class Resp(data: String)
  case class Req(name: String, number: Int)

  val uri = uri"http://httpbin.org/post"

  val postResponse = Hammock
    .request(Method.POST, uri, Map(), Some(Req("name", 4)))
    .as[Resp]
    .exec[IO]
    .unsafeRunSync

  println(postResponse)

  val getResponse = Hammock
    .request(Method.GET, uri"https://api.fidesmo.com/apps", Map())
    .as[List[String]]
    .exec[IO]
    .unsafeRunSync

  println(getResponse)
}
