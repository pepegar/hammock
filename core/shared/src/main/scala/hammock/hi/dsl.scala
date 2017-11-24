package hammock
package hi

object dsl {

  def auth(a: Auth): Opts => Opts = Opts.auth.set(Some(a))

  def cookies_!(cookies: List[Cookie]): Opts => Opts = Opts.cookies.set(Some(cookies))
  def cookies(cookies: List[Cookie]): Opts => Opts = Opts.cookies.modify {
    case None => Some(cookies)
    case c    => c.map(cookies ++ _)
  }
  def cookie(cookie: Cookie): Opts => Opts = Opts.cookies.modify {
    case None => Some(List(cookie))
    case c    => c.map(cookie :: _)
  }

  def headers_!(headers: Map[String, String]): Opts => Opts = Opts.headers.set(headers)
  def headers(headers: Map[String, String]): Opts => Opts   = Opts.headers.modify(headers ++ _)
  def header(header: (String, String)): Opts => Opts        = Opts.headers.modify(_ + header)

  implicit class opts2OptsSyntax(a: Opts => Opts) {
    def &>(b: Opts => Opts): Opts => Opts = a compose b
  }
}
