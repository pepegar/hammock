package example

import cats.Eval
import cats.data.EitherT
import cats.implicits._

import hammock._
import hammock.free._
import hammock.jvm.free._
import hammock.circe.implicits._

import scala.util.{ Failure, Success }

import io.circe._
import io.circe.generic.auto._

object Main extends App {
  import Codec._

  implicit val interpTrans = Interpreter()
  type Target[A] = EitherT[Eval, Throwable, A]
  def Target = EitherT

  case class Resp(data: String)
  case class Data(name: String, number: Int)

  val uriFromString: Target[Uri] = Target.fromEither[Eval](Uri.fromString("http://httpbin.org/post").leftMap(new Exception(_)))

  val request: Target[Resp] = uriFromString >>= { uri =>
    Hammock
      .request(Method.POST, uri, Map(), Some(Data("name", 4).encode))
      .exec[Target]
      .as[Resp]

  }

  request.value.value match {
    case Right(x) => println(s"$x")
    case Left(ex) => ex.printStackTrace
  }
}
