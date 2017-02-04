package hammock

import cats._
import cats.arrow._
import cats.syntax.show._

import free._
import free.algebra.{ HttpRequestIO, Ops }
import hi.Opts

object Hammock {

  def request(method: Method, url: String, headers: Map[String, String]): HttpRequestIO[HttpResponse] = method match {
    case Method.OPTIONS => Ops.options(url, headers, None)
    case Method.GET => Ops.get(url, headers, None)
    case Method.HEAD => Ops.head(url, headers, None)
    case Method.POST => Ops.post(url, headers, None)
    case Method.PUT => Ops.put(url, headers, None)
    case Method.DELETE => Ops.delete(url, headers, None)
    case Method.TRACE => Ops.trace(url, headers, None)
  }

  def request[A : Codec](method: Method, url: String, headers: Map[String, String], body: Option[A]): HttpRequestIO[HttpResponse] = method match {
    case Method.OPTIONS => Ops.options(url, headers, body.map(Codec[A].encode))
    case Method.GET => Ops.get(url, headers, body.map(Codec[A].encode))
    case Method.HEAD => Ops.head(url, headers, body.map(Codec[A].encode))
    case Method.POST => Ops.post(url, headers, body.map(Codec[A].encode))
    case Method.PUT => Ops.put(url, headers, body.map(Codec[A].encode))
    case Method.DELETE => Ops.delete(url, headers, body.map(Codec[A].encode))
    case Method.TRACE => Ops.trace(url, headers, body.map(Codec[A].encode))
  }

  def withOpts(method: Method, url: String, opts: Opts): HttpRequestIO[HttpResponse] = {
    request(method, constructUrl(url, opts.params), constructHeaders(opts))
  }

  def withOpts[A : Codec](method: Method, url: String, opts: Opts, body: Option[A]): HttpRequestIO[HttpResponse] =
    request(method, constructUrl(url, opts.params), constructHeaders(opts), body)

  def optionsWithOpts(url: String, opts: Opts): HttpRequestIO[HttpResponse] = withOpts(Method.OPTIONS, url, opts)
  def getWithOpts(url: String, opts: Opts): HttpRequestIO[HttpResponse] = withOpts(Method.GET, url, opts)
  def headWithOpts(url: String, opts: Opts): HttpRequestIO[HttpResponse] = withOpts(Method.HEAD, url, opts)
  def postWithOpts(url: String, opts: Opts): HttpRequestIO[HttpResponse] = withOpts(Method.POST, url, opts)
  def putWithOpts(url: String, opts: Opts): HttpRequestIO[HttpResponse] = withOpts(Method.PUT, url, opts)
  def deleteWithOpts(url: String, opts: Opts): HttpRequestIO[HttpResponse] = withOpts(Method.DELETE, url, opts)
  def traceWithOpts(url: String, opts: Opts): HttpRequestIO[HttpResponse] = withOpts(Method.TRACE, url, opts)

  private def constructHeaders(opts: Opts) =
    opts.headers ++
      opts.cookies.map(_.map(cookie => "Set-Cookie" -> cookie.show)).getOrElse(Map()) ++
      opts.auth.map(auth => Map("Authentication" -> auth.show)).getOrElse(Map())

  private def constructUrl(url: String, params: Map[String, String]): String = {
    val queryString = params map {
      case (k, v) => s"$k=$v"
    } mkString("&")

    if (queryString != "") {
      url + "?" + queryString
    } else {
      url
    }
  }
}
