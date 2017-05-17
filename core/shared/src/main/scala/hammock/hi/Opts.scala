package hammock
package hi

import monocle.Optional
import monocle.macros.GenLens

case class Opts(
  auth: Option[Auth],
  headers: Map[String, String],
  cookies: Option[List[Cookie]])

object Opts {

  object optics {

    val authOpt = GenLens[Opts](_.auth)

    val auth = Optional[Opts, Auth] {
      case Opts(None, _, _) => None
      case Opts(s@Some(x), _, _) => s
    } (auth => (s => authOpt.set(Some(auth))(s)))

    val headers = GenLens[Opts](_.headers)

    val cookiesOpt = GenLens[Opts](_.cookies)

  }

  implicit val defaultOptions = new Default[Opts] {
    def default = Opts(None, Map(), None)
  }

  val default = Default[Opts].default
}
