package example.interpret

import cats.effect.IO
import example.repr.{GetResp, GetRespWithQueryString, Req, Resp}
import hammock.{Hammock, Method}
import hammock.marshalling._
import hammock.circe.implicits._
import io.circe.generic.auto._
import hammock.apache.ApacheInterpreter._

object ApacheInterpExampleMain extends App {

  //GET
  val getResp = Hammock
    .request(Method.GET, getUri, Map())
    .as[GetResp]
    .exec[IO]
    .unsafeRunSync

  println(s"GET::Response = $getResp")

  //GET with query string
  val getRespWithQueryString = Hammock
    .request(Method.GET, getUriWithQueryString, Map())
    .as[GetRespWithQueryString]
    .exec[IO]
    .unsafeRunSync

  println(s"GET with query string::Response = $getRespWithQueryString")

  //POST
  val postResp = Hammock
    .request(Method.POST, postUri, Map(), Some(Req("name", 4)))
    .as[Resp]
    .exec[IO]
    .unsafeRunSync

  println(s"POST::Response = $postResp")

  //PUT
  val putResp = Hammock
    .request(Method.PUT, putUri, Map(), Some(Req("name", 4)))
    .as[Resp]
    .exec[IO]
    .unsafeRunSync

  println(s"PUT::Response = $putResp")

  //DELETE
  val deleteResp = Hammock
    .request(Method.DELETE, deleteUri, Map(), Some(Req("name", 4)))
    .exec[IO]
    .unsafeRunSync

  println(s"DELETE::Response = $deleteResp")

}
