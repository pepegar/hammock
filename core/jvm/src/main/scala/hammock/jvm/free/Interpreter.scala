package hammock
package jvm
package free

import hammock.free._

import cats._
import cats.implicits._
import cats.data._
import cats.effect.Sync

import java.io.{BufferedReader, InputStream, InputStreamReader}

import org.apache.http.{Header, HttpEntity}
import org.apache.http.client.HttpClient
import org.apache.http.client.methods._
import org.apache.http.{entity => apache}
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicHeader
import org.apache.http.util.EntityUtils

class Interpreter[F[_]](client: HttpClient) extends InterpTrans[F] {

  import hammock.free.algebra._
  import Uri._

  override def trans(implicit S: Sync[F]) = transK andThen λ[Kleisli[F, HttpClient, ?] ~> F](_.run(client))

  def transK(implicit S: Sync[F]): HttpRequestF ~> Kleisli[F, HttpClient, ?] =
    λ[HttpRequestF ~> Kleisli[F, HttpClient, ?]] {
      case req: Options => doReq(req)
      case req: Get     => doReq(req)
      case req: Head    => doReq(req)
      case req: Post    => doReq(req)
      case req: Put     => doReq(req)
      case req: Delete  => doReq(req)
      case req: Trace   => doReq(req)
    }

  private def doReq(reqF: HttpRequestF[HttpResponse])(implicit F: Sync[F]): Kleisli[F, HttpClient, HttpResponse] =
    Kleisli { client =>
      for {
        req             <- getApacheRequest(reqF)
        resp            <- F.delay(client.execute(req))
        body            <- F.delay(responseContentToString(resp.getEntity.getContent))
        status          <- Status.get(resp.getStatusLine.getStatusCode).pure[F]
        responseHeaders <- resp.getAllHeaders.map(h => h.getName -> h.getValue).toMap.pure[F]
        _               <- F.delay(EntityUtils.consume(resp.getEntity))
      } yield HttpResponse(status, responseHeaders, new Entity.StringEntity(body))
    }

  private def getApacheRequest(f: HttpRequestF[HttpResponse])(implicit F: Sync[F]): F[HttpUriRequest] = f match {
    case reqF @ (Get(_) | Options(_) | Head(_) | Delete(_) | Trace(_) | Post(_) | Put(_)) =>
      for {
        req <- F.pure(new HttpPost(reqF.req.uri.show))
        _   <- F.delay(req.setHeaders(prepareHeaders(reqF.req.headers)))
        _ <- reqF.req.entity match {
          case Some(e) =>
            mapEntity(e) map req.setEntity
          case None =>
            F.pure(())
        }
      } yield req
  }

  private def mapEntity(entity: Entity)(implicit F: Sync[F]): F[HttpEntity] = entity match {
    case Entity.StringEntity(body, contentType) =>
      mapContentType(contentType) map { parsedContentType =>
        new apache.StringEntity(body, parsedContentType)
      }
  }

  private def mapContentType(contentType: ContentType)(implicit F: Sync[F]): F[apache.ContentType] =
    F.delay(apache.ContentType.parse(contentType.name))

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
