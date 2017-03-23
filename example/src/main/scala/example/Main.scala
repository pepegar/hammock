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
  import Codec._

  implicit val interpTrans = Interpreter()

  case class Resp(data: String)
  case class Data(name: String, number: Int)

  val uriFromString: Try[Uri] = Uri.fromString("http://httpbin.org/post").leftMap(new Exception(_)).toTry

  val request: Try[Resp] = uriFromString >>= { uri =>
    Hammock
      .request(Method.POST, uri, Map(), Some(Data("name", 4).encode))
      .exec[Try]
      .as[Resp]
  }

  request match {
    case Success(x) => println(s"$x")
    case Failure(ex) => ex.printStackTrace
  }
}
