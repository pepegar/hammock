package hammock
package hi

object dsl {
  // authentication
  def auth(a: Auth): Opts => Opts = Opts.auth.set(a)

  def cookies_!(cookies: List[Cookie]): Opts => Opts = Opts.cookies.set(cookies)
  def cookies(cookies: List[Cookie]): Opts => Opts = Opts.cookies.modify(cookies ++ _)
  def cookie(cookie: Cookie): Opts => Opts = Opts.cookies.modify(cookie :: _)

  // headers
  def headers_!(headers: Map[String, String]): Opts => Opts = Opts.headers.set(headers)
  def headers(headers: Map[String, String]): Opts => Opts = Opts.headers.modify(headers ++ _)
  def header(header: (String, String)): Opts => Opts = Opts.headers.modify(_ + header)

  // params
  def params_!(params: Map[String, String]): Opts => Opts = Opts.params.set(params)
  def params(params: Map[String, String]): Opts => Opts = Opts.params.modify(params ++ _)
  def param(param: (String, String)): Opts => Opts = Opts.params.modify(_ + param)

  implicit class ops2OpsSyntax(a: Opts) {
    def &~>(b: Opts => Opts): Opts = b(a)
  }
}
