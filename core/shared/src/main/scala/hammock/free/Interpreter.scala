package hammock
package free

import cats.~>
import cats.effect.Sync
import algebra.HttpRequestF

trait InterpTrans {

  def trans[F[_]: Sync]: HttpRequestF ~> F

}
