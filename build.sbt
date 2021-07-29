import microsites._
import ReleaseTransformations._

inThisBuild(
  List(
    organization := "com.pepegar",
    homepage := Some(url("https://github.com/pepegar/hammock")),
    licenses := List("Apache-2.0" -> url("https://opensource.org/licenses/MIT")),
    developers := List(
      Developer(
        "pepegar",
        "Pepe García",
        "pepe@pepegar.com",
        url("https://pepegar.com")
      )
    )
  )
)

val Versions = Map(
  "circe"                   -> "0.13.0",
  "monocle"                 -> "2.1.0",
  "collection-compat"       -> "2.3.2",
  "atto"                    -> "0.9.0",
  "cats"                    -> "2.2.0",
  "cats-effect"             -> "2.2.0",
  "simulacrum"              -> "1.0.1",
  "scalatest"               -> "3.2.3",
  "scalacheck"              -> "1.15.2",
  "scalatestplusScalaCheck" -> "3.2.2.0",
  "scalatestplusMockito"    -> "1.0.0-M2",
  "discipline"              -> "1.1.3",
  "discipline-scalatest"    -> "2.1.1",
  "macro-paradise"          -> "2.1.1",
  "kind-projector"          -> "0.10.3",
  "akka-http"               -> "10.1.10",
  "akka-stream"             -> "2.5.30",
  "ahc"                     -> "2.12.1",
  "spring"                  -> "5.3.3",
  "findbugs"                -> "3.0.2",
  "apacheHttp"              -> "4.5.12",
  "mockito"                 -> "1.10.19"
)

val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false,
  skip in publish := true
)

val filterConsoleScalacOptions = { options: Seq[String] =>
  options.filterNot(
    Set(
      "-Werror",
      "-Wdead-code",
      "-Wunused:imports",
      "-Ywarn-unused",
      "-Ywarn-unused:imports",
      "-Ywarn-unused-import",
      "-Ywarn-dead-code",
      "-Xfatal-warnings"
    )
  )
}

val buildSettings = Seq(
  organization := "com.pepegar",
  scalaVersion := "2.13.4",
  crossScalaVersions := Seq(scalaVersion.value, "2.12.10"),
  licenses := Seq(("MIT", url("http://opensource.org/licenses/MIT"))),
  scalacOptions ++= Seq(
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-encoding",
    "utf-8", // Specify character encoding used by source files.
    "-explaintypes", // Explain type errors in more detail.
    "-feature", // Emit warning and location for usages of features that should be imported explicitly.
    "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
    "-language:experimental.macros", // Allow macro definition (besides implementation and application)
    "-language:higherKinds", // Allow higher-kinded types
    "-language:implicitConversions", // Allow definition of implicit functions called views
    "-unchecked", // Enable additional warnings where generated code depends on assumptions.
    "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
    "-Xfatal-warnings", // Fail the compilation if there are any warnings.
    "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
    "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
    "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
    "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
    "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
    "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
    "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
    "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
    "-Xlint:option-implicit", // Option.apply used implicit view.
    "-Xlint:package-object-classes", // Class or object defined in package object.
    "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
    "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
    "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
    "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
    "-Ywarn-dead-code", // Warn when dead code is identified.
    "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
    "-Ywarn-numeric-widen", // Warn when numerics are widened.
    "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
    "-Ywarn-unused:locals", // Warn if a local definition is unused.
    "-Ywarn-unused:params", // Warn if a value parameter is unused.
    "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
    "-Ywarn-unused:privates", // Warn if a private member is unused.
    "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
  ),
  scalacOptions in (Compile, console) ~= filterConsoleScalacOptions,
  scalacOptions in (Compile, doc) ~= filterConsoleScalacOptions,
  scalafmtOnCompile in ThisBuild := true
)

