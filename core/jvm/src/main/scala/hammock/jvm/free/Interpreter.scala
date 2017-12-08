package hammock
package jvm
package free

import hammock.free._

import cats._
import cats.data._
import cats.syntax.show._
import cats.effect.Sync

import java.io.{BufferedReader, InputStream, InputStreamReader}

import org.apache.http.Header
import org.apache.http.client.HttpClient
import org.apache.http.client.methods._
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicHeader
import org.apache.http.util.EntityUtils

class Interpreter[F[_]](client: HttpClient) extends InterpTrans[F] {

  import hammock.free.algebra._
  import Uri._

  override def trans(implicit S: Sync[F]) = transK andThen λ[Kleisli[F, HttpClient, ?] ~> F](_.run(client))

  def transK(implicit S: Sync[F]): HttpRequestF ~> Kleisli[F, HttpClient, ?] =
    λ[HttpRequestF ~> Kleisli[F, HttpClient, ?]](_ match {
      case req @ Options(uri, headers)    => doReq(req)
      case req @ Get(uri, headers)        => doReq(req)
      case req @ Head(uri, headers)       => doReq(req)
      case req @ Post(uri, headers, body) => doReq(req)
      case req @ Put(uri, headers, body)  => doReq(req)
      case req @ Delete(uri, headers)     => doReq(req)
      case req @ Trace(uri, headers)      => doReq(req)
    })

  private def doReq(reqF: HttpRequestF[HttpResponse])(implicit S: Sync[F]): Kleisli[F, HttpClient, HttpResponse] =
    Kleisli { client =>
      Sync[F].delay {
        val req             = getApacheRequest(reqF)
        val resp            = client.execute(req)
        val entity          = resp.getEntity
        val body            = responseContentToString(entity.getContent())
        val status          = Status.get(resp.getStatusLine.getStatusCode)
        val responseHeaders = resp.getAllHeaders().map(h => h.getName -> h.getValue).toMap
        EntityUtils.consume(entity)

        HttpResponse(status, responseHeaders, body)
      }
    }

  private def getApacheRequest(f: HttpRequestF[HttpResponse]): HttpUriRequest = f match {
    case Get(uri, headers) =>
      val req = new HttpGet(uri.show)
      req.setHeaders(prepareHeaders(headers))
      req
    case Options(uri, headers) =>
      val req = new HttpOptions(uri.show)
      req.setHeaders(prepareHeaders(headers))
      req
    case Head(uri, headers) =>
      val req = new HttpHead(uri.show)
      req.setHeaders(prepareHeaders(headers))
      req
    case Post(uri, headers, body) =>
      val req = new HttpPost(uri.show)
      req.setHeaders(prepareHeaders(headers))
      body foreach (b => req.setEntity(new StringEntity(b)))
      req
    case Put(uri, headers, body) =>
      val req = new HttpPut(uri.show)
      req.setHeaders(prepareHeaders(headers))
      body foreach (b => req.setEntity(new StringEntity(b)))
      req
    case Delete(uri, headers) =>
      val req = new HttpDelete(uri.show)
      req.setHeaders(prepareHeaders(headers))
      req
    case Trace(uri, headers) =>
      val req = new HttpTrace(uri.show)
      req.setHeaders(prepareHeaders(headers))
      req
  }

  private def prepareHeaders(headers: Map[String, String]): Array[Header] =
    headers map {
      case (k, v) => new BasicHeader(k, v)
    } toArray

  private def responseContentToString(content: InputStream): String = {
    val rd = new BufferedReader(new InputStreamReader(content))

    Stream.continually(rd.readLine()).takeWhile(_ != null).mkString("")
  }
}

object Interpreter {
  implicit val client = HttpClientBuilder.create().build()

  def apply[F[_]]: Interpreter[F] = new Interpreter[F](client)
}
