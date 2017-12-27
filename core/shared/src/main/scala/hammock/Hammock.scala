package hammock

import cats.syntax.show._

import free.algebra.{HttpRequestIO, Ops}
import hi.Opts
import Codec.ops._

object Hammock {

  /** Create an [[hammock.HttpRequestIO[HttpResponse]]] from the
   * [[Method]], [[Uri]], and [[Map[String, String] headers]].  It
   * can be later executed via an interpreter.
   */
  def request(method: Method, uri: Uri, headers: Map[String, String]): HttpRequestIO[HttpResponse] = method match {
    case Method.OPTIONS => Ops.options(uri, headers)
    case Method.GET     => Ops.get(uri, headers)
    case Method.HEAD    => Ops.head(uri, headers)
    case Method.POST    => Ops.post(uri, headers, None)
    case Method.PUT     => Ops.put(uri, headers, None)
    case Method.DELETE  => Ops.delete(uri, headers)
    case Method.TRACE   => Ops.trace(uri, headers)
  }

  /** Create an [[hammock.HttpRequestIO]] as well, but you can pass it a
   * [[Option[A] body]] when it exists an instance for the [[Codec]]
   * typeclass for the given type [[A]]
   */
  def request[A: Codec](
      method: Method,
      uri: Uri,
      headers: Map[String, String],
      body: Option[A]): HttpRequestIO[HttpResponse] = method match {
    case Method.OPTIONS => Ops.options(uri, headers)
    case Method.GET     => Ops.get(uri, headers)
    case Method.HEAD    => Ops.head(uri, headers)
    case Method.POST    => Ops.post(uri, headers, body.map(x => x.encode))
    case Method.PUT     => Ops.put(uri, headers, body.map(x => x.encode))
    case Method.DELETE  => Ops.delete(uri, headers)
    case Method.TRACE   => Ops.trace(uri, headers)
  }

  /** Creates a HttpRequestIO[HttpResponse] value given a [[Method
   * method]], [[Uri uri]], and [[Opts opts]].
   *
   * Usage:
   *
   * {{{
   * scala> import hammock._, hammock.jvm.free.Interpreter, hammock.hi._, hammock.hi.dsl._, cats._, cats.implicits._, scala.util.Try
   * import hammock._
   * import hammock.jvm.free.Interpreter
   * import hammock.hi._
   * import hammock.hi.dsl._
   * import cats._
   * import cats.implicits._
   * import scala.util.Try
   *
   * scala> val opts = (header("X-Test" -> "works!") &> auth(Auth.BasicAuth("user", "pass")) &> cookie(Cookie("key", "value")))(Opts.empty)
   * opts: hammock.hi.Opts = Opts(Some(BasicAuth(user,pass)),Map(X-Test -> works!),Some(List(Cookie(key,value,None,None,None,None,None,None,None,None))))
   *
   * scala> val response = Hammock.withOpts(Method.GET, Uri.unsafeParse("http://httpbin.org/get"), opts)
   * response: hammock.free.algebra.HttpRequestIO[hammock.HttpResponse] = Free(...)
   * }}}
   *
   */
  def withOpts(method: Method, uri: Uri, opts: Opts): HttpRequestIO[HttpResponse] =
    request(method, uri, constructHeaders(opts))

  /** Variant of [[withOpts]] methods that also takes an optional body
   * of a request. There should be a Codec instance for the body type
   * for this to work.
   *
   * @see Hammock.withOpts
   */
  def withOpts[A: Codec](method: Method, uri: Uri, opts: Opts, body: Option[A]): HttpRequestIO[HttpResponse] =
    request(method, uri, constructHeaders(opts), body)

  /** Create an OPTIONS request to the given [[Uri uri]] and [[Opts
   * opts]].
   *
   * {{{
   * scala> import hammock._, hammock.jvm.free.Interpreter, hammock.hi._, hammock.hi.dsl._, cats._, cats.implicits._, scala.util.Try
   * import hammock._
   * import hammock.jvm.free.Interpreter
   * import hammock.hi._
   * import hammock.hi.dsl._
   * import cats._
   * import cats.implicits._
   * import scala.util.Try
   *
   * scala> val opts = (header("X-Test" -> "works!") &> auth(Auth.BasicAuth("user", "pass")) &> cookie(Cookie("key", "value")))(Opts.empty)
   * opts: hammock.hi.Opts = Opts(Some(BasicAuth(user,pass)),Map(X-Test -> works!),Some(List(Cookie(key,value,None,None,None,None,None,None,None,None))))
   *
   * scala> Hammock.optionsWithOpts(Uri.unsafeParse("http://httpbin.org/get"), opts)
   * res1: hammock.free.algebra.HttpRequestIO[hammock.HttpResponse] = Free(...)
   * }}}
   *
   */
  def optionsWithOpts(uri: Uri, opts: Opts): HttpRequestIO[HttpResponse] = withOpts(Method.OPTIONS, uri, opts)

