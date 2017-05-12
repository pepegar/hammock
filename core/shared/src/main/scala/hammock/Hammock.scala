package hammock

import cats._
import cats.arrow._
import cats.syntax.show._

import free._
import free.algebra.{ HttpRequestIO, Ops }
import hi.Opts
import Codec._

object Hammock {

  def request(method: Method, uri: Uri, headers: Map[String, String]): HttpRequestIO[HttpResponse] = method match {
    case Method.OPTIONS => Ops.options(uri, headers)
    case Method.GET => Ops.get(uri, headers)
    case Method.HEAD => Ops.head(uri, headers)
    case Method.POST => Ops.post(uri, headers, None)
    case Method.PUT => Ops.put(uri, headers, None)
    case Method.DELETE => Ops.delete(uri, headers)
    case Method.TRACE => Ops.trace(uri, headers)
  }

  def request[A : Codec](method: Method, uri: Uri, headers: Map[String, String], body: Option[A]): HttpRequestIO[HttpResponse] = method match {
    case Method.OPTIONS => Ops.options(uri, headers)
    case Method.GET => Ops.get(uri, headers)
    case Method.HEAD => Ops.head(uri, headers)
    case Method.POST => Ops.post(uri, headers, body.map(x => x.encode))
    case Method.PUT => Ops.put(uri, headers, body.map(x => x.encode))
    case Method.DELETE => Ops.delete(uri, headers)
    case Method.TRACE => Ops.trace(uri, headers)
  }

  def withOpts(method: Method, uri: Uri, opts: Opts): HttpRequestIO[HttpResponse] = {
    request(method, uri, constructHeaders(opts))
  }

  def withOpts[A : Codec](method: Method, uri: Uri, opts: Opts, body: Option[A]): HttpRequestIO[HttpResponse] =
    request(method, uri, constructHeaders(opts), body)

  def optionsWithOpts(uri: Uri, opts: Opts): HttpRequestIO[HttpResponse] = withOpts(Method.OPTIONS, uri, opts)
  def getWithOpts(uri: Uri, opts: Opts): HttpRequestIO[HttpResponse] = withOpts(Method.GET, uri, opts)
  def headWithOpts(uri: Uri, opts: Opts): HttpRequestIO[HttpResponse] = withOpts(Method.HEAD, uri, opts)
  def postWithOpts[A : Codec](uri: Uri, opts: Opts, body: Option[A] = None): HttpRequestIO[HttpResponse] = withOpts(Method.POST, uri, opts, body)
  def putWithOpts[A : Codec](uri: Uri, opts: Opts, body: Option[A] = None): HttpRequestIO[HttpResponse] = withOpts(Method.PUT, uri, opts, body)
  def deleteWithOpts(uri: Uri, opts: Opts): HttpRequestIO[HttpResponse] = withOpts(Method.DELETE, uri, opts)
  def traceWithOpts(uri: Uri, opts: Opts): HttpRequestIO[HttpResponse] = withOpts(Method.TRACE, uri, opts)

  private def constructHeaders(opts: Opts) =
    opts.headers ++
      opts.cookies.map(_.map(cookie => "Set-Cookie" -> cookie.show)).getOrElse(Map()) ++
      opts.auth.map(auth => Map("Authentication" -> auth.show)).getOrElse(Map())
}
