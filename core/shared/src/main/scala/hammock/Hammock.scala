package hammock

import cats.syntax.show._
import cats.free.Free

import hi.Opts
import Encoder.ops._

object Hammock {

  /** Creates an [[HttpF]] and from the
   * [[Method]], [[Uri]], and [[Map[String, String] headers]].  It
   * can be later executed via an interpreter.
   */
  def request(method: Method, uri: Uri, headers: Map[String, String]): Free[HttpF, HttpResponse] = method match {
    case Method.OPTIONS => Ops.options(uri, headers)
    case Method.GET     => Ops.get(uri, headers)
    case Method.HEAD    => Ops.head(uri, headers)
    case Method.POST    => Ops.post(uri, headers, None)
    case Method.PUT     => Ops.put(uri, headers, None)
    case Method.DELETE  => Ops.delete(uri, headers)
    case Method.TRACE   => Ops.trace(uri, headers)
    case Method.PATCH   => Ops.patch(uri, headers, None)
  }

  /** similar to [[request]], but you can pass it a
   * body when it exists an instance for the [[Encoder]]
   * typeclass for the given type [[A]]
   */
  def request[A: Encoder](
      method: Method,
      uri: Uri,
      headers: Map[String, String],
      body: Option[A]): Free[HttpF, HttpResponse] = method match {
    case Method.OPTIONS => Ops.options(uri, headers)
    case Method.GET     => Ops.get(uri, headers)
    case Method.HEAD    => Ops.head(uri, headers)
    case Method.POST    => Ops.post(uri, headers, body.map(x => x.encode))
    case Method.PUT     => Ops.put(uri, headers, body.map(x => x.encode))
    case Method.DELETE  => Ops.delete(uri, headers)
    case Method.TRACE   => Ops.trace(uri, headers)
    case Method.PATCH   => Ops.patch(uri, headers, body.map(x => x.encode))
  }

  /** Creates a request value given a [[Method method]], [[Uri uri]], and [[hi.Opts opts]], and suspends it into a [[cats.free.Free]].
   *
   * Usage:
   *
   * {{{
   * scala> import hammock._, hammock.jvm.Interpreter, hammock.hi._, hammock.hi._, cats._, cats.implicits._, scala.util.Try
   * import hammock._
   * import hammock.jvm.Interpreter
   * import hammock.hi._
   * import cats._
   * import cats.implicits._
   * import scala.util.Try
   *
   * scala> val opts = (header("X-Test" -> "works!") >>> auth(Auth.BasicAuth("user", "pass")) >>> cookie(Cookie("key", "value")))(Opts.empty)
   * opts: hammock.hi.Opts = Opts(Some(BasicAuth(user,pass)),Map(X-Test -> works!),Some(List(Cookie(key,value,None,None,None,None,None,None,None,None))))
   *
   * scala> val response = Hammock.withOpts(Method.GET, uri"http://httpbin.org/get", opts)
   * response: Free[HttpF, hammock.HttpResponse] = Free(...)
   * }}}
   *
   */
  def withOpts(method: Method, uri: Uri, opts: Opts): Free[HttpF, HttpResponse] =
    request(method, uri, constructHeaders(opts))

  /** Variant of [[withOpts]] methods that also takes an optional body
   * of a request. There should be a Encoder instance for the body type
   * for this to work.
   *
   * @see Hammock.withOpts
   */
  def withOpts[A: Encoder](method: Method, uri: Uri, opts: Opts, body: Option[A]): Free[HttpF, HttpResponse] =
    request(method, uri, constructHeaders(opts), body)

  /** Creates an OPTIONS request to the given [[Uri uri]] and [[hi.Opts opts]].
   *
   * {{{
   * scala> import hammock._, hammock.jvm.Interpreter, hammock.hi._, hammock.hi._, cats._, cats.implicits._, scala.util.Try
   * import hammock._
   * import hammock.jvm.Interpreter
   * import hammock.hi._
   * import cats._
   * import cats.implicits._
   * import scala.util.Try
   *
   * scala> val opts = (header("X-Test" -> "works!") >>> auth(Auth.BasicAuth("user", "pass")) >>> cookie(Cookie("key", "value")))(Opts.empty)
   * opts: hammock.hi.Opts = Opts(Some(BasicAuth(user,pass)),Map(X-Test -> works!),Some(List(Cookie(key,value,None,None,None,None,None,None,None,None))))
   *
   * scala> Hammock.optionsWithOpts(uri"http://httpbin.org/get", opts)
   * res1: Free[HttpF, hammock.HttpResponse] = Free(...)
   * }}}
   *
   */
  def optionsWithOpts(uri: Uri, opts: Opts): Free[HttpF, HttpResponse] = withOpts(Method.OPTIONS, uri, opts)

  /** Creates a GET request to the given [[Uri uri]] and [[hi.Opts opts]].
   *
   * {{{
   * scala> import hammock._, hammock.jvm.Interpreter, hammock.hi._, hammock.hi._, cats._, cats.implicits._, scala.util.Try
   * import hammock._
   * import hammock.jvm.Interpreter
   * import hammock.hi._
   * import cats._
   * import cats.implicits._
   * import scala.util.Try
   *
   * scala> val opts = (header("X-Test" -> "works!") >>> auth(Auth.BasicAuth("user", "pass")) >>> cookie(Cookie("key", "value")))(Opts.empty)
   * opts: hammock.hi.Opts = Opts(Some(BasicAuth(user,pass)),Map(X-Test -> works!),Some(List(Cookie(key,value,None,None,None,None,None,None,None,None))))
   *
   * scala> Hammock.getWithOpts(uri"http://httpbin.org/get", opts)
   * res1: Free[HttpF, hammock.HttpResponse] = Free(...)
   * }}}
   *
   */
  def getWithOpts(uri: Uri, opts: Opts): Free[HttpF, HttpResponse] = withOpts(Method.GET, uri, opts)