val commonDependencies = Seq(
  libraryDependencies ++= Seq(
    "org.scala-lang.modules"     %% "scala-collection-compat" % Versions("collection-compat"),
    "org.typelevel"              %% "cats-core"               % Versions("cats"),
    "org.typelevel"              %% "cats-free"               % Versions("cats"),
    "org.typelevel"              %% "alleycats-core"          % Versions("cats"),
    "org.typelevel"              %% "simulacrum"              % Versions("simulacrum"),
    "com.github.julien-truffaut" %% "monocle-core"            % Versions("monocle"),
    "com.github.julien-truffaut" %% "monocle-macro"           % Versions("monocle"),
    "org.tpolecat"               %% "atto-core"               % Versions("atto"),
    "com.github.julien-truffaut" %% "monocle-law"             % Versions("monocle") % Test,
    "org.typelevel"              %% "cats-laws"               % Versions("cats") % Test,
    "org.typelevel"              %% "cats-testkit"            % Versions("cats") % Test,
    "org.scalatest"              %% "scalatest"               % Versions("scalatest") % Test,
    "org.scalacheck"             %% "scalacheck"              % Versions("scalacheck") % Test,
    "org.scalatestplus"          %% "scalacheck-1-14"         % Versions("scalatestplusScalaCheck") % Test,
    "org.typelevel"              %% "discipline-core"         % Versions("discipline") % Test,
    "org.typelevel"              %% "discipline-scalatest"    % Versions("discipline-scalatest") % Test
  )
)

val compilerPlugins = Seq(
  libraryDependencies ++= Seq(
    compilerPlugin("org.typelevel" %% "kind-projector" % Versions("kind-projector"))
  ),
  Compile / scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, n)) if n >= 13 => "-Ymacro-annotations" :: Nil
      case _                       => Nil
    }
  },
  libraryDependencies ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, n)) if n >= 13 => Nil
      case _ =>
        compilerPlugin("org.scalamacros" % "paradise" % Versions("macro-paradise") cross CrossVersion.full) :: Nil
    }
  }
)

lazy val hammock = project
  .in(file("."))
  .settings(buildSettings)
  .settings(noPublishSettings)
  .dependsOn(core, circe, apache, akka, asynchttpclient, resttemplate)
  .aggregate(core, circe, apache, akka, asynchttpclient, resttemplate)

lazy val core = project
  .in(file("core"))
  .settings(moduleName := "hammock-core")
  .settings(buildSettings)
  .settings(commonDependencies)
  .settings(compilerPlugins)

lazy val circe = project
  .in(file("hammock-circe"))
  .settings(moduleName := "hammock-circe")
  .settings(buildSettings)
  .settings(commonDependencies)
  .settings(compilerPlugins)
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core"    % Versions("circe"),
      "io.circe" %% "circe-generic" % Versions("circe"),
      "io.circe" %% "circe-parser"  % Versions("circe")
    )
  )
  .dependsOn(core)

lazy val apache = project
  .in(file("hammock-apache-http"))
  .settings(moduleName := "hammock-apache-http")
  .settings(buildSettings)
  .settings(commonDependencies)
  .settings(compilerPlugins)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel"             %% "cats-effect"           % Versions("cats-effect"),
      "org.apache.httpcomponents" % "httpclient"             % Versions("apacheHttp"),
      "org.scalatestplus"         %% "scalatestplus-mockito" % Versions("scalatestplusMockito") % Test,
      "org.mockito"               % "mockito-all"            % Versions("mockito") % Test
    )
  )
  .dependsOn(core)

lazy val akka = project
  .in(file("hammock-akka-http"))
  .settings(moduleName := "hammock-akka-http")
  .settings(buildSettings)
  .settings(commonDependencies)
  .settings(compilerPlugins)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel"     %% "cats-effect"           % Versions("cats-effect"),
      "com.typesafe.akka" %% "akka-http"             % Versions("akka-http"),
      "com.typesafe.akka" %% "akka-stream"           % Versions("akka-stream"),
      "org.mockito"       % "mockito-all"            % Versions("mockito") % Test,
      "org.scalatestplus" %% "scalatestplus-mockito" % Versions("scalatestplusMockito") % Test
    )
  )
  .dependsOn(core)

