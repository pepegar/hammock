package example

import cats.implicits._
import scala.util._

import hammock._
import hammock.free._
import hammock.jvm.free._
import hammock.circe.implicits._

import scala.util.{ Failure, Success }

import io.circe._
import io.circe.generic.auto._

object Main extends App{
  implicit val interpTrans = Interpreter()

  case class Resp(data: String)
  case class Data(name: String, number: Int)

  val request = Hammock
    .request(Method.POST, "http://httpbin.org/post", Map(), Some(Codec[Data].encode(Data("name", 4))))
    .exec[Try]
    .as[Resp]

  request match {
    case Success(x) => println(s"$x")
    case Failure(ex) => ex.printStackTrace
  }
}
