package hammock
package hi

object dsl {

  def auth(a: Auth): Opts => Opts = Opts.optics.auth.set(a)

  def cookies_!(cookies: List[Cookie]): Opts => Opts = Opts.optics.cookies.set(cookies)
  def cookies(cookies: List[Cookie]): Opts => Opts = Opts.optics.cookies.modify(cookies ++ _)
  def cookie(cookie: Cookie): Opts => Opts = Opts.optics.cookies.modify(cookie :: _)

  def headers_!(headers: Map[String, String]): Opts => Opts = Opts.optics.headers.set(headers)
  def headers(headers: Map[String, String]): Opts => Opts = Opts.optics.headers.modify(headers ++ _)
  def header(header: (String, String)): Opts => Opts = Opts.optics.headers.modify(_ + header)

  def params_!(params: Map[String, String]): Opts => Opts = Opts.optics.params.set(params)
  def params(params: Map[String, String]): Opts => Opts = Opts.optics.params.modify(params ++ _)
  def param(param: (String, String)): Opts => Opts = Opts.optics.params.modify(_ + param)

  implicit class opts2OptsSyntax(a: Opts => Opts) {
    def &>(b: Opts => Opts): Opts => Opts = a compose b
  }
}