lazy val asynchttpclient = project
  .in(file("hammock-asynchttpclient"))
  .settings(moduleName := "hammock-asynchttpclient")
  .settings(buildSettings)
  .settings(commonDependencies)
  .settings(compilerPlugins)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel"       %% "cats-effect"           % Versions("cats-effect"),
      "org.asynchttpclient" % "async-http-client"      % Versions("ahc"),
      "org.scalatestplus"   %% "scalatestplus-mockito" % Versions("scalatestplusMockito") % Test,
      "org.mockito"         % "mockito-all"            % Versions("mockito") % Test
    )
  )
  .dependsOn(core)

lazy val resttemplate = project
  .in(file("hammock-resttemplate"))
  .settings(moduleName := "hammock-resttemplate")
  .settings(buildSettings)
  .settings(commonDependencies)
  .settings(compilerPlugins)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel"            %% "cats-effect"           % Versions("cats-effect"),
      "com.google.code.findbugs" % "jsr305"                 % Versions("findbugs") % Optional,
      "org.springframework"      % "spring-web"             % Versions("spring"),
      "org.scalatestplus"        %% "scalatestplus-mockito" % Versions("scalatestplusMockito") % Test,
      "org.mockito"              % "mockito-all"            % Versions("mockito") % Test
    )
  )
  .dependsOn(core)

lazy val javadocIoUrl = settingKey[String]("the url of hammock documentation in http://javadoc.io")

lazy val docs = project
  .in(file("docs"))
  .dependsOn(core, circe, apache, akka, asynchttpclient, resttemplate)
  .settings(moduleName := "hammock-docs")
  .settings(buildSettings)
  .settings(compilerPlugins)
  .settings(noPublishSettings)
  .settings(
    micrositeName := "Hammock",
    micrositeDescription := "Purely functional HTTP client",
    micrositeBaseUrl := "hammock",
    javadocIoUrl := s"https://www.javadoc.io/doc/${organization.value}/hammock-core_${scalaVersion.value}",
    micrositeDocumentationUrl := javadocIoUrl.value,
    micrositeGithubOwner := "pepegar",
    micrositeGithubRepo := "hammock",
    micrositeHighlightTheme := "tomorrow",
    micrositePushSiteWith := GitHub4s,
    micrositeGithubToken := sys.env.get("GITHUB_TOKEN"),
    micrositeExtraMdFiles := Map(
      file("README.md") -> ExtraMdFileConfig(
        "index.md",
        "home"
      ),
      file("CHANGELOG.md") -> ExtraMdFileConfig(
        "changelog.md",
        "home",
        Map("title" -> "changelog", "section" -> "changelog", "position" -> "99")
      )
    ),
    micrositeCompilingDocsTool := WithMdoc,
    mdocIn := tutSourceDirectory.value,
    scalacOptions ~= filterConsoleScalacOptions,
    scalacOptions += "-language:postfixOps"
  )
  .enablePlugins(MicrositesPlugin)

lazy val readme = (project in file("readme"))
  .settings(moduleName := "hammock-readme")
  .dependsOn(core, circe, apache)
  .settings(buildSettings)
  .settings(noPublishSettings)
  .settings(
    mdocIn := baseDirectory.value / "docs",
    mdocOut := baseDirectory.value.getParentFile,
    scalacOptions ~= (_ filterNot Set("-Xfatal-warnings", "-Ywarn-unused-import", "-Xlint").contains)
  )
  .enablePlugins(MdocPlugin)

lazy val example = project
  .in(file("example"))
  .settings(buildSettings)
  .settings(noPublishSettings)
  .settings(compilerPlugins)
  .dependsOn(core, circe, apache, akka, asynchttpclient, resttemplate)

addCommandAlias("formatAll", ";sbt:scalafmt;test:scalafmt;compile:scalafmt")
addCommandAlias("validateScalafmt", ";sbt:scalafmt::test;test:scalafmt::test;compile:scalafmt::test")
addCommandAlias("validateDoc", ";docs/mdoc;readme/mdoc")
addCommandAlias(
  "validateTests",
  ";validateScalafmt;+core/test;+circe/test;+akka/test;+asynchttpclient/test;+resttemplate/test;+apache/test")
addCommandAlias("validate", ";clean;validateScalafmt;validateTests;validateDoc")
