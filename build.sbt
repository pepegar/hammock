import microsites._
import ReleaseTransformations._
import sbtcrossproject.{crossProject, CrossType}

val Versions = Map(
  "contextual"     -> "1.0.1",
  "circe"          -> "0.9.0",
  "monocle"        -> "1.5.0-cats",
  "atto"           -> "0.6.1",
  "cats"           -> "1.0.1",
  "cats-effect"    -> "0.8",
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
  scalacOptions in (Compile, console) ~= filterConsoleScalacOptions,
  scalafmtOnCompile in ThisBuild := true
)

val commonDependencies = Seq(
  libraryDependencies ++= Seq(
    "org.typelevel"              %%% "cats-core"      % Versions("cats"),
    "org.typelevel"              %%% "cats-free"      % Versions("cats"),
    "org.typelevel"              %%% "alleycats-core" % Versions("cats"),
    "com.propensive"             %%% "contextual"     % Versions("contextual"),
    "org.typelevel"              %%% "cats-effect"    % Versions("cats-effect"),
    "com.github.mpilquist"       %%% "simulacrum"     % Versions("simulacrum"),
    "com.github.julien-truffaut" %%% "monocle-core"   % Versions("monocle"),
    "com.github.julien-truffaut" %%% "monocle-macro"  % Versions("monocle"),
    "org.tpolecat"               %%% "atto-core"      % Versions("atto"),
    "com.github.julien-truffaut" %%% "monocle-law"    % Versions("monocle") % "test",
    "org.typelevel"              %%% "cats-laws"      % Versions("cats") % "test",
    "org.typelevel"              %%% "cats-testkit"   % Versions("cats") % "test",
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
      "org.apache.httpcomponents" % "httpclient"  % "4.5.4",
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
    scalacOptions in Tut ~= filterConsoleScalacOptions
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
