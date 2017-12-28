package hammock

import monocle.macros.Lenses

@Lenses case class Status(code: Int, text: String, description: String)

object Status {
  val Continue = Status(
    100,
    "Continue",
    "The server has received the request headers, and the client should proceed to send the request body.")
  val SwitchingProtocols =
    Status(101, "Switching Protocols", "The server is switching protocols, because the client requested the switch.")
  val Processing = Status(102, "Processing", "The server is processing the request, but no response is available yet.")
  val OK         = Status(200, "OK", "OK")
  val Created    = Status(201, "Created", "The request has been fulfilled and resulted in a new resource being created.")
  val Accepted =
    Status(202, "Accepted", "The request has been accepted for processing, but the processing has not been completed.")
  val NonAuthoritativeInformation = Status(
    203,
    "Non-Authoritative Information",
    "The server successfully processed the request, but is returning information that may be from another source.")
  val NoContent =
    Status(204, "No Content", "The server successfully processed the request and is not returning any content.")
  val ResetContent =
    Status(205, "Reset Content", "The server successfully processed the request, but is not returning any content.")
  val PartialContent = Status(
    206,
    "Partial Content",
    "The server is delivering only part of the resource due to a range header sent by the client.")
  val MultiStatus = Status(
    207,
    "Multi-Status",
    "The message body that follows is an XML message and can contain a number of separate response codes, depending on how many sub-requests were made."
  )
  val AlreadyReported = Status(
    208,
    "Already Reported",
    "The members of a DAV binding have already been enumerated in a previous reply to this request, and are not being included again."
  )
  val IMUsed = Status(
    226,
    "IM Used",
    "The server has fulfilled a GET request for the resource, and the response is a representation of the result of one or more instance-manipulations applied to the current instance."
  )
  val MultipleChoices = Status(300, "Multiple Choices", "")
  val MovedPermanently =
    Status(301, "Moved Permanently", "This and all future requests should be directed to the given URI.")
  val Found = Status(302, "Found", "The resource was found, but at a different URI.")
  val SeeOther =
    Status(303, "See Other", "The response to the request can be found under another URI using a GET method.")
  val NotModified = Status(304, "Not Modified", "The resource has not been modified since last requested.")
  val UseProxy =
    Status(305, "Use Proxy", "This single request is to be repeated via the proxy given by the Location field.")
  val TemporaryRedirect = Status(
    307,
    "Temporary Redirect",
    "The request should be repeated with another URI, but future requests can still use the original URI.")
  val PermanentRedirect =
    Status(308, "Permanent Redirect", "The request, and all future requests should be repeated using another URI.")
  val BadRequest      = Status(400, "Bad Request", "The request contains bad syntax or cannot be fulfilled.")
  val Unauthorized    = Status(401, "Unauthorized", "Authentication is possible but has failed or not yet been provided.")
  val PaymentRequired = Status(402, "Payment Required", "Reserved for future use.")
  val Forbidden =
    Status(403, "Forbidden", "The request was a legal request, but the server is refusing to respond to it.")
  val NotFound =
    Status(404, "Not Found", "The requested resource could not be found but may be available again in the future.")
  val MethodNotAllowed = Status(
    405,
    "Method Not Allowed",
    "A request was made of a resource using a request method not supported by that resource;")
  val NotAcceptable = Status(
    406,
    "Not Acceptable",
    "The requested resource is only capable of generating content not acceptable according to the Accept headers sent in the request."
  )
  val ProxyAuthenticationRequired =
    Status(407, "Proxy Authentication Required", "Proxy authentication is required to access the requested resource.")
  val RequestTimeout = Status(408, "Request Timeout", "The server timed out waiting for the request.")
  val Conflict = Status(
    409,
    "Conflict",
    "The request could not be processed because of conflict in the request, such as an edit conflict.")
  val Gone = Status(410, "Gone", "The resource requested is no longer available and will not be available again.")
  val LengthRequired = Status(
    411,
    "Length Required",
    "The request did not specify the length of its content, which is required by the requested resource.")
  val PreconditionFailed = Status(
    412,
    "Precondition Failed",
    "The server does not meet one of the preconditions that the requester put on the request.")
  val RequestEntityTooLarge =
    Status(413, "Request Entity Too Large", "The request is larger than the server is willing or able to process.")
  val RequestUriTooLong =
    Status(414, "Request-URI Too Long", "The URI provided was too long for the server to process.")
  val UnsupportedMediaType = Status(
    415,
    "Unsupported Media Type",
    "The request entity has a media type which the server or resource does not support.")
  val RequestedRangeNotSatisfiable = Status(
    416,
    "Requested Range Not Satisfiable",
    "The client has asked for a portion of the file, but the server cannot supply that portion.")
  val ExpectationFailed =
    Status(417, "Expectation Failed", "The server cannot meet the requirements of the Expect request-header field.")
  val EnhanceYourCalm = Status(420, "Enhance Your Calm", "You are being rate-limited.")
  val UnprocessableEntity = Status(
    422,
    "Unprocessable Entity",
    "The request was well-formed but was unable to be followed due to semantic errors.")
  val Locked               = Status(423, "Locked", "The resource that is being accessed is locked.")
  val FailedDependency     = Status(424, "Failed Dependency", "The request failed due to failure of a previous request.")
  val UnorderedCollection  = Status(425, "Unordered Collection", "The collection is unordered.")
  val UpgradeRequired      = Status(426, "Upgrade Required", "The client should switch to a different protocol.")
  val PreconditionRequired = Status(428, "Precondition Required", "The server requires the request to be conditional.")
  val TooManyRequests =
    Status(429, "Too Many Requests", "The user has sent too many requests in a given amount of time.")
  val RequestHeaderFieldsTooLarge = Status(
    431,
    "Request Header Fields Too Large",
    "The server is unwilling to process the request because either an individual header field, or all the header fields collectively, are too large."
  )
  val RetryWith = Status(449, "Retry With", "The request should be retried after doing the appropriate action.")
  val BlockedByParentalControls = Status(
    450,
    "Blocked by Windows Parental Controls",
    "Windows Parental Controls are turned on and are blocking access to the given webpage.")
  val UnavailableForLegalReasons =
    Status(451, "Unavailable For Legal Reasons", "Resource access is denied for legal reasons.")
  val InternalServerError = Status(500, "Internal Server Error", "There was an internal server error.")
  val NotImplemented = Status(
    501,
    "Not Implemented",
    "The server either does not recognize the request method, or it lacks the ability to fulfill the request.")
  val BadGateway = Status(
    502,
    "Bad Gateway",
    "The server was acting as a gateway or proxy and received an invalid response from the upstream server.")
  val ServiceUnavailable = Status(
    503,
    "Service Unavailable",
    "The server is currently unavailable (because it is overloaded or down for maintenance).")
  val GatewayTimeout = Status(
    504,
    "Gateway Timeout",
    "The server was acting as a gateway or proxy and did not receive a timely response from the upstream server.")
  val HTTPVersionNotSupported = Status(
    505,
    "HTTP Version Not Supported",
    "The server does not support the HTTP protocol version used in the request.")
  val VariantAlsoNegotiates = Status(
    506,
    "Variant Also Negotiates",
    "Transparent content negotiation for the request, results in a circular reference.")
  val InsufficientStorage    = Status(507, "Insufficient Storage", "Insufficient storage to complete the request.")
  val LoopDetected           = Status(508, "Loop Detected", "The server detected an infinite loop while processing the request.")
  val BandwidthLimitExceeded = Status(509, "Bandwidth Limit Exceeded", "Bandwidth limit has been exceeded.")
  val NotExtended =
    Status(510, "Not Extended", "Further extensions to the request are required for the server to fulfill it.")
  val NetworkAuthenticationRequired =
    Status(511, "Network Authentication Required", "The client needs to authenticate to gain network access.")
  val NetworkReadTimeout    = Status(598, "Network read timeout error", "")
  val NetworkConnectTimeout = Status(599, "Network connect timeout error", "")

