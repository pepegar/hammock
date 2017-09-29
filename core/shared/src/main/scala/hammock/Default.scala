package hammock

import simulacrum.typeclass

/**
 * Typeclass for types with a default value.  In conjunction with a
 * Semigroup, a Monoid for the type can be mechanically derived.
 */
@typeclass
trait Default[A] {
  def default: A
}
