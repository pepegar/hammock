package hammock

import cats.{MonadError, ~>}
import cats.free.Free
import cats.data.Kleisli

trait Transformer[Op[_]] {
  type OpIO[A] = Free[Op, A]

  def trans[M[_] : MonadError[?[_], Throwable], A](operation: OpIO[A]): M[A] =
    operation foldMap interp

  def interp[M[_]: MonadError[?[_], Throwable]]: (Op ~> M)
}
