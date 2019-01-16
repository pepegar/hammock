package example

import cats.effect._

import hammock._
import hammock.apache._
import hammock.marshalling._
import hammock.circe.implicits._

import io.circe.generic.auto._

object Main extends App {
  implicit val interpTrans = ApacheInterpreter[IO]

  case class Resp(data: String)
  case class Req(name: String, number: Int)

  val uri = uri"http://httpbin.org/post"

  val resp = Hammock
    .request(Method.POST, uri, Map(), Some(Req("name", 4)))
    .as[Resp]
    .exec[IO]
    .unsafeRunSync

  println(resp)
}
