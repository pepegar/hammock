package hammock
package hi

import monocle.{Lens, Optional}
import monocle.macros.GenLens

case class Opts(auth: Option[Auth], headers: Map[String, String], cookies: Option[List[Cookie]])

object Opts {

  val auth: Lens[Opts, Option[Auth]] = GenLens[Opts](_.auth)

  val authOpt: Optional[Opts, Auth] = Optional[Opts, Auth] {
    _.auth
  } { auth =>
    {
      case opts @ Opts(Some(_), _, _) => opts.copy(auth = Some(auth))
      case opts                       => opts
    }
  }

  val headers: Lens[Opts, Map[String, String]] = GenLens[Opts](_.headers)

  val cookies: Lens[Opts, Option[List[Cookie]]] = GenLens[Opts](_.cookies)

  val cookiesOpt: Optional[Opts, List[Cookie]] = Optional[Opts, List[Cookie]] {
    _.cookies
  } { cookies =>
    {
      case opts @ Opts(_, _, Some(_)) => opts.copy(cookies = Some(cookies))
      case opts                       => opts
    }
  }

  implicit val defaultOptions = new Default[Opts] {
    def default = Opts(None, Map(), None)
  }

  val default = Default[Opts].default
}
