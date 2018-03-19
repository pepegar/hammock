package hammock

import atto._
import Atto._
import cats.implicits._
import org.scalacheck._
import org.scalacheck.Prop._

object UriProps extends Properties("Uri") {
  import Uri._
  import TestInstances._

  property("authority roudtrip") = forAll(authorityArbitrary.arbitrary) { auth: Authority =>
    Uri.Authority.parse.parseOnly(auth.show).either === Right(auth)
  }

  property("uri roudtrip") = forAll(uriArbitrary.arbitrary) { uri: Uri =>
    Uri.unsafeParse(uri.show) === uri
  }

  property("/ appends to the path") = forAll(uriArbitrary.arbitrary, nonEmptyAlphanumString) { (uri: Uri, toAppend: String) =>
    (uri / toAppend).path === uri.path ++ "/" ++ toAppend
  }
}

