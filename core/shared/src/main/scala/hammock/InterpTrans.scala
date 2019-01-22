package hammock

import cats.~>

trait InterpTrans[F[_]] {
  def trans: HttpF ~> F
}
