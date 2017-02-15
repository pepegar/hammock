package examplejs

import scala.scalajs.js.JSApp
import scala.util._
import org.scalajs.jquery.jQuery

import cats._
import cats.free._
import cats.implicits._

import hammock._
import hammock.free._
import hammock.js.free._
import hammock.circe.implicits._

import io.circe._
import io.circe.generic.auto._

object Main extends JSApp {
  def main(): Unit = {

    implicit val interpTrans = Interpreter
    case class Resp(json: String)
    case class Data(name: String, number: Int)

    val request = Hammock
      .request(Method.POST, "http://httpbin.org/post", Map(), Some(Codec[Data].encode(Data("name", 4))))
      .exec[Try]
      .as[Resp]

    request match {
      case Success(resp) =>
        val dec = Codec[Data].decode(resp.json)
        jQuery("#result").append(s"""
<h3>Data you sent to the server:</h3>
<pre>$dec</pre>
""")
      case Failure(ex) => ex.printStackTrace
    }
  }
}