  /** Create an GET request to the given [[Uri uri]] and [[Opts
   * opts]].
   *
   * {{{
   * scala> import hammock._, hammock.jvm.free.Interpreter, hammock.hi._, hammock.hi.dsl._, cats._, cats.implicits._, scala.util.Try
   * import hammock._
   * import hammock.jvm.free.Interpreter
   * import hammock.hi._
   * import hammock.hi.dsl._
   * import cats._
   * import cats.implicits._
   * import scala.util.Try
   *
   * scala> val opts = (header("X-Test" -> "works!") &> auth(Auth.BasicAuth("user", "pass")) &> cookie(Cookie("key", "value")))(Opts.empty)
   * opts: hammock.hi.Opts = Opts(Some(BasicAuth(user,pass)),Map(X-Test -> works!),Some(List(Cookie(key,value,None,None,None,None,None,None,None,None))))
   *
   * scala> Hammock.getWithOpts(Uri.unsafeParse("http://httpbin.org/get"), opts)
   * res1: hammock.free.algebra.HttpRequestIO[hammock.HttpResponse] = Free(...)
   * }}}
   *
   */
  def getWithOpts(uri: Uri, opts: Opts): HttpRequestIO[HttpResponse] = withOpts(Method.GET, uri, opts)

  /** Create an HEAD request to the given [[Uri uri]] and [[Opts
   * opts]].
   *
   * {{{
   * scala> import hammock._, hammock.jvm.free.Interpreter, hammock.hi._, hammock.hi.dsl._, cats._, cats.implicits._, scala.util.Try
   * import hammock._
   * import hammock.jvm.free.Interpreter
   * import hammock.hi._
   * import hammock.hi.dsl._
   * import cats._
   * import cats.implicits._
   * import scala.util.Try
   *
   * scala> val opts = (header("X-Test" -> "works!") &> auth(Auth.BasicAuth("user", "pass")) &> cookie(Cookie("key", "value")))(Opts.empty)
   * opts: hammock.hi.Opts = Opts(Some(BasicAuth(user,pass)),Map(X-Test -> works!),Some(List(Cookie(key,value,None,None,None,None,None,None,None,None))))
   *
   * scala> Hammock.headWithOpts(Uri.unsafeParse("http://httpbin.org/get"), opts)
   * res1: hammock.free.algebra.HttpRequestIO[hammock.HttpResponse] = Free(...)
   * }}}
   *
   */
  def headWithOpts(uri: Uri, opts: Opts): HttpRequestIO[HttpResponse] = withOpts(Method.HEAD, uri, opts)

  /** Create an POST request to the given [[Uri uri]] and [[Opts opts]].
   * It also has an optional [[Option[A] body]] parameter that can be
   * used.
   *
   * {{{
   * scala> import hammock._, hammock.jvm.free.Interpreter, hammock.hi._, hammock.hi.dsl._, cats._, cats.implicits._, scala.util.Try
   * import hammock._
   * import hammock.jvm.free.Interpreter
   * import hammock.hi._
   * import hammock.hi.dsl._
   * import cats._
   * import cats.implicits._
   * import scala.util.Try
   *
   * scala> val opts = (header("X-Test" -> "works!") &> auth(Auth.BasicAuth("user", "pass")) &> cookie(Cookie("key", "value")))(Opts.empty)
   * opts: hammock.hi.Opts = Opts(Some(BasicAuth(user,pass)),Map(X-Test -> works!),Some(List(Cookie(key,value,None,None,None,None,None,None,None,None))))
   *
   * scala> implicit val stringCodec = new Codec[String] {
   *      |   def encode(s: String) = s
   *      |   def decode(s: String) = Right(s)
   *      | }
   * stringCodec: hammock.Codec[String]{def decode(s: String): scala.util.Right[Nothing,String]} = $anon$1@1533f96a
   *
   * scala> Hammock.postWithOpts(Uri.unsafeParse("http://httpbin.org/get"), opts, Some("""{"body": true}"""))
   * res1: hammock.free.algebra.HttpRequestIO[hammock.HttpResponse] = Free(...)
   * }}}
   *
   */
  def postWithOpts[A: Codec](uri: Uri, opts: Opts, body: Option[A] = None): HttpRequestIO[HttpResponse] =
    withOpts(Method.POST, uri, opts, body)

