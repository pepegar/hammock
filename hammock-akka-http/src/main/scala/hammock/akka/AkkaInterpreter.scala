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
import cats.effect.{Async, IO, Sync}
import cats.implicits._
import hammock.free._
import hammock.free.algebra._

import scala.concurrent.{ExecutionContext, Future}

class AkkaInterpreter[F[_]: Async](
    client: HttpExt)(implicit materializer: ActorMaterializer, executionContext: ExecutionContext)
    extends InterpTrans[F] {

  def trans(implicit S: Sync[F]): HttpRequestF ~> F = transK andThen λ[Kleisli[F, HttpExt, ?] ~> F](_.run(client))

  def transK: HttpRequestF ~> Kleisli[F, HttpExt, ?] =
    λ[HttpRequestF ~> Kleisli[F, HttpExt, ?]] {
      case req @ (Options(_) | Get(_) | Head(_) | Post(_) | Put(_) | Delete(_) | Trace(_)) => doReq(req)
    }

  def doReq(req: HttpRequestF[HttpResponse]): Kleisli[F, HttpExt, HttpResponse] = Kleisli { http =>
    for {
      akkaRequest <- transformRequest(req)
      responseFuture <- Sync[F].delay(
        http
          .singleRequest(akkaRequest)
          .flatMap(transformResponse))
      responseF <- IO.fromFuture(Eval.later(responseFuture)).to[F]
    } yield responseF
  }

  def transformRequest(reqF: HttpRequestF[HttpResponse]): F[AkkaRequest] =
    for {
      method      <- mapMethod(reqF)
      akkaHeaders <- reqF.req.headers.map { case (k, v) => RawHeader(k, v) }.toList.pure[F]
      req <- (reqF.req.entity match {
        case Some(Entity.StringEntity(body, contentType)) =>
          mapContentType(contentType) >>= { ct =>
            new RequestBuilder(method)(Uri(reqF.req.uri.show), HttpEntity.Strict(ct, ByteString.fromString(body)))
              .pure[F]
          }
        case None => new RequestBuilder(method)(Uri(reqF.req.uri.show)).pure[F]
      }).map(_.withHeaders(akkaHeaders))
    } yield req

  def mapMethod(reqF: HttpRequestF[HttpResponse]): F[HttpMethod] =
    (reqF match {
      case Options(_) => HttpMethods.OPTIONS
      case Get(_)     => HttpMethods.GET
      case Head(_)    => HttpMethods.HEAD
      case Post(_)    => HttpMethods.POST
      case Put(_)     => HttpMethods.PUT
      case Delete(_)  => HttpMethods.DELETE
      case Trace(_)   => HttpMethods.TRACE
    }).pure[F]

  def mapContentType(ct: ContentType)(implicit F: Sync[F]): F[AkkaContentType] =
    for {
      parsed <- AkkaContentType.parse(ct.name) match {
        case Left(errors) =>
          F.raiseError(new Exception(s"unable to parse content type ${ct.name}, $errors"))
        case Right(contentType) =>
          F.pure(contentType)
      }
    } yield parsed

  def transformResponse(akkaResp: AkkaResponse): Future[HttpResponse] = {
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
    case StatusCodes.UnorderedCollection           => Status.UnorderedCollection
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
    // case StatusCodes.ClientError(x) => Status.ClientError(x)
    // case StatusCodes.CustomStatusCode(x) => Status.CustomStatusCode(x)
    // case StatusCodes.Informational(x) => Status.Informational(x)
    // case StatusCodes.Redirection(x) => Status.Redirection(x)
    // case StatusCodes.ServerError(x) => Status.ServerError(x)
    // case StatusCodes.Success(x) => Status.Success(x)
  }
}
