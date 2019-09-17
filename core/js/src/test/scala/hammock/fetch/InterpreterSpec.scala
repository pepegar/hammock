package hammock
package fetch

import cats.effect.IO
import org.scalatest.Matchers
import org.scalatest.flatspec.AsyncFlatSpec
import scala.concurrent.ExecutionContextExecutor
import Interpreter._

class InterpreterSpec extends AsyncFlatSpec with Matchers {

  behavior of "node.Interpreter.trans"

  implicit override def executionContext: ExecutionContextExecutor =
    scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
  implicit val cs = IO.contextShift(executionContext)

  Seq(
    ("Options", (uri: Uri, headers: Map[String, String]) => Ops.options(uri, headers)),
    ("Get", (uri: Uri, headers: Map[String, String]) => Ops.get(uri, headers)),
    ("Head", (uri: Uri, headers: Map[String, String]) => Ops.head(uri, headers)),
    ("Post", (uri: Uri, headers: Map[String, String]) => Ops.post(uri, headers, None)),
    ("Put", (uri: Uri, headers: Map[String, String]) => Ops.put(uri, headers, None)),
    ("Delete", (uri: Uri, headers: Map[String, String]) => Ops.delete(uri, headers)),
    ("Trace", (uri: Uri, headers: Map[String, String]) => Ops.trace(uri, headers)),
    ("Patch", (uri: Uri, headers: Map[String, String]) => Ops.patch(uri, headers, None))
  ) foreach {
    case (method, operation) =>
      it should s"get response from mocky with $method requests" in {
        operation(uri"http://www.mocky.io/v2/5185415ba171ea3a00704eed", Map("mock" -> "header"))
          .foldMap(Interpreter[IO].trans)
          .unsafeToFuture
          .map { resp =>
            resp.status.code shouldBe 200
          }
      }
  }
}
