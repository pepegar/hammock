package hammock
package hi

import cats.tests.CatsSuite
import monocle.law.discipline._

class OptsLawTests extends CatsSuite {

  import TestInstances._

  checkAll("Opts.auth", LensTests(Opts.auth))
  checkAll("Opts.authOpt", OptionalTests(Opts.authOpt))
  checkAll("Opts.headers", LensTests(Opts.headers))
  checkAll("Opts.cookies", LensTests(Opts.cookies))
  checkAll("Opts.cookiesOpt", OptionalTests(Opts.cookiesOpt))

}
