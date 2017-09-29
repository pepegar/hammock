package hammock
package akka

import _root_.akka.http
import _root_.akka.actor.ActorSystem
import _root_.akka.http.scaladsl.{Http, HttpExt}
import _root_.akka.http.scaladsl.model.{
  HttpResponse => AkkaResponse,
  HttpRequest => AkkaRequest,
  StatusCode => AkkaStatus,
  _
}
import _root_.akka.stream.ActorMaterializer
import _root_.akka.http.scaladsl.model.HttpMethods
import _root_.akka.http.scaladsl.client.RequestBuilding.RequestBuilder
import _root_.akka.util.ByteString
import scala.concurrent.{ExecutionContext, Future}

import cats.{~>, Eval}
import cats.syntax.show._
import cats.data.Kleisli
import cats.effect.{Async, IO, Sync}

import hammock.free._
import hammock.free.algebra._

class AkkaInterpreter[F[_]: Async](
    client: HttpExt)(implicit system: ActorSystem, materializer: ActorMaterializer, executionContext: ExecutionContext)
    extends InterpTrans[F] {

  def trans(implicit S: Sync[F]): HttpRequestF ~> F = transK andThen λ[Kleisli[F, HttpExt, ?] ~> F](_.run(client))

  def transK(implicit S: Sync[F]): HttpRequestF ~> Kleisli[F, HttpExt, ?] =
    λ[HttpRequestF ~> Kleisli[F, HttpExt, ?]]({
      case req @ Options(uri, headers)    => doReq(req)
      case req @ Get(uri, headers)        => doReq(req)
      case req @ Head(uri, headers)       => doReq(req)
      case req @ Post(uri, headers, body) => doReq(req)
      case req @ Put(uri, headers, body)  => doReq(req)
      case req @ Delete(uri, headers)     => doReq(req)
      case req @ Trace(uri, headers)      => doReq(req)
    })

  def doReq(req: HttpRequestF[HttpResponse]): Kleisli[F, HttpExt, HttpResponse] = Kleisli { http =>
    val akkaRequest = transformRequest(req)

    val responseFuture = http
      .singleRequest(akkaRequest)
      .flatMap(transformResponse)

    IO.fromFuture(Eval.later(responseFuture)).to[F]
  }

  def transformRequest(req: HttpRequestF[HttpResponse]): AkkaRequest = {
    val method = req.method match {
      case Method.OPTIONS => HttpMethods.OPTIONS
      case Method.GET     => HttpMethods.GET
      case Method.HEAD    => HttpMethods.HEAD
      case Method.POST    => HttpMethods.POST
      case Method.PUT     => HttpMethods.PUT
      case Method.DELETE  => HttpMethods.DELETE
      case Method.TRACE   => HttpMethods.TRACE
      case Method.CONNECT => HttpMethods.CONNECT
    }

    req.body match {
      case Some(body) =>
        new RequestBuilder(method)(
          Uri(req.uri.show),
          HttpEntity.Strict(ContentTypes.`application/json`, ByteString.fromString(body)))
      case None => new RequestBuilder(method)(Uri(req.uri.show))
    }
  }

  def transformResponse(akkaResp: AkkaResponse): Future[HttpResponse] = {
    val status  = mapStatus(akkaResp.status)
    val headers = akkaResp.headers.map(h => (h.name, h.value)).toMap
    akkaResp.entity.dataBytes
      .runFold(ByteString(""))(_ ++ _)
      .map(_.utf8String)
      .map(body => HttpResponse(status, headers, body))
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
