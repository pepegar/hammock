package hammock
package jvm

import cats._
import cats.implicits._
import cats.data._
import cats.effect.Sync

import java.io.{BufferedReader, InputStreamReader}

import org.apache.http.{Header, HttpEntity, HttpResponse => ApacheResponse}
import org.apache.http.client.HttpClient
import org.apache.http.client.methods._
import org.apache.http.{entity => apache}
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.message.BasicHeader
import org.apache.http.util.EntityUtils

class Interpreter[F[_]](client: HttpClient) extends InterpTrans[F] {

  import Uri._

  override def trans(implicit S: Sync[F]) = transK andThen λ[Kleisli[F, HttpClient, ?] ~> F](_.run(client))

  def transK(implicit S: Sync[F]): HttpF ~> Kleisli[F, HttpClient, ?] =
    λ[HttpF ~> Kleisli[F, HttpClient, ?]] {
      case req: Options => doReq(req)
      case req: Get     => doReq(req)
      case req: Head    => doReq(req)
      case req: Post    => doReq(req)
      case req: Put     => doReq(req)
      case req: Delete  => doReq(req)
      case req: Trace   => doReq(req)
      case req: Patch   => doReq(req)
    }

  private def doReq(reqF: HttpF[HttpResponse])(implicit F: Sync[F]): Kleisli[F, HttpClient, HttpResponse] =
    Kleisli { client =>
      for {
        req             <- getApacheRequest(reqF)
        resp            <- F.delay(client.execute(req))
        entity          <- responseToEntity(resp)
        status          <- Status.get(resp.getStatusLine.getStatusCode).pure[F]
        responseHeaders <- resp.getAllHeaders.map(h => h.getName -> h.getValue).toMap.pure[F]
        _               <- F.delay(EntityUtils.consume(resp.getEntity))
      } yield HttpResponse(status, responseHeaders, entity)
    }

  def getApacheRequest(f: HttpF[HttpResponse])(implicit F: Sync[F]): F[HttpUriRequest] = f match {
    case Get(HttpRequest(uri, headers, _)) =>
      F.delay {
        val req = new HttpGet(uri.show)
        req.setHeaders(prepareHeaders(headers))
        req
      }
    case Options(HttpRequest(uri, headers, _)) =>
      F.delay {
        val req = new HttpOptions(uri.show)
        req.setHeaders(prepareHeaders(headers))
        req
      }
    case Head(HttpRequest(uri, headers, _)) =>
      F.delay {
        val req = new HttpHead(uri.show)
        req.setHeaders(prepareHeaders(headers))
        req
      }
    case Post(HttpRequest(uri, headers, entity)) =>
      for {
        req <- new HttpPost(uri.show).pure[F]
        _   <- F.delay(req.setHeaders(prepareHeaders(headers)))
        _ <- if (entity.isDefined) {
          mapEntity(entity.get) >>= { apacheEntity =>
            F.delay(req.setEntity(apacheEntity))
          }
        } else {
          ().pure[F]
        }
      } yield req
    case Put(HttpRequest(uri, headers, entity)) =>
      for {
        req <- new HttpPut(uri.show).pure[F]
        _   <- F.delay(req.setHeaders(prepareHeaders(headers)))
        _ <- if (entity.isDefined) {
          mapEntity(entity.get) >>= { apacheEntity =>
            F.delay(req.setEntity(apacheEntity))
          }
        } else {
          ().pure[F]
        }
      } yield req
    case Delete(HttpRequest(uri, headers, _)) =>
      F.delay {
        val req = new HttpDelete(uri.show)
        req.setHeaders(prepareHeaders(headers))
        req
      }
    case Trace(HttpRequest(uri, headers, _)) =>
      F.delay {
        val req = new HttpTrace(uri.show)
        req.setHeaders(prepareHeaders(headers))
        req
      }
    case Patch(HttpRequest(uri, headers, entity)) =>
      for {
        req <- new HttpPatch(uri.show).pure[F]
        _   <- F.delay(req.setHeaders(prepareHeaders(headers)))
        _ <- if (entity.isDefined) {
          mapEntity(entity.get) >>= { apacheEntity =>
            F.delay(req.setEntity(apacheEntity))
          }
        } else {
          ().pure[F]
        }
      } yield req
  }

  private def mapEntity(entity: Entity)(implicit F: Sync[F]): F[HttpEntity] = entity match {
    case Entity.StringEntity(body, contentType) =>
      mapContentType(contentType) map { parsedContentType =>
        new apache.StringEntity(body, parsedContentType)
      }
    case Entity.ByteArrayEntity(body, contentType) =>
      mapContentType(contentType) map { parsedContentType =>
        new apache.ByteArrayEntity(body, parsedContentType)
      }
    case Entity.MultipartEntity(body, contentType) =>
      mapContentType(contentType) map { parsedContentType =>
        val builder = MultipartEntityBuilder.create()

        body.foldLeft(builder)()
      }
    case Entity.EmptyEntity =>
      F.delay(new apache.BasicHttpEntity())
  }

  private def mapContentType(contentType: ContentType)(implicit F: Sync[F]): F[apache.ContentType] =
    F.delay(apache.ContentType.parse(contentType.name))

  private def prepareHeaders(headers: Map[String, String]): Array[Header] =
    headers
      .map({
        case (k, v) => new BasicHeader(k, v)
      })
      .toArray

  private def responseToEntity(response: ApacheResponse)(implicit F: Sync[F]): F[Entity] = F.delay {
    Option(response.getEntity) // getEntity can return null
      .map(_.getContent)
      .map { content =>
        val rd = new BufferedReader(new InputStreamReader(content))
        Entity.StringEntity(Stream.continually(rd.readLine()).takeWhile(_ != null).mkString(""))
      } getOrElse Entity.EmptyEntity
  }
}

object Interpreter {
  implicit val client = HttpClientBuilder.create().build()

  def apply[F[_]]: Interpreter[F] = new Interpreter[F](client)
}