  /** Create an PUT request to the given [[Uri uri]] and [[Opts opts]].
   * It also has an optional [[Option[A] body]] parameter that can be
   * used.
   *
   * {{{
   * scala> import hammock._, hammock.jvm.free.Interpreter, hammock.hi._, hammock.hi.dsl._, cats._, cats.implicits._, scala.util.Try
   * import hammock._
   * import hammock.jvm.free.Interpreter
   * import hammock.hi._
   * import hammock.hi.dsl._
   * import cats._
   * import cats.implicits._
   * import scala.util.Try
   *
   * scala> val opts = (header("X-Test" -> "works!") &> auth(Auth.BasicAuth("user", "pass")) &> cookie(Cookie("key", "value")))(Opts.empty)
   * opts: hammock.hi.Opts = Opts(Some(BasicAuth(user,pass)),Map(X-Test -> works!),Some(List(Cookie(key,value,None,None,None,None,None,None,None,None))))
   *
   * scala> implicit val stringCodec = new Codec[String] {
   *      |   def encode(s: String) = s
   *      |   def decode(s: String) = Right(s)
   *      | }
   * stringCodec: hammock.Codec[String]{def decode(s: String): scala.util.Right[Nothing,String]} = $anon$1@1533f96a
   *
   * scala> Hammock.putWithOpts(Uri.unsafeParse("http://httpbin.org/get"), opts, Some("""{"body": true}"""))
   * res1: hammock.free.algebra.HttpRequestIO[hammock.HttpResponse] = Free(...)
   * }}}
   *
   */
  def putWithOpts[A: Codec](uri: Uri, opts: Opts, body: Option[A] = None): HttpRequestIO[HttpResponse] =
    withOpts(Method.PUT, uri, opts, body)

  /** Create an DELETE request to the given [[Uri uri]] and [[Opts
   * opts]].
   *
   * {{{
   * scala> val opts = (header("X-Test" -> "works!") &> auth(Auth.BasicAuth("user", "pass")) &> cookie(Cookie("key", "value")))(Opts.empty)
   * opts: hammock.hi.Opts = Opts(Some(BasicAuth(user,pass)),Map(X-Test -> works!),Some(List(Cookie(key,value,None,None,None,None,None,None,None,None))))
   *
   * scala> Hammock.deleteWithOpts(Uri.unsafeParse("http://httpbin.org/get"), opts)
   * res1: hammock.free.algebra.HttpRequestIO[hammock.HttpResponse] = Free(...)
   * }}}
   *
   */
  def deleteWithOpts(uri: Uri, opts: Opts): HttpRequestIO[HttpResponse] = withOpts(Method.DELETE, uri, opts)

  /** Create an TRACE request to the given [[Uri uri]] and [[Opts
   * opts]].
   *
   * {{{
   * scala> val opts = (header("X-Test" -> "works!") &> auth(Auth.BasicAuth("user", "pass")) &> cookie(Cookie("key", "value")))(Opts.empty)
   * opts: hammock.hi.Opts = Opts(Some(BasicAuth(user,pass)),Map(X-Test -> works!),Some(List(Cookie(key,value,None,None,None,None,None,None,None,None))))
   *
   * scala> Hammock.traceWithOpts(Uri.unsafeParse("http://httpbin.org/get"), opts)
   * res1: hammock.free.algebra.HttpRequestIO[hammock.HttpResponse] = Free(...)
   * }}}
   *
   */
  def traceWithOpts(uri: Uri, opts: Opts): HttpRequestIO[HttpResponse] = withOpts(Method.TRACE, uri, opts)

  private def constructHeaders(opts: Opts) =
    opts.headers ++
      opts.cookies.map(_.map(cookie => "Set-Cookie" -> cookie.show)).getOrElse(Map()) ++
      opts.auth.map(auth => Map("Authentication"    -> auth.show)).getOrElse(Map())
}
