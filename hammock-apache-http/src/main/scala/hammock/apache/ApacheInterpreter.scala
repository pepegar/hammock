package hammock
package apache

import cats._
import cats.data._
import cats.implicits._
import cats.effect.Sync
import java.io.{BufferedReader, InputStreamReader}
import org.apache.http.{Header, HttpEntity, HttpResponse => ApacheResponse}
import org.apache.http.client.HttpClient
import org.apache.http.client.methods._
import org.apache.http.{entity => apache}
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicHeader
import org.apache.http.util.EntityUtils
import Uri._

object ApacheInterpreter {

  def apply[F[_]](implicit F: InterpTrans[F]): InterpTrans[F] = F

  implicit def instance[F[_]: Sync](
      implicit
      client: HttpClient = HttpClientBuilder.create().build()): InterpTrans[F] =
    new InterpTrans[F] {
      def trans: HttpF ~> F = transK andThen λ[Kleisli[F, HttpClient, *] ~> F](_.run(client))
    }

  def transK[F[_]: Sync]: HttpF ~> Kleisli[F, HttpClient, *] = {

    def responseToEntity(response: ApacheResponse): F[Entity] = Sync[F].delay {
      Option(response.getEntity) // getEntity can return null
        .map(_.getContent)
        .map { content =>
          val rd = new BufferedReader(new InputStreamReader(content))
          Entity.StringEntity(Stream.continually(rd.readLine()).takeWhile(_ != null).mkString(""))
        } getOrElse Entity.EmptyEntity
    }

    def doReq(reqF: HttpF[HttpResponse]): Kleisli[F, HttpClient, HttpResponse] = Kleisli { client =>
      for {
        req             <- mapRequest(reqF)
        resp            <- Sync[F].delay(client.execute(req))
        entity          <- responseToEntity(resp)
        status          <- Status.get(resp.getStatusLine.getStatusCode).pure[F]
        responseHeaders <- resp.getAllHeaders.map(h => h.getName -> h.getValue).toMap.pure[F]
        _               <- Sync[F].delay(EntityUtils.consume(resp.getEntity))
      } yield HttpResponse(status, responseHeaders, entity)
    }

    λ[HttpF ~> Kleisli[F, HttpClient, *]] {
      case req: Options => doReq(req)
      case req: Get     => doReq(req)
      case req: Head    => doReq(req)
      case req: Post    => doReq(req)
      case req: Put     => doReq(req)
      case req: Delete  => doReq(req)
      case req: Trace   => doReq(req)
      case req: Patch   => doReq(req)
    }

  }

  def mapRequest[F[_]: Sync](f: HttpF[HttpResponse]): F[HttpUriRequest] = {

    def mapContentType(contentType: ContentType): F[apache.ContentType] =
      Sync[F].delay(apache.ContentType.parse(contentType.name))

    def mapEntity(entity: Entity): F[HttpEntity] = entity match {
      case Entity.StringEntity(body, contentType) =>
        mapContentType(contentType) map (parsedContentType => new apache.StringEntity(body, parsedContentType))
      case Entity.ByteArrayEntity(body, contentType) =>
        mapContentType(contentType) map (parsedContentType => new apache.ByteArrayEntity(body, parsedContentType))
      case Entity.EmptyEntity => Sync[F].delay(new apache.BasicHttpEntity())
    }

    def prepareHeaders(headers: Map[String, String]): Array[Header] =
      headers.map { case (k, v) => new BasicHeader(k, v) }.toArray

    f match {
      case Get(HttpRequest(uri, headers, _)) =>
        Sync[F].delay {
          val req = new HttpGet(uri.show)
          req.setHeaders(prepareHeaders(headers))
          req
        }
      case Options(HttpRequest(uri, headers, _)) =>
        Sync[F].delay {
          val req = new HttpOptions(uri.show)
          req.setHeaders(prepareHeaders(headers))
          req
        }
      case Head(HttpRequest(uri, headers, _)) =>
        Sync[F].delay {
          val req = new HttpHead(uri.show)
          req.setHeaders(prepareHeaders(headers))
          req
        }
      case Post(HttpRequest(uri, headers, entity)) =>
        for {
          req <- Sync[F].delay(new HttpPost(uri.show))
          _   <- Sync[F].delay(req.setHeaders(prepareHeaders(headers)))
          _ <- if (entity.isDefined) {
            mapEntity(entity.get) >>= (apacheEntity => Sync[F].delay(req.setEntity(apacheEntity)))
          } else ().pure[F]
        } yield req
      case Put(HttpRequest(uri, headers, maybeEntity)) =>
        for {
          req <- Sync[F].delay(new HttpPut(uri.show))
          _   <- Sync[F].delay(req.setHeaders(prepareHeaders(headers)))
          _ <- if (maybeEntity.isDefined) {
            mapEntity(maybeEntity.get) >>= (apacheEntity => Sync[F].delay(req.setEntity(apacheEntity)))
          } else ().pure[F]
        } yield req
      case Delete(HttpRequest(uri, headers, _)) =>
        Sync[F].delay {
          val req = new HttpDelete(uri.show)
          req.setHeaders(prepareHeaders(headers))
          req
        }
      case Trace(HttpRequest(uri, headers, _)) =>
        Sync[F].delay {
          val req = new HttpTrace(uri.show)
          req.setHeaders(prepareHeaders(headers))
          req
        }
      case Patch(HttpRequest(uri, headers, entity)) =>
        for {
          req <- Sync[F].delay(new HttpPatch(uri.show))
          _   <- Sync[F].delay(req.setHeaders(prepareHeaders(headers)))
          _ <- if (entity.isDefined) {
            mapEntity(entity.get) >>= (apacheEntity => Sync[F].delay(req.setEntity(apacheEntity)))
          } else ().pure[F]
        } yield req
    }
  }
}
