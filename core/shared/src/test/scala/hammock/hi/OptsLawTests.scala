package hammock
package hi

import cats.tests.CatsSuite
import monocle.law.discipline._

class OptsLawTests extends CatsSuite {

  import TestInstances._

  checkAll("Opts.auth", LensTests(Opts.auth))
  // checkAll("authOpt", OptionalTests(Opts.authOpt))
  checkAll("Opts.headers", LensTests(Opts.headers))
  checkAll("Opts.cookies", LensTests(Opts.cookies))
  // checkAll("cookiesOpt", OptionalTests(Opts.cookiesOpt))

}
