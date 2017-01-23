package hammock
package free

import cats._
import algebra.HttpRequestF

trait InterpTrans {

  def trans[F[_]](implicit ME: MonadError[F, Throwable]): HttpRequestF ~> F

}
