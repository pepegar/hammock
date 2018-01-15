package hammock

import org.scalacheck._
import hammock.hi._
import cats._
import cats.implicits._
import java.util.Date

object TestInstances {
  import Cookie._
  import Auth._
  import Uri._

  val nonEmptyString: Gen[String] = Gen.alphaNumStr.suchThat(!_.isEmpty)

  val nonEmptyStringPair: Gen[(String, String)] = for {
    k <- nonEmptyString
    v <- nonEmptyString
  } yield (k, v)

  val octet: Gen[Int] = Gen.choose(0, 255)

  val ipv4Gen = for {
    a <- octet
    b <- octet
    c <- octet
    d <- octet
  } yield Host.IPv4(a, b, c, d)

  val ipv6GroupGen = for {
    a <- octet
    b <- octet
  } yield Host.IPv6Group(Vector(a.toByte, b.toByte))

  val ipv6Gen = for {
    a <- ipv6GroupGen
    b <- ipv6GroupGen
    c <- ipv6GroupGen
    d <- ipv6GroupGen
    e <- ipv6GroupGen
    f <- ipv6GroupGen
    g <- ipv6GroupGen
    h <- ipv6GroupGen
  } yield Host.IPv6(a, b, c, d, e, f, g, h)

  val otherGen = nonEmptyString.map(Host.Other)

  val localHostGen = Gen.const(Host.Localhost)

  implicit val hostArbitrary: Arbitrary[Host] = Arbitrary(Gen.oneOf(ipv4Gen, ipv6Gen, otherGen, localHostGen))

  implicit val authorityArbitrary: Arbitrary[Authority] = Arbitrary(for {
    user <- Gen.option(nonEmptyString)
    host <- hostArbitrary.arbitrary
    port <- Gen.option(Gen.choose(0L, 65536L))
  } yield Authority(user, host, port))

  implicit val uriArbitrary: Arbitrary[Uri] = Arbitrary(for {
    scheme <- Gen.some(Gen.oneOf("http", "https", "ftp", "ws"))
    authority <- Gen.some(authorityArbitrary.arbitrary)
    path <- Gen.alphaNumStr.map(_.mkString("/", "/", ""))
    query <- Gen.mapOf(nonEmptyStringPair)
    fragment <- Gen.option(nonEmptyString)
  } yield Uri(scheme, authority, path, query, fragment))

  implicit val dateCogen: Cogen[Date] = Cogen[Long].contramap(d => d.getTime())

  implicit val genApplicative: Applicative[Gen] = new Applicative[Gen] {
    def pure[A](a: A): Gen[A] = Gen.const(a)
    def ap[A, B](fn: Gen[A => B])(fa: Gen[A]): Gen[B] = fn flatMap { f =>
      fa.map(f)
    }
  }

  implicit val authArbitrary: Arbitrary[Auth] = Arbitrary(
    Gen.oneOf(
      nonEmptyString.map(OAuth2Bearer),
      nonEmptyString.map(OAuth2Token),
      (nonEmptyString, nonEmptyString).mapN(BasicAuth)
    )
  )

  implicit val authCogen: Cogen[Auth] = Cogen[Either[String, Either[String, (String, String)]]].contramap[Auth] {
    case OAuth2Token(token) => Left(token)
    case OAuth2Bearer(token) => Right(Left(token))
    case BasicAuth(user, pass) => Right(Right((user, pass)))
  }

  implicit val sameSiteCogen: Cogen[SameSite] = Cogen[Unit].contramap[SameSite](_ => ())

  implicit val sameSiteArbitrary: Arbitrary[SameSite] = Arbitrary(Gen.oneOf(SameSite.Lax, SameSite.Strict))

  implicit val cookieArbitrary: Arbitrary[Cookie] = Arbitrary(
    for {
      name <- nonEmptyString
      value <- nonEmptyString
    } yield Cookie(name, value)
  )

  implicit val cookieCogen: Cogen[Cookie] =
    Cogen[
      (String,
      String,
      Option[Date],
      Option[Int],
      Option[String],
      Option[String],
      Option[Boolean],
      Option[Boolean],
      Option[SameSite],
      Option[Map[String, String]])].contramap[Cookie] { c =>
    (c.name,c.value,c.expires,c.maxAge,c.domain,c.path,c.secure,c.httpOnly,c.sameSite,c.custom)
    }


  implicit val optsArbitrary: Arbitrary[Opts] = Arbitrary(
    for {
      auth <- Gen.some(authArbitrary.arbitrary)
      headers <- Gen.mapOfN(5, nonEmptyStringPair)
      cookies <- Gen.some(Gen.listOf(cookieArbitrary.arbitrary))
    } yield Opts(auth, headers, cookies)
  )

}
