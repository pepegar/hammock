import ReleaseTransformations._

import sbtcrossproject.{crossProject, CrossType}

val Versions = Map(
  "circe"          -> "0.9.0-M2",
  "monocle"        -> "1.5.0-cats-M2",
  "atto"           -> "0.6.1-M7",
  "cats"           -> "1.0.0-RC1",
  "cats-effect"    -> "0.5",
  "simulacrum"     -> "0.10.0",
  "scalatest"      -> "3.0.1",
  "scalacheck"     -> "1.13.4",
  "discipline"     -> "0.7.3",
  "macro-paradise" -> "2.1.0",
  "kind-projector" -> "0.9.4",
  "akka-http"      -> "10.0.9"
)

val noPublishSettings = Seq(
  skip in publish := true
)

val publishSettings = Seq(
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
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
  crossScalaVersions := Seq("2.11.11", scalaVersion.value),
  scalacOptions ++= Seq(
    "-encoding",
    "UTF-8", // 2 args
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:postfixOps",
    "-language:implicitConversions",
    "-language:experimental.macros",
    "-unchecked",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-value-discard"
  ),
  scalafmtOnCompile in ThisBuild := true
)

val commonDependencies = Seq(
  libraryDependencies ++= Seq(
    "org.typelevel"              %%% "cats-core"      % Versions("cats"),
    "org.typelevel"              %%% "cats-free"      % Versions("cats"),
    "org.typelevel"              %%% "cats-laws"      % Versions("cats"),
    "org.typelevel"              %%% "alleycats-core" % Versions("cats"),
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
  .dependsOn(coreJVM, coreJS, circeJVM, circeJS, akka)
  .aggregate(coreJVM, coreJS, circeJVM, circeJS, akka)
  .enablePlugins(ScalaUnidocPlugin)

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

lazy val docs = project
  .in(file("docs"))
  .dependsOn(coreJVM, circeJVM, akka)
  .settings(moduleName := "hammock-docs")
  .settings(buildSettings)
  .settings(compilerPlugins)
  .settings(noPublishSettings)
  .settings(
    micrositeName := "Hammock",
    micrositeDescription := "Purely functional HTTP client",
    micrositeBaseUrl := "hammock",
    micrositeDocumentationUrl := "/hammock/docs.html",
    micrositeGithubOwner := "pepegar",
    micrositeGithubRepo := "hammock",
    micrositeHighlightTheme := "tomorrow",
    micrositePushSiteWith := GitHub4s,
    micrositeGithubToken := sys.env.get("GITHUB_TOKEN"),
    scalacOptions ~= (_ filterNot Set("-Ywarn-unused-import", "-Xlint").contains)
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
    tutNameFilter := """README.md""".r)
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
addCommandAlias("validateJVM", ";validateScalafmt;coreJVM/test;circeJVM/test;akka/test;validateDoc")
addCommandAlias("validateJS", ";validateScalafmt;coreJS/test;circeJS/test")
addCommandAlias("validate", ";clean;validateScalafmt;validateJS;validateJVM;validateDoc")
