package hammock
package free

import cats.~>
import cats.effect.Sync
import algebra.HttpRequestF

trait InterpTrans[F[_]] {

  def trans(implicit S: Sync[F]): HttpRequestF ~> F

}
