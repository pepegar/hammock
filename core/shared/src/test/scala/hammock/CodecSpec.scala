package hammock

import cats.Eq
import cats.instances.option._
import cats.instances.int._
import cats.laws._
import cats.laws.discipline._

import org.scalacheck.{ Arbitrary, Prop }
import org.scalatest._
import org.typelevel.discipline.Laws
import org.typelevel.discipline.scalatest.Discipline

import scala.util._

trait CodecLaws[A] {
  implicit def F: Codec[A]

  def decodeAfterEncodeEquality(a: A): IsEq[Option[A]] =
    F.decode(F.encode(a)).right.toOption <-> Some(a)
}

object CodecLaws {
  def apply[T](implicit ev: Codec[T]): CodecLaws[T] = new CodecLaws[T] {def F = ev}
}

trait CodecTests[A] extends Laws {
  def laws: CodecLaws[A]

  def codec(implicit A: Arbitrary[A], eq: Eq[A]): RuleSet = new DefaultRuleSet(
    "Codec",
    None,
    "decodeAfterEncodeEquality" -> Prop.forAll { (a: A) =>
      laws.decodeAfterEncodeEquality(a)
    })
}

object CodecTests {

  def apply[A : Codec](implicit A: Arbitrary[A], eq: Eq[A]): CodecTests[A] = new CodecTests[A] {
    def laws: CodecLaws[A] = CodecLaws[A]
  }

}


class CodecSpec extends FunSuite with Discipline {

  implicit val intCodec = new Codec[Int] {
    def encode(t: Int) = t.toString
    def decode(s: String): Either[CodecException, Int] = Try(s.toInt) match {
      case Success(n) => Right(n)
      case Failure(ex) => Left(CodecException.withMessage(""))
    }
  }

  checkAll("Codec[Int]", CodecTests[Int].codec)
}
