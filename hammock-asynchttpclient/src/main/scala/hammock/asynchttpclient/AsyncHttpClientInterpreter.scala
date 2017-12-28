package hammock
package asynchttpclient

import cats._
import cats.implicits._
import cats.data.Kleisli
import cats.effect._

import org.asynchttpclient._
import java.util.{concurrent => jc}
import scala.util._
import scala.collection.JavaConverters._

class AsyncHttpClientInterpreter[F[_]: Async](client: AsyncHttpClient = new DefaultAsyncHttpClient())
    extends InterpTrans[F] {

  def toF[A](future: jc.Future[A]): F[A] =
    Async[F].async(_(Try(future.get) match {
      case Failure(err) => Left(err)
      case Success(a)   => Right(a)
    }))

  def getBuilder(reqF: HttpF[HttpResponse]): BoundRequestBuilder = reqF match {
    case Get(_)     => client.prepareGet(reqF.req.uri.show)
    case Delete(_)  => client.prepareDelete(reqF.req.uri.show)
    case Head(_)    => client.prepareHead(reqF.req.uri.show)
    case Options(_) => client.prepareOptions(reqF.req.uri.show)
    case Post(_)    => client.preparePost(reqF.req.uri.show)
    case Put(_)     => client.preparePut(reqF.req.uri.show)
    case Trace(_)   => client.prepareTrace(reqF.req.uri.show)
  }

  def putHeaders(req: BoundRequestBuilder, headers: Map[String, String]): F[Unit] = Async[F].delay {
    req.setSingleHeaders(headers.map(kv => kv._1.asInstanceOf[CharSequence] -> kv._2).asJava)
    ()
  }

  def mapRequest(reqF: HttpF[HttpResponse]): F[BoundRequestBuilder] =
    for {
      req <- getBuilder(reqF).pure[F]
      _   <- putHeaders(req, reqF.req.headers)
      _ = reqF.req.entity
        .foreach(_.cata(str => req.setBody(str.content), bytes => req.setBody(bytes.content)))
    } yield req

  def transK: HttpF ~> Kleisli[F, AsyncHttpClient, ?] =
    λ[HttpF ~> Kleisli[F, AsyncHttpClient, ?]] { reqF =>
      reqF match {
        case Get(_) | Options(_) | Delete(_) | Head(_) | Options(_) | Trace(_) | Post(_) | Put(_) =>
          Kleisli { client =>
            for {
              req             <- mapRequest(reqF)
              ahcResponse     <- toF(req.execute())
              hammockResponse <- mapResponse(ahcResponse)
            } yield hammockResponse
          }
      }
    }

  def trans(implicit S: Sync[F]): HttpF ~> F =
    transK andThen λ[Kleisli[F, AsyncHttpClient, ?] ~> F](_.run(client))

  def createEntity(contentType: String, body: String): Entity = contentType match {
    case "application/octet-stream" => Entity.ByteArrayEntity(body.toCharArray.map(_.toByte))
    case _                          => Entity.StringEntity(body)
  }

  def mapResponse(ahcResponse: Response): F[HttpResponse] = {
    HttpResponse(
      Status.Statuses(ahcResponse.getStatusCode),
      ahcResponse.getHeaders.names.asScala.map(name => (name, ahcResponse.getHeaders.get(name))).toMap,
      createEntity(ahcResponse.getContentType, ahcResponse.getResponseBody)
    ).pure[F]
  }
}
