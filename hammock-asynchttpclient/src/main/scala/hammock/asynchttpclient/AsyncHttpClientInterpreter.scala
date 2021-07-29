package hammock
package asynchttpclient

import cats._
import cats.implicits._
import cats.data.Kleisli
import cats.effect._
import org.asynchttpclient._
import java.util.{concurrent => jc}
import scala.util._
import scala.jdk.CollectionConverters._

object AsyncHttpClientInterpreter {

  def apply[F[_]](implicit F: InterpTrans[F]): InterpTrans[F] = F

  implicit def instance[F[_]: Sync](
      implicit client: AsyncHttpClient = new DefaultAsyncHttpClient()
  ): InterpTrans[F] = new InterpTrans[F] {
    override def trans: HttpF ~> F = transK andThen λ[Kleisli[F, AsyncHttpClient, *] ~> F](_.run(client))
  }

  def transK[F[_]: Sync]: HttpF ~> Kleisli[F, AsyncHttpClient, *] = {

    def toF[A](future: jc.Future[A]): F[A] =
      Sync[F].blocking(future.get)

    λ[HttpF ~> Kleisli[F, AsyncHttpClient, *]] {
      case reqF @ (Get(_) | Options(_) | Delete(_) | Head(_) | Options(_) | Trace(_) | Post(_) | Put(_) | Patch(_)) =>
        Kleisli { implicit client =>
          for {
            req             <- mapRequest[F](reqF)
            ahcResponse     <- toF(req.execute())
            hammockResponse <- mapResponse[F](ahcResponse)
          } yield hammockResponse
        }
    }
  }

  def mapRequest[F[_]: Sync](reqF: HttpF[HttpResponse])(implicit client: AsyncHttpClient): F[BoundRequestBuilder] = {

    def putHeaders(req: BoundRequestBuilder, headers: Map[String, String]): F[Unit] =
      Sync[F].delay {
        req.setSingleHeaders(headers.map(kv => kv._1.asInstanceOf[CharSequence] -> kv._2).asJava)
      }.void

    def getBuilder(reqF: HttpF[HttpResponse]): BoundRequestBuilder = reqF match {
      case Get(_)     => client.prepareGet(reqF.req.uri.show)
      case Delete(_)  => client.prepareDelete(reqF.req.uri.show)
      case Head(_)    => client.prepareHead(reqF.req.uri.show)
      case Options(_) => client.prepareOptions(reqF.req.uri.show)
      case Post(_)    => client.preparePost(reqF.req.uri.show)
      case Put(_)     => client.preparePut(reqF.req.uri.show)
      case Trace(_)   => client.prepareTrace(reqF.req.uri.show)
      case Patch(_)   => client.preparePatch(reqF.req.uri.show)
    }

    for {
      req <- getBuilder(reqF).pure[F]
      _   <- putHeaders(req, reqF.req.headers)
      _ = reqF.req.entity
        .foreach(_.cata(str => req.setBody(str.content), bytes => req.setBody(bytes.content), Function.const(())))
    } yield req
  }

  def mapResponse[F[_]: Applicative](ahcResponse: Response): F[HttpResponse] = {

    def createEntity(r: Response): Entity = r.getContentType match {
      case AsyncHttpClientContentType.`application/octet-stream` => Entity.ByteArrayEntity(r.getResponseBodyAsBytes)
      case _                                                     => Entity.StringEntity(r.getResponseBody)
    }

    HttpResponse(
      Status.Statuses(ahcResponse.getStatusCode),
      ahcResponse.getHeaders.names.asScala.map(name => (name, ahcResponse.getHeaders.get(name))).toMap,
      createEntity(ahcResponse)
    ).pure[F]
  }

}
