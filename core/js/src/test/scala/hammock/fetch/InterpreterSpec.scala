package hammock
package fetch

import cats.effect.IO
import org.scalatest.{AsyncFlatSpec, Matchers}

class InterpreterSpec extends AsyncFlatSpec with Matchers {
  behavior of "node.Interpreter.trans"
  implicit override def executionContext = scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
  Seq(
    ("Options", (uri: Uri, headers: Map[String, String]) => Ops.options(uri, headers)),
    ("Get", (uri: Uri, headers: Map[String, String]) => Ops.get(uri, headers)),
    ("Head", (uri: Uri, headers: Map[String, String]) => Ops.head(uri, headers)),
    ("Post", (uri: Uri, headers: Map[String, String]) => Ops.post(uri, headers, None)),
    ("Put", (uri: Uri, headers: Map[String, String]) => Ops.put(uri, headers, None)),
    ("Delete", (uri: Uri, headers: Map[String, String]) => Ops.delete(uri, headers)),
    ("Trace", (uri: Uri, headers: Map[String, String]) => Ops.trace(uri, headers)),
    ("Patch", (uri: Uri, headers: Map[String, String]) => Ops.patch(uri, headers, None))
  ) map {
    case (method, operation) =>
      it should s"get response from mocky with $method requests" in {
        operation(uri"http://www.mocky.io/v2/5185415ba171ea3a00704eed", Map("mock" -> "header")) foldMap Interpreter[IO].trans unsafeToFuture () map (_.status.code shouldBe 200)
      }
  }
}
