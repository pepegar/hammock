import microsites._
import ReleaseTransformations._
import sbtcrossproject.{crossProject, CrossType}

val Versions = Map(
  "contextual"     -> "1.0.1",
  "circe"          -> "0.9.0",
  "monocle"        -> "1.5.0-cats",
  "atto"           -> "0.6.1",
  "cats"           -> "1.0.1",
  "cats-effect"    -> "0.7",
  "simulacrum"     -> "0.11.0",
  "scalatest"      -> "3.0.4",
  "scalacheck"     -> "1.13.5",
  "discipline"     -> "0.8",
  "macro-paradise" -> "2.1.1",
  "kind-projector" -> "0.9.5",
  "akka-http"      -> "10.0.9",
  "ahc"            -> "2.1.0-RC2"
)

publishTo in ThisBuild := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false,
  skip in publish := true
)

val publishSettings = Seq(
  publishMavenStyle := true,
  publishArtifact in Test := false,
  useGpg := true,
  homepage := Some(url("https://github.com/pepegar/hammock")),
  pomIncludeRepository := Function.const(false),
  pomExtra :=
    <scm>
      <url>https://github.com/pepegar/hammock</url>
      <connection>scm:git:git@github.com:pepegar/hammock.git</connection>
    </scm>
    <developers>
      <developer>
        <id>pepegar</id>
        <name>Pepe García</name>
        <url>http://pepegar.com</url>
      </developer>
    </developers>,
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommand("publishSigned"),
    setNextVersion,
    commitNextVersion,
    releaseStepCommand("sonatypeReleaseAll"),
    releaseStepCommand("docs/publishMicrosite"),
    pushChanges
  )
)

val buildSettings = Seq(
  organization := "com.pepegar",
  scalaVersion := "2.12.4",
  licenses := Seq(("MIT", url("http://opensource.org/licenses/MIT"))),
  crossScalaVersions := Seq("2.11.12", scalaVersion.value),
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
    "-language:postfixOps", // Allow postfix operations without preceeding `.`
    "-unchecked", // Enable additional warnings where generated code depends on assumptions.
    "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
    "-Xfatal-warnings", // Fail the compilation if there are any warnings.
    "-Xfuture", // Turn on future language features.
    "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
    "-Xlint:by-name-right-associative", // By-name parameter of right associative operator.
    "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
    "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
    "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
    "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
    "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
    "-Xlint:nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
    "-Xlint:option-implicit", // Option.apply used implicit view.
    "-Xlint:package-object-classes", // Class or object defined in package object.
    "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
    "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
    "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
    "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
    "-Xlint:unsound-match", // Pattern match may not be typesafe.
    "-Yno-adapted-args", // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
    "-Ypartial-unification", // Enable partial unification in type constructor inference
    "-Ywarn-dead-code", // Warn when dead code is identified.
    "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
    "-Ywarn-infer-any", // Warn when a type argument is inferred to be `Any`.
    "-Ywarn-nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Ywarn-nullary-unit", // Warn when nullary methods return Unit.
    "-Ywarn-numeric-widen", // Warn when numerics are widened.
    "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
  ),
  scalacOptions := {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 12)) =>
        Seq(
          "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
          "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
          "-Ywarn-unused:locals", // Warn if a local definition is unused.
          "-Ywarn-unused:params", // Warn if a value parameter is unused.
          "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
          "-Ywarn-unused:privates" // Warn if a private member is unused.
        )

      case _ => Seq.empty[String]
    }
  },
  scalacOptions in (Compile, console) --= Seq("-Ywarn-unused:imports", "-Xfatal-warnings"),
  scalafmtOnCompile in ThisBuild := true
)

val commonDependencies = Seq(
  libraryDependencies ++= Seq(
    "org.typelevel"              %%% "cats-core"      % Versions("cats"),
    "org.typelevel"              %%% "cats-free"      % Versions("cats"),
    "org.typelevel"              %%% "cats-laws"      % Versions("cats"),
    "org.typelevel"              %%% "alleycats-core" % Versions("cats"),
    "com.propensive"             %%% "contextual"     % Versions("contextual"),
    "org.typelevel"              %% "cats-effect"     % Versions("cats-effect"),
    "com.github.mpilquist"       %%% "simulacrum"     % Versions("simulacrum"),
    "com.github.julien-truffaut" %%% "monocle-core"   % Versions("monocle"),
    "com.github.julien-truffaut" %%% "monocle-macro"  % Versions("monocle"),
    "org.tpolecat"               %%% "atto-core"      % Versions("atto"),
    "org.scalatest"              %%% "scalatest"      % Versions("scalatest") % "test",
    "org.scalacheck"             %%% "scalacheck"     % Versions("scalacheck") % "test",
    "org.typelevel"              %%% "discipline"     % Versions("discipline") % "test"
  )
)

val compilerPlugins = Seq(
  libraryDependencies ++= Seq(
    compilerPlugin("org.scalamacros" %% "paradise"       % Versions("macro-paradise") cross CrossVersion.full),
    compilerPlugin("org.spire-math"  %% "kind-projector" % Versions("kind-projector"))
  )
)

lazy val hammock = project
  .in(file("."))
  .settings(buildSettings)
  .settings(noPublishSettings)
  .dependsOn(coreJVM, coreJS, circeJVM, circeJS, akka, asynchttpclient)
  .aggregate(coreJVM, coreJS, circeJVM, circeJS, akka, asynchttpclient)

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("core"))
  .settings(moduleName := "hammock-core")
  .settings(buildSettings)
  .settings(commonDependencies)
  .settings(compilerPlugins)
  .settings(publishSettings)
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.1"
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.apache.httpcomponents" % "httpclient"  % "4.5.2",
      "org.mockito"               % "mockito-all" % "1.10.18" % "test"
    )
  )

