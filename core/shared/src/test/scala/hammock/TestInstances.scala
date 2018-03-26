package hammock

import cats._
import cats.implicits._
import hammock.hi._
import org.scalacheck._

object TestInstances {
  import Auth._
  import Cookie._
  import Uri._
  import Host._

  val nonEmptyString: Gen[String] = Gen.nonEmptyListOf(Gen.oneOf {
    Seq('!', '#', '$', '%', '&', '\'', '*', '+', '-', '.', '^', '_', '`', '|', '~') ++
      ('0' to '9') ++ ('A' to 'Z') ++ ('a' to 'z')
  }).map(_.mkString)

  val nonEmptyAlphanumString: Gen[String] = Gen.nonEmptyListOf(Gen.oneOf {
    ('0' to '9') ++ ('a' to 'z') ++ ('A' to 'Z')
  }).map(_.mkString)

  val nonEmptyStringPair: Gen[(String, String)] = for {
    k <- nonEmptyAlphanumString
    v <- nonEmptyAlphanumString
  } yield (k, v)

  val octet: Gen[Int] = Gen.choose(0, 255).label("Octet")

  val ipv4Gen = (for {
    a <- octet
    b <- octet
    c <- octet
    d <- octet
  } yield Host.IPv4(a, b, c, d)).label("IPV4")

  val ipv6GroupGen = Gen.choose[Short](0, Short.MaxValue).map(x => IPv6Group(x)).label("IPV6Group")

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

  val otherGen = nonEmptyAlphanumString.map(Host.Other.apply).label("Other")

  val localHostGen = Gen.const(Host.Localhost).label("Localhost")

  implicit val hostArbitrary: Arbitrary[Host] = Arbitrary(Gen.oneOf(ipv4Gen, ipv6Gen, otherGen, localHostGen).label("Host"))

  implicit val authorityArbitrary: Arbitrary[Authority] = Arbitrary((for {
    user <- Gen.option(nonEmptyAlphanumString)
    host <- hostArbitrary.arbitrary
    port <- Gen.option(Gen.choose(0L, 65536L))
  } yield Authority(user, host, port)).label("Authority"))

  implicit val uriArbitrary: Arbitrary[Uri] = Arbitrary((for {
    scheme <- Gen.some(Gen.oneOf("http", "https", "ftp", "ws"))
    authority <- Gen.some(authorityArbitrary.arbitrary)
    path <- Gen.alphaNumStr.map(_.mkString("/", "/", ""))
    query <- Gen.mapOf(nonEmptyStringPair)
    fragment <- Gen.option(nonEmptyAlphanumString)
  } yield Uri(scheme, authority, path, query, fragment)).label("Uri"))

  implicit val uriCogen: Cogen[Uri] =
    Cogen[String].contramap(_.show)

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
    ).label("Auth")
  )

  implicit val authCogen: Cogen[Auth] = Cogen[String].contramap[Auth](_.show)

  implicit val sameSiteArbitrary: Arbitrary[SameSite] = Arbitrary(Gen.oneOf(SameSite.Lax, SameSite.Strict).label("SameSite"))

  implicit val sameSiteCogen: Cogen[SameSite] = Cogen[Boolean].contramap[SameSite](_ match {
    case SameSite.Lax => true
    case SameSite.Strict => true
  })

  implicit val mapArbitrary: Arbitrary[Map[String, String]] = Arbitrary(
    Gen.frequency(
      1 -> Gen.const(Map.empty[String, String]),
      5 -> Gen.choose(1, 50).flatMap(x => Gen.mapOfN(x, nonEmptyStringPair))
    ).label("Map[String, String]")
  )

  implicit val cookieCogen: Cogen[Cookie] =
    Cogen.tuple9[
    String,
    String,
    Option[Int],
    Option[String],
    Option[String],
    Option[Boolean],
    Option[Boolean],
    Option[SameSite],
      Option[Map[String, String]]].contramap[Cookie] { c =>
      (c.name, c.value, c.maxAge, c.domain, c.path, c.secure, c.httpOnly, c.sameSite, c.custom)
    }

  // TODO: we should add generator for custom too, but I cannot make it work correctly
  implicit val cookieArbitrary: Arbitrary[Cookie] = Arbitrary(
    (for {
      name <- nonEmptyString
      value <- nonEmptyString
      expires <- Gen.const(None)
      maxAge <- Gen.option(Gen.choose(0, Int.MaxValue))
      domain <- Gen.option(nonEmptyString)
      path <- Gen.option(nonEmptyString)
      secure <- Gen.option(Gen.oneOf(true, false))
      httpOnly <- Gen.option(Gen.oneOf(true, false))
      sameSite <- Gen.option(sameSiteArbitrary.arbitrary)
      custom <- Gen.option(Gen.mapOf(nonEmptyStringPair))
    } yield Cookie(name, value, expires, maxAge, domain, path, secure, httpOnly, sameSite)).label("Cookie")
  )

  implicit val optsCogen: Cogen[Opts] = Cogen.tuple3[Option[Auth], Map[String, String], Option[List[Cookie]]].contramap[Opts] { o =>
    (o.auth, o.headers, o.cookies)
  }

  implicit val optsArbitrary: Arbitrary[Opts] = Arbitrary(
    (for {
      auth <- Gen.some(authArbitrary.arbitrary)
      headers <- mapArbitrary.arbitrary
      cookies <- Gen.option(Gen.nonEmptyListOf(cookieArbitrary.arbitrary).map(_.toList))
    } yield Opts(auth, headers, cookies)).label("Opts")
  )

}
