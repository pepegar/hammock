package hammock

import cats.{Show, Eq}
import cats.instances.string._
import cats.syntax.contravariant._

trait Method {
  def name: String
}

object Method {
  case object OPTIONS extends Method {
    def name: String = "OPTIONS"
  }
  case object GET extends Method {
    def name: String = "GET"
  }
  case object HEAD extends Method {
    def name: String = "HEAD"
  }
  case object POST extends Method {
    def name: String = "POST"
  }
  case object PUT extends Method {
    def name: String = "PUT"
  }
  case object DELETE extends Method {
    def name: String = "DELETE"
  }
  case object TRACE extends Method {
    def name: String = "TRACE"
  }
  case object CONNECT extends Method {
    def name: String = "CONNECT"
  }
  case object PATCH extends Method {
    def name: String = "PATCH"
  }

  implicit val show: Show[Method] = Show[String].contramap(_.name)
  implicit val eq: Eq[Method] = Eq.fromUniversalEquals
}
