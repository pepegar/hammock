package hammock
package hi

import monocle.Optional
import monocle.macros.GenLens

case class Opts(
  auth: Option[Auth],
  headers: Map[String, String],
  params: Map[String, String],
  cookies: Option[List[Cookie]])

object Opts {
  object optics {

    val authOpt = GenLens[Opts](_.auth)

    val auth = Optional[Opts, Auth] {
      case Opts(None, _, _, _) => None
      case Opts(s@Some(x), _, _, _) => s
    } (auth => (s => authOpt.set(Some(auth))(s)))

    val headers = GenLens[Opts](_.headers)

    val params = GenLens[Opts](_.params)

    val cookiesOpt = GenLens[Opts](_.cookies)
    val cookies = Optional[Opts, List[Cookie]] {
      case Opts(_, _, _, None) => None
      case Opts(_, _, _, s@Some(_)) => s
    } (cookies => (s => cookiesOpt.set(Some(cookies))(s)))

  }

  implicit val defaultOptions = new Default[Opts] {
    def default = Opts(None, Map(), Map(), None)
  }

  val default = Default[Opts].default
}
