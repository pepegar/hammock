package hammock
package free

import cats._
import cats.data._

import java.io.{ BufferedReader, InputStream, InputStreamReader }

import org.apache.http.client.HttpClient
import org.apache.http.client.methods._
import org.apache.http.impl.client.HttpClientBuilder

object Interpreter extends InterpTrans {

  implicit val client = HttpClientBuilder.create().build()

  import algebra._

  override def trans[F[_]](implicit ME: MonadError[F, Throwable]) = transK andThen λ[Kleisli[F, HttpClient, ?] ~> F](_.run(client))

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
      val status = Status.get(resp.getStatusLine.getStatusCode)
      val responseHeaders = resp.getAllHeaders().map(h => h.getName -> h.getValue).toMap

      HttpResponse(status, responseHeaders, body)
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