lazy val coreJVM = core.jvm
lazy val coreJS  = core.js

lazy val circe = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("hammock-circe"))
  .settings(moduleName := "hammock-circe")
  .settings(buildSettings)
  .settings(publishSettings)
  .settings(commonDependencies)
  .settings(compilerPlugins)
  .settings(libraryDependencies ++= Seq(
    "io.circe" %%% "circe-core"    % Versions("circe"),
    "io.circe" %%% "circe-generic" % Versions("circe"),
    "io.circe" %%% "circe-parser"  % Versions("circe")))
  .dependsOn(core)

lazy val circeJVM = circe.jvm
lazy val circeJS  = circe.js

lazy val akka = project
  .in(file("hammock-akka-http"))
  .settings(moduleName := "hammock-akka-http")
  .settings(buildSettings)
  .settings(commonDependencies)
  .settings(compilerPlugins)
  .settings(publishSettings)
  .settings(
    libraryDependencies += "com.typesafe.akka" %% "akka-http" % Versions("akka-http")
  )
  .settings(libraryDependencies += "org.mockito" % "mockito-all" % "1.10.18" % "test")
  .dependsOn(coreJVM)

lazy val asynchttpclient = project
  .in(file("hammock-asynchttpclient"))
  .settings(moduleName := "hammock-asynchttpclient")
  .settings(buildSettings)
  .settings(commonDependencies)
  .settings(compilerPlugins)
  .settings(publishSettings)
  .settings(
    libraryDependencies += "org.asynchttpclient" % "async-http-client" % Versions("ahc")
  )
  .dependsOn(coreJVM)

lazy val javadocIoUrl = settingKey[String]("the url of hammock documentation in http://javadoc.io")

lazy val docs = project
  .in(file("docs"))
  .dependsOn(coreJVM, circeJVM, akka, asynchttpclient)
  .settings(moduleName := "hammock-docs")
  .settings(buildSettings)
  .settings(compilerPlugins)
  .settings(noPublishSettings)
  .settings(
    micrositeName := "Hammock",
    micrositeDescription := "Purely functional HTTP client",
    micrositeBaseUrl := "hammock",
    javadocIoUrl := s"https://www.javadoc.io/doc/${organization.value}/hammock-core_2.12",
    micrositeDocumentationUrl := javadocIoUrl.value,
    micrositeGithubOwner := "pepegar",
    micrositeGithubRepo := "hammock",
    micrositeHighlightTheme := "tomorrow",
    micrositePushSiteWith := GitHub4s,
    micrositeGithubToken := sys.env.get("GITHUB_TOKEN"),
    micrositeExtraMdFiles := Map(
      file("README.md") -> ExtraMdFileConfig(
        "index.md",
        "home",
        Map("title" -> "Home", "section" -> "home", "position" -> "0")
      ),
      file("CHANGELOG.md") -> ExtraMdFileConfig(
        "changelog.md",
        "home",
        Map("title" -> "changelog", "section" -> "changelog", "position" -> "99")
      )
    ),
    scalacOptions in Tut ~= (_ filterNot Set(
      "-Xfatal-warnings",
      "-Ywarn-unused-import",
      "-Ywarn-unused:imports",
      "-Xlint").contains)
  )
  .enablePlugins(MicrositesPlugin)

lazy val readme = (project in file("tut"))
  .settings(moduleName := "hammock-readme")
  .dependsOn(coreJVM, circeJVM)
  .settings(buildSettings)
  .settings(noPublishSettings)
  .settings(
    tutSourceDirectory := baseDirectory.value,
    tutTargetDirectory := baseDirectory.value.getParentFile,
    tutNameFilter := """README.md""".r,
    scalacOptions ~= (_ filterNot Set("-Xfatal-warnings", "-Ywarn-unused-import", "-Xlint").contains)
  )
  .enablePlugins(TutPlugin)

lazy val example = project
  .in(file("example"))
  .settings(buildSettings)
  .settings(noPublishSettings)
  .settings(compilerPlugins)
  .dependsOn(coreJVM, circeJVM)

lazy val exampleJS = project
  .in(file("example-js"))
  .enablePlugins(ScalaJSPlugin)
  .settings(buildSettings)
  .settings(noPublishSettings)
  .settings(compilerPlugins)
  .settings(libraryDependencies += "be.doeraene" %%% "scalajs-jquery" % "0.9.2")
  .settings(jsDependencies += "org.webjars" % "jquery" % "2.1.3" / "2.1.3/jquery.js")
  .dependsOn(coreJS, circeJS)

addCommandAlias("formatAll", ";sbt:scalafmt;test:scalafmt;compile:scalafmt")
addCommandAlias("validateScalafmt", ";sbt:scalafmt::test;test:scalafmt::test;compile:scalafmt::test")
addCommandAlias("validateDoc", ";docs/tut;readme/tut")
addCommandAlias(
  "validateJVM",
  ";validateScalafmt;coreJVM/test;circeJVM/test;akka/test;asynchttpclient/test;validateDoc")
addCommandAlias("validateJS", ";validateScalafmt;coreJS/test;circeJS/test")
addCommandAlias("validate", ";clean;validateScalafmt;validateJS;validateJVM;validateDoc")
