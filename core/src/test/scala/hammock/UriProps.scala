package hammock

import atto._
import Atto._
import cats.Foldable
import cats.data.NonEmptyList
import cats.implicits._
import org.scalacheck._
import org.scalacheck.Prop._
import org.scalacheck.Gen

object UriProps extends Properties("Uri") {
  import Uri._
  import TestInstances._

  property("authority roudtrip") = forAll(authorityArbitrary.arbitrary) { auth: Authority =>
    Uri.Authority.parse.parseOnly(auth.show).either === Right(auth)
  }

  property("uri roudtrip") = forAll(uriArbitrary.arbitrary) { uri: Uri =>
    Uri.unsafeParse(uri.show) === uri
  }

  property("/ appends to the path") = forAll(uriArbitrary.arbitrary, nonEmptyAlphanumString) {
    (uri: Uri, toAppend: String) =>
      (uri / toAppend).path === uri.path ++ "/" ++ toAppend
  }

  property("param method appends parameter to the query") =
    forAll(uriArbitrary.arbitrary, nonEmptyAlphanumString, nonEmptyAlphanumString) {
      (uri: Uri, key: String, value: String) =>
        val query = uri.param(key, value).query
        query.contains(key) && query(key) === value
    }

  // property("string interpolation") = forAll(nonEmptyAlphanumString, nonEmptyAlphanumString, nonEmptyAlphanumString) {
  //   (url: String, key1: String, value1: String) =>
  //     val substituedUri = uri"https://${url}?${key1}=${value1}&other=query"
  //     substituedUri.query.contains(key1) && substituedUri.query(key1) === value1
  //     substituedUri.show == s"https://${url}?${key1}=${value1}&other=query"
  // }

  property("params method appends multiple parameters to the query") =
    forAll(uriArbitrary.arbitrary, Gen.listOf(nonEmptyStringPair)) { (uri, pairs) =>
      val query = uri.params(pairs: _*).query
      Foldable[List].foldLeft(pairs.map(pair => query.contains(pair._1)), true)(_ && _)
    }

  property("? method should do the same as 'params'") =
    forAll(uriArbitrary.arbitrary, Gen.nonEmptyListOf(nonEmptyStringPair)) { (uri, pairs) =>
      val result = uri ? NonEmptyList.fromListUnsafe(pairs)
      result === uri.copy(query = uri.query ++ pairs)
    }

}
