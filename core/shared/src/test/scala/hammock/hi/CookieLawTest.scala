package hammock
package hi

import cats.tests.CatsSuite
import cats.kernel.laws.discipline.EqTests
import monocle.law.discipline._

class CookieLawTests extends CatsSuite {

  import TestInstances._

  checkAll("Cookie.eq", EqTests[Cookie].eqv)
  checkAll("SameSite.eq", EqTests[Cookie.SameSite].eqv)
  checkAll("Cookie.maxAgeOpt", OptionalTests(Cookie.maxAgeOpt))
  checkAll("Cookie.domainOpt", OptionalTests(Cookie.domainOpt))
  checkAll("Cookie.pathOpt", OptionalTests(Cookie.pathOpt))
  checkAll("Cookie.secureOpt", OptionalTests(Cookie.secureOpt))
  checkAll("Cookie.httpOnlyOpt", OptionalTests(Cookie.httpOnlyOpt))
  checkAll("Cookie.sameSiteOpt", OptionalTests(Cookie.sameSiteOpt))
  checkAll("Cookie.customOpt", OptionalTests(Cookie.customOpt))

}