  /** Creates a HEAD request to the given [[Uri uri]] and [[hi.Opts opts]].
   *
   * {{{
   * import hammock._
   * import hammock.jvm.Interpreter
   * import hammock.hi._
   * import cats._
   * import cats.implicits._
   * import scala.util.Try
   *
   * val opts = (header("X-Test" -> "works!") >>> auth(Auth.BasicAuth("user", "pass")) >>> cookie(Cookie("key", "value")))(Opts.empty)
   *
   * Hammock.headWithOpts(uri"http://httpbin.org/get", opts)
   * }}}
   *
   */
  def headWithOpts(uri: Uri, opts: Opts): Free[HttpF, HttpResponse] = withOpts(Method.HEAD, uri, opts)

  /** Creates a POST request to the given [[Uri uri]] and [[hi.Opts opts]].
   * It also has an optional body parameter that can be
   * used.
   *
   * {{{
   * import hammock._
   * import hammock.jvm.Interpreter
   * import hammock.hi._
   * import cats._
   * import cats.implicits._
   * import scala.util.Try
   *
   * val opts = (header("X-Test" -> "works!") >>> auth(Auth.BasicAuth("user", "pass")) >>> cookie(Cookie("key", "value")))(Opts.empty)
   *
   * implicit val stringEncoder = new Encoder[String] {
   *    def encode(s: String) = s
   * }
   *
   * Hammock.postWithOpts(uri"http://httpbin.org/get", opts, Some("""{"body": true}"""))
   * }}}
   */
  def postWithOpts[A: Encoder](uri: Uri, opts: Opts, body: Option[A] = None): Free[HttpF, HttpResponse] =
    withOpts(Method.POST, uri, opts, body)

  /** Creates a PUT request to the given [[Uri uri]] and [[hi.Opts opts]].
   * It also has an optional body parameter that can be
   * used.
   *
   * {{{
   * import hammock._
   * import hammock.jvm.Interpreter
   * import hammock.hi._
   * import cats._
   * import cats.implicits._
   * import scala.util.Try
   *
   * val opts = (header("X-Test" -> "works!") >>> auth(Auth.BasicAuth("user", "pass")) >>> cookie(Cookie("key", "value")))(Opts.empty)
   *
   * implicit val stringEncoder = new Encoder[String] {
   *    def encode(s: String) = s
   * }
   *
   * Hammock.postWithOpts(uri"http://httpbin.org/get", opts, Some("""{"body": true}"""))
   * }}}
   *
   */
  def putWithOpts[A: Encoder](uri: Uri, opts: Opts, body: Option[A] = None): Free[HttpF, HttpResponse] =
    withOpts(Method.PUT, uri, opts, body)

  /** Creates a DELETE request to the given [[Uri uri]] and [[hi.Opts opts]].
   *
   * {{{
   * val opts = (header("X-Test" -> "works!") >>> auth(Auth.BasicAuth("user", "pass")) >>> cookie(Cookie("key", "value")))(Opts.empty)
   *
   * Hammock.deleteWithOpts(uri"http://httpbin.org/get", opts)
   * }}}
   *
   */
  def deleteWithOpts(uri: Uri, opts: Opts): Free[HttpF, HttpResponse] = withOpts(Method.DELETE, uri, opts)

  /** Creates a TRACE request to the given [[Uri uri]] and [[hi.Opts opts]].
   *
   * {{{
   * val opts = (header("X-Test" -> "works!") >>> auth(Auth.BasicAuth("user", "pass")) >>> cookie(Cookie("key", "value")))(Opts.empty)
   *
   * Hammock.traceWithOpts(uri"http://httpbin.org/get", opts)
   * }}}
   *
   */
  def traceWithOpts(uri: Uri, opts: Opts): Free[HttpF, HttpResponse] = withOpts(Method.TRACE, uri, opts)

  /** Creates a PATCH request to the given [[Uri uri]] and [[hi.Opts opts]].
    * It also has an optional body parameter that can be
    * used.
    *
    * {{{
    * import hammock._
    * import hammock.jvm.Interpreter
    * import hammock.hi._
    * import cats._
    * import cats.implicits._
    * import scala.util.Try
    *
    * val opts = (header("X-Test" -> "works!") >>> auth(Auth.BasicAuth("user", "pass")) >>> cookie(Cookie("key", "value")))(Opts.empty)
    *
    * implicit val stringCodec = new Codec[String] {
    *    def encode(s: String) = s
    *    def decode(s: String) = Right(s)
    * }
    *
    * Hammock.patchWithOpts(uri"http://httpbin.org/patch", opts, Some("""{"body": true}"""))
    * }}}
    *
    */
  def patchWithOpts[A: Codec](uri: Uri, opts: Opts, body: Option[A] = None): Free[HttpF, HttpResponse] =
    withOpts(Method.PATCH, uri, opts, body)

  private def constructHeaders(opts: Opts) =
    opts.headers ++
      opts.cookies.map(_.map(cookie => "Set-Cookie" -> cookie.show)).getOrElse(Map()) ++
      opts.auth.map(auth => Map("Authentication"    -> auth.show)).getOrElse(Map())
}
