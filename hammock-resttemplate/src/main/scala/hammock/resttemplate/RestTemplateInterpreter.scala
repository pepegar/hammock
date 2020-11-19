package hammock
package resttemplate

import java.net.URI

import cats._
import cats.syntax.all._
import cats.data.Kleisli
import cats.effect._
import org.springframework.http._
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

import scala.jdk.CollectionConverters._

object RestTemplateInterpreter {

  def apply[F[_]](implicit F: InterpTrans[F]): InterpTrans[F] = F

  implicit def instance[F[_]: Sync](
      implicit client: RestTemplate = new RestTemplate()
  ): InterpTrans[F] = new InterpTrans[F] {
    override def trans: HttpF ~> F = transK andThen λ[Kleisli[F, RestTemplate, *] ~> F](_.run(client))
  }

  def transK[F[_]: Sync]: HttpF ~> Kleisli[F, RestTemplate, *] = {
    λ[HttpF ~> Kleisli[F, RestTemplate, *]] {
      case reqF @ (Get(_) | Delete(_) | Head(_) | Options(_) | Trace(_) | Post(_) | Put(_) | Patch(_)) =>
        Kleisli { implicit client =>
          for {
            req             <- mapRequest[F](reqF)
            res             <- execute[F](req)
            hammockResponse <- mapResponse[F](res)
          } yield hammockResponse
        }
    }
  }

  def mapRequest[F[_]: Sync](reqF: HttpF[HttpResponse]): F[RequestEntity[String]] = {

    def httpEntity: HttpEntity[String] = new HttpEntity(
      reqF.req.entity.map(_.cata[String](_.body, _.body.map(_.toChar).mkString, Function.const(""))).orNull,
      new LinkedMultiValueMap[String, String](reqF.req.headers.view.mapValues(List(_).asJava).toMap.asJava)
    )

    def requestEntity(httpMethod: HttpMethod): RequestEntity[String] =
      new RequestEntity[String](httpEntity.getBody, httpEntity.getHeaders, httpMethod, new URI(reqF.req.uri.show))

    (reqF match {
      case Get(_)     => requestEntity(HttpMethod.GET)
      case Delete(_)  => requestEntity(HttpMethod.DELETE)
      case Head(_)    => requestEntity(HttpMethod.HEAD)
      case Options(_) => requestEntity(HttpMethod.OPTIONS)
      case Post(_)    => requestEntity(HttpMethod.POST)
      case Put(_)     => requestEntity(HttpMethod.PUT)
      case Trace(_)   => requestEntity(HttpMethod.TRACE)
      case Patch(_)   => requestEntity(HttpMethod.PATCH)
    }).pure[F]
  }

  def execute[F[_]: Sync](rtRequest: RequestEntity[String])(implicit client: RestTemplate): F[ResponseEntity[String]] =
    Sync[F].delay { client.exchange(rtRequest, classOf[String]) }

  def mapResponse[F[_]: Applicative](response: ResponseEntity[String]): F[HttpResponse] = {

    def createEntity(response: ResponseEntity[String]): Entity = response.getHeaders.getContentType match {
      case MediaType.APPLICATION_OCTET_STREAM => Entity.ByteArrayEntity(response.getBody.getBytes)
      case _                                  => Entity.StringEntity(response.getBody)
    }

    HttpResponse(
      Status.Statuses(response.getStatusCodeValue),
      response.getHeaders.toSingleValueMap.asScala.toMap,
      createEntity(response)
    ).pure[F]
  }
}
