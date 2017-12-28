package hammock

import cats.effect.Sync
import cats.~>

trait InterpTrans[F[_]] {

  def trans(implicit S: Sync[F]): HttpF ~> F

}
