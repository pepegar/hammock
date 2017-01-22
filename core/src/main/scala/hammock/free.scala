package hammock

import cats._
import cats.free._
import cats.data.Kleisli

import java.io.{ BufferedReader, InputStream, InputStreamReader }

import org.apache.http.client.HttpClient
import org.apache.http.client.methods._


object free {

  sealed abstract class HttpRequestF[A] extends Product with Serializable {
    def url: String
    def headers: Map[String, String]
    def body: Option[String]
  }
  final case class Options(url: String, headers: Map[String, String], body: Option[String]) extends HttpRequestF[HttpResponse]
  final case class Get(url: String, headers: Map[String, String], body: Option[String]) extends HttpRequestF[HttpResponse]
  final case class Head(url: String, headers: Map[String, String], body: Option[String]) extends HttpRequestF[HttpResponse]
  final case class Post(url: String, headers: Map[String, String], body: Option[String]) extends HttpRequestF[HttpResponse]
  final case class Put(url: String, headers: Map[String, String], body: Option[String]) extends HttpRequestF[HttpResponse]
  final case class Delete(url: String, headers: Map[String, String], body: Option[String]) extends HttpRequestF[HttpResponse]
  final case class Trace(url: String, headers: Map[String, String], body: Option[String]) extends HttpRequestF[HttpResponse]

  type HttpRequestIO[A] = Free[HttpRequestF, A]

  object Ops {
    def options(url: String, headers: Map[String, String], body: Option[String]): HttpRequestIO[HttpResponse] = Free.liftF(Options(url, headers, body))
    def get(url: String, headers: Map[String, String], body: Option[String]): HttpRequestIO[HttpResponse] = Free.liftF(Get(url, headers, body))
    def head(url: String, headers: Map[String, String], body: Option[String]): HttpRequestIO[HttpResponse] = Free.liftF(Head(url, headers, body))
    def post(url: String, headers: Map[String, String], body: Option[String]): HttpRequestIO[HttpResponse] = Free.liftF(Post(url, headers, body))
    def put(url: String, headers: Map[String, String], body: Option[String]): HttpRequestIO[HttpResponse] = Free.liftF(Put(url, headers, body))
    def delete(url: String, headers: Map[String, String], body: Option[String]): HttpRequestIO[HttpResponse] = Free.liftF(Delete(url, headers, body))
    def trace(url: String, headers: Map[String, String], body: Option[String]): HttpRequestIO[HttpResponse] = Free.liftF(Trace(url, headers, body))
  }

  class HttpRequestC[F[_]](implicit I: Inject[HttpRequestF, F]) {
    def options(url: String, headers: Map[String, String], body: Option[String]): Free[F, HttpResponse] = Free.inject(Options(url, headers, body))
    def get(url: String, headers: Map[String, String], body: Option[String]): Free[F, HttpResponse] = Free.inject(Get(url, headers, body))
    def head(url: String, headers: Map[String, String], body: Option[String]): Free[F, HttpResponse] = Free.inject(Head(url, headers, body))
    def post(url: String, headers: Map[String, String], body: Option[String]): Free[F, HttpResponse] = Free.inject(Post(url, headers, body))
    def put(url: String, headers: Map[String, String], body: Option[String]): Free[F, HttpResponse] = Free.inject(Put(url, headers, body))
    def delete(url: String, headers: Map[String, String], body: Option[String]): Free[F, HttpResponse] = Free.inject(Delete(url, headers, body))
    def trace(url: String, headers: Map[String, String], body: Option[String]): Free[F, HttpResponse] = Free.inject(Trace(url, headers, body))
  }

  object HttpRequestC {
    implicit def httpRequestC[F[_]](implicit I: Inject[HttpRequestF, F]): HttpRequestC[F] = new HttpRequestC[F]
  }

  object Interp {
    def trans[F[_]](implicit httpClient: HttpClient, ME: MonadError[F, Throwable]) = transK andThen λ[Kleisli[F, HttpClient, ?] ~> F](_.run(httpClient))

    def transK[F[_]](implicit ME: MonadError[F, Throwable]): HttpRequestF ~> Kleisli[F, HttpClient, ?] = λ[HttpRequestF ~> Kleisli[F, HttpClient, ?]](_ match {
      case req@Options(url, headers, body) => doReq(req)
      case req@Get(url, headers, body) => doReq(req)
      case req@Head(url, headers, body) => doReq(req)
      case req@Post(url, headers, body) => doReq(req)
      case req@Put(url, headers, body) => doReq(req)
      case req@Delete(url, headers, body) => doReq(req)
      case req@Trace(url, headers, body) => doReq(req)
    })

    private def doReq[F[_]](reqF: HttpRequestF[HttpResponse])(implicit ME: MonadError[F, Throwable]): Kleisli[F, HttpClient, HttpResponse] = Kleisli { client =>
      ME.catchNonFatal {
        val req = getApacheRequest(reqF)
        reqF.headers.foreach {
          case (k, v) =>
            req.addHeader(k, v)
        }

        val resp = client.execute(req)
        
        val body = responseContentToString(resp.getEntity().getContent())
        val status = Status.Statuses.getOrElse(resp.getStatusLine.getStatusCode, throw new Exception) // todo: This is shitty
        val responseHeaders = resp.getAllHeaders().map(h => h.getName -> h.getValue).toMap

        HttpResponse(status, responseHeaders, body)
      }
    }
  }

  private def getApacheRequest(f: HttpRequestF[HttpResponse]): HttpUriRequest = f match {
    case Get(url, headers, body) => new HttpGet(url)
    case Options(url, headers, body) => new HttpOptions(url)
    case Head(url, headers, body) => new HttpHead(url)
    case Post(url, headers, body) => new HttpPost(url)
    case Put(url, headers, body) => new HttpPut(url)
    case Delete(url, headers, body) => new HttpDelete(url)
    case Trace(url, headers, body) => new HttpTrace(url)
  }

  private def responseContentToString(content: InputStream): String = {
    val rd = new BufferedReader(new InputStreamReader(content))

    Stream.continually(rd.readLine()).takeWhile(_ != null).mkString("")
  }
}
