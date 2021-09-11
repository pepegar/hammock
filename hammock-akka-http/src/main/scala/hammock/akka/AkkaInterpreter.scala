package hammock
package akka

import _root_.akka.http.scaladsl.HttpExt
import _root_.akka.http.scaladsl.client.RequestBuilding.RequestBuilder
import _root_.akka.http.scaladsl.model.{
  HttpMethods,
  ContentType => AkkaContentType,
  HttpRequest => AkkaRequest,
  HttpResponse => AkkaResponse,
  StatusCode => AkkaStatus,
  _
}
import _root_.akka.http.scaladsl.model.headers.RawHeader
import _root_.akka.stream.ActorMaterializer
import _root_.akka.util.ByteString
import cats._
import cats.data.Kleisli
import cats.effect.{Async, Sync}
import cats.implicits._
import scala.concurrent.{ExecutionContext, Future}

object AkkaInterpreter {

  def apply[F[_]](implicit F: InterpTrans[F]): InterpTrans[F] = F

  implicit def instance[F[_]: Async](
      implicit
      client: HttpExt,
      materializer: ActorMaterializer,
      executionContext: ExecutionContext) =
    new InterpTrans[F] {
      override def trans: HttpF ~> F = transK andThen λ[Kleisli[F, HttpExt, *] ~> F](_.run(client))
    }

  def transK[F[_]: Async](
      implicit materializer: ActorMaterializer,
      executionContext: ExecutionContext): HttpF ~> Kleisli[F, HttpExt, *] = {

    def doReq(req: HttpF[HttpResponse]): Kleisli[F, HttpExt, HttpResponse] = Kleisli { http =>
      for {
        akkaRequest    <- mapRequest(req)
        responseFuture <- Sync[F].delay(http.singleRequest(akkaRequest).flatMap(mapResponse))
        responseF      <- Async[F].fromFuture(Sync[F].delay(responseFuture))
      } yield responseF
    }

    def mapResponse(akkaResp: AkkaResponse): Future[HttpResponse] = {
      val status  = mapStatus(akkaResp.status)
      val headers = akkaResp.headers.map(h => (h.name, h.value)).toMap
      akkaResp.entity.dataBytes
        .runFold(ByteString(""))(_ ++ _)
        .map(bs => Entity.StringEntity(bs.utf8String))
        .map(entity => HttpResponse(status, headers, entity))
    }

    def mapStatus(st: AkkaStatus): Status = st match {
      case StatusCodes.Continue                      => Status.Continue
      case StatusCodes.SwitchingProtocols            => Status.SwitchingProtocols
      case StatusCodes.Processing                    => Status.Processing
      case StatusCodes.OK                            => Status.OK
      case StatusCodes.Created                       => Status.Created
      case StatusCodes.Accepted                      => Status.Accepted
      case StatusCodes.NonAuthoritativeInformation   => Status.NonAuthoritativeInformation
      case StatusCodes.NoContent                     => Status.NoContent
      case StatusCodes.ResetContent                  => Status.ResetContent
      case StatusCodes.PartialContent                => Status.PartialContent
      case StatusCodes.MultiStatus                   => Status.MultiStatus
      case StatusCodes.AlreadyReported               => Status.AlreadyReported
      case StatusCodes.IMUsed                        => Status.IMUsed
      case StatusCodes.MultipleChoices               => Status.MultipleChoices
      case StatusCodes.MovedPermanently              => Status.MovedPermanently
      case StatusCodes.Found                         => Status.Found
      case StatusCodes.SeeOther                      => Status.SeeOther
      case StatusCodes.NotModified                   => Status.NotModified
      case StatusCodes.UseProxy                      => Status.UseProxy
      case StatusCodes.TemporaryRedirect             => Status.TemporaryRedirect
      case StatusCodes.PermanentRedirect             => Status.PermanentRedirect
      case StatusCodes.BadRequest                    => Status.BadRequest
      case StatusCodes.Unauthorized                  => Status.Unauthorized
      case StatusCodes.PaymentRequired               => Status.PaymentRequired
      case StatusCodes.Forbidden                     => Status.Forbidden
      case StatusCodes.NotFound                      => Status.NotFound
      case StatusCodes.MethodNotAllowed              => Status.MethodNotAllowed
      case StatusCodes.NotAcceptable                 => Status.NotAcceptable
      case StatusCodes.ProxyAuthenticationRequired   => Status.ProxyAuthenticationRequired
      case StatusCodes.RequestTimeout                => Status.RequestTimeout
      case StatusCodes.Conflict                      => Status.Conflict
      case StatusCodes.Gone                          => Status.Gone
      case StatusCodes.LengthRequired                => Status.LengthRequired
      case StatusCodes.PreconditionFailed            => Status.PreconditionFailed
      case StatusCodes.RequestEntityTooLarge         => Status.RequestEntityTooLarge
      case StatusCodes.RequestUriTooLong             => Status.RequestUriTooLong
      case StatusCodes.UnsupportedMediaType          => Status.UnsupportedMediaType
      case StatusCodes.RequestedRangeNotSatisfiable  => Status.RequestedRangeNotSatisfiable
      case StatusCodes.ExpectationFailed             => Status.ExpectationFailed
      case StatusCodes.EnhanceYourCalm               => Status.EnhanceYourCalm
      case StatusCodes.UnprocessableEntity           => Status.UnprocessableEntity
      case StatusCodes.Locked                        => Status.Locked
      case StatusCodes.FailedDependency              => Status.FailedDependency
      case StatusCodes.TooEarly                      => Status.TooEarly
      case StatusCodes.UpgradeRequired               => Status.UpgradeRequired
      case StatusCodes.PreconditionRequired          => Status.PreconditionRequired
      case StatusCodes.TooManyRequests               => Status.TooManyRequests
      case StatusCodes.RequestHeaderFieldsTooLarge   => Status.RequestHeaderFieldsTooLarge
      case StatusCodes.RetryWith                     => Status.RetryWith
      case StatusCodes.BlockedByParentalControls     => Status.BlockedByParentalControls
      case StatusCodes.UnavailableForLegalReasons    => Status.UnavailableForLegalReasons
      case StatusCodes.InternalServerError           => Status.InternalServerError
      case StatusCodes.NotImplemented                => Status.NotImplemented
      case StatusCodes.BadGateway                    => Status.BadGateway
      case StatusCodes.ServiceUnavailable            => Status.ServiceUnavailable
      case StatusCodes.GatewayTimeout                => Status.GatewayTimeout
      case StatusCodes.HTTPVersionNotSupported       => Status.HTTPVersionNotSupported
      case StatusCodes.VariantAlsoNegotiates         => Status.VariantAlsoNegotiates
      case StatusCodes.InsufficientStorage           => Status.InsufficientStorage
      case StatusCodes.LoopDetected                  => Status.LoopDetected
      case StatusCodes.BandwidthLimitExceeded        => Status.BandwidthLimitExceeded
      case StatusCodes.NotExtended                   => Status.NotExtended
      case StatusCodes.NetworkAuthenticationRequired => Status.NetworkAuthenticationRequired
      case StatusCodes.NetworkReadTimeout            => Status.NetworkReadTimeout
      case StatusCodes.NetworkConnectTimeout         => Status.NetworkConnectTimeout
      case StatusCodes.ClientError(x)                => Status.custom(x)
      case StatusCodes.CustomStatusCode(x)           => Status.custom(x)
      case StatusCodes.Informational(x)              => Status.custom(x)
      case StatusCodes.Redirection(x)                => Status.custom(x)
      case StatusCodes.ServerError(x)                => Status.custom(x)
      case StatusCodes.Success(x)                    => Status.custom(x)
    }

    λ[HttpF ~> Kleisli[F, HttpExt, *]] {
      case req @ (Options(_) | Get(_) | Head(_) | Post(_) | Put(_) | Delete(_) | Trace(_) | Patch(_)) => doReq(req)
    }
  }

