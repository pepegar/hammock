package example

import cats.instances.try_._
import hammock._
import hammock.circe.implicits._
import hammock.jvm.free.Interpreter

import scala.util.Try

object DeclarativeAsExample extends App {
  implicit val interpreter = Interpreter()

  val response = Hammock
    .request(Method.GET, Uri.unsafeParse("https://api.fidesmo.com/apps"), Map())
    .as[List[String]]
    .exec[Try]

  println(response)
}
