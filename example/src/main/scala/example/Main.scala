package example

import cats.effect._

import hammock._
import hammock.apache._
import hammock.marshalling._
import hammock.circe.implicits._
import ApacheInterpreter._
import io.circe.generic.auto._
import repr._

object Main extends App {

  val resp = Hammock
    .request(Method.POST, postUri, Map(), Some(Req("name", 4)))
    .as[Resp]
    .exec[IO]
    .unsafeRunSync

  println(resp)
}