  def mapRequest[F[_]: Async](reqF: HttpF[HttpResponse]): F[AkkaRequest] = {

    def mapContentType(ct: ContentType): F[AkkaContentType] = AkkaContentType.parse(ct.name) match {
      case Left(errors)       => Async[F].raiseError(new Exception(s"Unable to parse content type ${ct.name}, $errors"))
      case Right(contentType) => Async[F].pure(contentType)
    }

    def mapHeaders: F[List[RawHeader]] = reqF.req.headers.map { case (k, v) => RawHeader(k, v) }.toList.pure[F]

    def mapMethod(reqF: HttpF[HttpResponse]): F[HttpMethod] =
      (reqF match {
        case Options(_) => HttpMethods.OPTIONS
        case Get(_)     => HttpMethods.GET
        case Head(_)    => HttpMethods.HEAD
        case Post(_)    => HttpMethods.POST
        case Put(_)     => HttpMethods.PUT
        case Delete(_)  => HttpMethods.DELETE
        case Trace(_)   => HttpMethods.TRACE
        case Patch(_)   => HttpMethods.PATCH
      }).pure[F]

    def mapAkkaRequest(method: HttpMethod, akkaHeaders: List[RawHeader]): F[AkkaRequest] = {
      (reqF.req.entity match {
        case Some(Entity.StringEntity(body, contentType)) =>
          mapContentType(contentType) >>= { ct =>
            new RequestBuilder(method)(Uri(reqF.req.uri.show), HttpEntity.Strict(ct, ByteString.fromString(body)))
              .pure[F]
          }
        case Some(Entity.ByteArrayEntity(body, contentType)) =>
          mapContentType(contentType) >>= { ct =>
            new RequestBuilder(method)(Uri(reqF.req.uri.show), HttpEntity.Strict(ct, ByteString(body)))
              .pure[F]
          }
        case _ => new RequestBuilder(method)(Uri(reqF.req.uri.show)).pure[F]
      }).map(_.withHeaders(akkaHeaders))
    }

    for {
      method      <- mapMethod(reqF)
      akkaHeaders <- mapHeaders
      req         <- mapAkkaRequest(method, akkaHeaders)
    } yield req
  }
}
