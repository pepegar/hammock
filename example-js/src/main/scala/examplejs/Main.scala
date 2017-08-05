package examplejs

import scala.scalajs.js.JSApp
import scala.util._
import org.scalajs.jquery.jQuery

import cats._
import cats.free._
import cats.implicits._
import cats.data.EitherT

import hammock._
import hammock.free._
import hammock.js.free._
import hammock.circe.implicits._

import io.circe._
import io.circe.generic.auto._

object Main extends JSApp {
  def main(): Unit = {

    import Codec._
    implicit val interpTrans = Interpreter

    case class Resp(json: String)
    case class Data(name: String, number: Int)


    type Target[A] = EitherT[Eval, Throwable, A]
    def Target = EitherT


    val uriFromString: Target[Uri] = Target.fromEither(Uri.fromString("http://httpbin.org/post").leftMap(new Exception(_)))

    val request: Target[Resp] = uriFromString >>= { uri =>
      Hammock
        .request(Method.POST, uri, Map(), Some(Data("name", 4).encode))
        .exec[Target]
        .as[Resp]
    }

    request.value.value match {
      case Right(resp) =>
        val dec = Codec[Data].decode(resp.json)
        jQuery("#result").append(s"""
<h3>Data you sent to the server:</h3>
<pre>$dec</pre>
""")
      case Left(ex) => ex.printStackTrace
    }
  }
}
