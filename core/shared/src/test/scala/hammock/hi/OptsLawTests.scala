package hammock
package hi

import cats.tests.CatsSuite
import monocle.law.discipline.{LensTests, OptionalTests}

class OptsLawTests extends CatsSuite {

  import TestInstances._

  checkAll("Opts.auth", LensTests[Opts, Option[Auth]](Opts.auth))
  checkAll("Opts.authOpt", OptionalTests[Opts, Auth](Opts.authOpt))
  checkAll("Opts.headers", LensTests(Opts.headers))
  checkAll("Opts.cookies", LensTests(Opts.cookies))
  checkAll("Opts.cookiesOpt", OptionalTests(Opts.cookiesOpt))
}