  def custom(x: Int, text: String = "", description: String = "") = Status(x, text, description)

  val Statuses = Map(
    100 -> Continue,
    101 -> SwitchingProtocols,
    102 -> Processing,
    200 -> OK,
    201 -> Created,
    202 -> Accepted,
    203 -> NonAuthoritativeInformation,
    204 -> NoContent,
    205 -> ResetContent,
    206 -> PartialContent,
    207 -> MultiStatus,
    208 -> AlreadyReported,
    226 -> IMUsed,
    300 -> MultipleChoices,
    301 -> MovedPermanently,
    302 -> Found,
    303 -> SeeOther,
    304 -> NotModified,
    305 -> UseProxy,
    307 -> TemporaryRedirect,
    308 -> PermanentRedirect,
    400 -> BadRequest,
    401 -> Unauthorized,
    402 -> PaymentRequired,
    403 -> Forbidden,
    404 -> NotFound,
    405 -> MethodNotAllowed,
    406 -> NotAcceptable,
    407 -> ProxyAuthenticationRequired,
    408 -> RequestTimeout,
    409 -> Conflict,
    410 -> Gone,
    411 -> LengthRequired,
    412 -> PreconditionFailed,
    413 -> RequestEntityTooLarge,
    414 -> RequestUriTooLong,
    415 -> UnsupportedMediaType,
    416 -> RequestedRangeNotSatisfiable,
    417 -> ExpectationFailed,
    420 -> EnhanceYourCalm,
    422 -> UnprocessableEntity,
    423 -> Locked,
    424 -> FailedDependency,
    425 -> UnorderedCollection,
    426 -> UpgradeRequired,
    428 -> PreconditionRequired,
    429 -> TooManyRequests,
    431 -> RequestHeaderFieldsTooLarge,
    449 -> RetryWith,
    450 -> BlockedByParentalControls,
    451 -> UnavailableForLegalReasons,
    500 -> InternalServerError,
    501 -> NotImplemented,
    502 -> BadGateway,
    503 -> ServiceUnavailable,
    504 -> GatewayTimeout,
    505 -> HTTPVersionNotSupported,
    506 -> VariantAlsoNegotiates,
    507 -> InsufficientStorage,
    508 -> LoopDetected,
    509 -> BandwidthLimitExceeded,
    510 -> NotExtended,
    511 -> NetworkAuthenticationRequired,
    598 -> NetworkReadTimeout,
    599 -> NetworkConnectTimeout
  )

  def get(code: Int): Status = Statuses.getOrElse(code, Status(code, "Undefined", "Undefined StatusCode"))
}
