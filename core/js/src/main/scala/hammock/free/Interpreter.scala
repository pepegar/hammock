package hammock
package free

import scala.util.{ Success, Failure }
import scala.concurrent.Await
import scala.concurrent.duration.Duration

import cats._
import cats.data._

import fr.hmil.roshttp.HttpRequest
import fr.hmil.roshttp.{Method => RosMethod}
import fr.hmil.roshttp.body.PlainTextBody
import fr.hmil.roshttp.response.SimpleHttpResponse

import monix.execution.Scheduler

class Interpreter(implicit sch: Scheduler, timeout: Duration) extends InterpTrans {

  import algebra._

  override def trans[F[_]](implicit ME: MonadError[F, Throwable]): HttpRequestF ~> F = λ[HttpRequestF ~> F](_ match {
    case req@Options(url, headers, body) => doReq(req, RosMethod.OPTIONS)
    case req@Get(url, headers, body) => doReq(req, RosMethod.GET)
    case req@Head(url, headers, body) => doReq(req, RosMethod.HEAD)
    case req@Post(url, headers, body) => doReq(req, RosMethod.POST)
    case req@Put(url, headers, body) => doReq(req, RosMethod.PUT)
    case req@Delete(url, headers, body) => doReq(req, RosMethod.DELETE)
    case req@Trace(url, headers, body) => doReq(req, RosMethod.TRACE)
  })

  private def doReq[F[_]](req: HttpRequestF[HttpResponse], method: RosMethod)(implicit ME: MonadError[F, Throwable]): F[HttpResponse] = ME.catchNonFatal {
    val req1 = HttpRequest(req.url)
      .withMethod(method)

    val req2 = req.body match {
      case Some(str) => req1.withBody(PlainTextBody(str))
      case _ => req1
    }

    val request = req.headers.foldLeft(req2)((acc, kv) => acc.withHeader(kv._1, kv._2))
    val responseF = request.send()
    var response: SimpleHttpResponse = Await.result(responseF, timeout)

    val status = Status.Statuses.getOrElse(response.statusCode, throw new Exception(s"unknown status ${response.statusCode}"))
    val responseHeaders = response.headers
    val body = response.body

    HttpResponse(status, responseHeaders, body)
  }
}

object Interpreter {

  def apply(implicit sch: Scheduler, timeout: Duration): Interpreter = new Interpreter

}
