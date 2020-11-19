package hammock
package hi

import cats.instances.string._
import cats.instances.map._
import cats.instances.option._
import cats.instances.list._
import org.scalatest.prop.Configuration
import org.scalatest.funsuite.AnyFunSuite
import org.typelevel.discipline.scalatest.FunSuiteDiscipline
import monocle.law.discipline.{LensTests, OptionalTests}

class OptsLawTests extends AnyFunSuite with Configuration with FunSuiteDiscipline {

  import TestInstances._

  checkAll("Opts.auth", LensTests[Opts, Option[Auth]](Opts.auth))
  checkAll("Opts.authOpt", OptionalTests[Opts, Auth](Opts.authOpt))
  checkAll("Opts.headers", LensTests(Opts.headers))
  checkAll("Opts.cookies", LensTests(Opts.cookies))
  checkAll("Opts.cookiesOpt", OptionalTests(Opts.cookiesOpt))
}
