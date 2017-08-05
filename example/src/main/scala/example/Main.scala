package example

import cats.Eval
import cats.data.EitherT
import cats.implicits._
import cats.effect._

import hammock._
import hammock.free._
import hammock.jvm.free._
import hammock.circe.implicits._

import scala.util.{ Failure, Success }
import scala.concurrent.ExecutionContext.Implicits.global

import io.circe._
import io.circe.generic.auto._

object Main extends App {
  import Codec._

  implicit val interpTrans = Interpreter[IO]

  case class Resp(data: String)
  case class Data(name: String, number: Int)

  val uriFromString: IO[Uri] = Sync[IO].delay(Uri.unsafeParse("http://httpbin.org/post"))

  val request: IO[Resp] = uriFromString >>= { uri =>
    Hammock
      .request(Method.POST, uri, Map(), Some(Data("name", 4).encode))
      .exec[IO]
      .as[Resp]

  }

  request.unsafeToFuture.onComplete(_ match {
    case Success(x) => println(s"$x")
    case Failure(ex) => ex.printStackTrace
  })
}
