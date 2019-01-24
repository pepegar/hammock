import microsites._
import ReleaseTransformations._
import sbtcrossproject.{crossProject, CrossType}

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
  ))

val Versions = Map(
  "contextual"     -> "1.1.0",
  "circe"          -> "0.11.1",
  "monocle"        -> "1.5.1-cats",
  "atto"           -> "0.6.5",
  "cats"           -> "1.5.0",
  "cats-effect"    -> "1.2.0",
  "simulacrum"     -> "0.15.0",
  "scalatest"      -> "3.0.5",
  "scalacheck"     -> "1.14.0",
  "discipline"     -> "0.10.0",
  "macro-paradise" -> "2.1.1",
  "kind-projector" -> "0.9.9",
  "akka-http"      -> "10.0.15",
  "ahc"            -> "2.1.2"
)

val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false,
  skip in publish := true
)

val buildSettings = Seq(
  organization := "com.pepegar",
  scalaVersion := "2.12.5",
  licenses := Seq(("MIT", url("http://opensource.org/licenses/MIT"))),
  crossScalaVersions := Seq("2.11.12", scalaVersion.value),
  scalacOptions in (Compile, console) ~= filterConsoleScalacOptions,
  scalacOptions in (Compile, doc) ~= filterConsoleScalacOptions,
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
  .dependsOn(coreJVM, coreJS, circeJVM, circeJS, apache, akka, asynchttpclient)
  .aggregate(coreJVM, coreJS, circeJVM, circeJS, apache, akka, asynchttpclient)

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("core"))
  .settings(moduleName := "hammock-core")
  .settings(buildSettings)
  .settings(commonDependencies)
  .settings(compilerPlugins)
  .jsSettings(
    libraryDependencies ++= Seq(
      "org.scala-js"      %%% "scalajs-dom"     % "0.9.6",
      "io.scalajs.npm"    %%% "node-fetch"      % "0.4.2",
      "io.github.cquiroz" %%% "scala-java-time" % "2.0.0-RC1"
    ),
    npmDependencies in Test += "node-fetch" -> "2.1.2"
  )

lazy val coreJVM = core.jvm
lazy val coreJS  = core.js.enablePlugins(ScalaJSBundlerPlugin)

lazy val circe = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("hammock-circe"))
  .settings(moduleName := "hammock-circe")
  .settings(buildSettings)
  .settings(commonDependencies)
  .settings(compilerPlugins)
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core"    % Versions("circe"),
      "io.circe" %%% "circe-generic" % Versions("circe"),
      "io.circe" %%% "circe-parser"  % Versions("circe")))
  .dependsOn(core)

lazy val circeJVM = circe.jvm
lazy val circeJS  = circe.js

lazy val apache = project
  .in(file("hammock-apache-http"))
  .settings(moduleName := "hammock-apache-http")
  .settings(buildSettings)
  .settings(commonDependencies)
  .settings(compilerPlugins)
  .settings(
    libraryDependencies ++= Seq(
      "org.apache.httpcomponents" % "httpclient" % "4.5.6"
    )
  )
  .settings(libraryDependencies += "org.mockito" % "mockito-all" % "1.10.19" % "test")
  .dependsOn(coreJVM)

lazy val akka = project
  .in(file("hammock-akka-http"))
  .settings(moduleName := "hammock-akka-http")
  .settings(buildSettings)
  .settings(commonDependencies)
  .settings(compilerPlugins)
  .settings(
    libraryDependencies += "com.typesafe.akka" %% "akka-http" % Versions("akka-http")
  )
  .settings(libraryDependencies += "org.mockito" % "mockito-all" % "1.10.19" % "test")
  .dependsOn(coreJVM)

lazy val asynchttpclient = project
  .in(file("hammock-asynchttpclient"))
  .settings(moduleName := "hammock-asynchttpclient")
  .settings(buildSettings)
  .settings(commonDependencies)
  .settings(compilerPlugins)
  .settings(
    libraryDependencies += "org.asynchttpclient" % "async-http-client" % Versions("ahc")
  )
  .dependsOn(coreJVM)

lazy val javadocIoUrl = settingKey[String]("the url of hammock documentation in http://javadoc.io")

lazy val docs = project
  .in(file("docs"))
  .dependsOn(coreJVM, circeJVM, apache, akka, asynchttpclient)
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
    scalacOptions in Tut ~= filterConsoleScalacOptions,
    scalacOptions in Tut += "-language:postfixOps"
  )
  .enablePlugins(MicrositesPlugin)

lazy val readme = (project in file("tut"))
  .settings(moduleName := "hammock-readme")
  .dependsOn(coreJVM, circeJVM, apache)
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
  .dependsOn(coreJVM, circeJVM, apache)

lazy val exampleJS = project
  .in(file("example-js"))
  .enablePlugins(ScalaJSPlugin)
  .settings(buildSettings)
  .settings(noPublishSettings)
  .settings(compilerPlugins)
  .settings(libraryDependencies += "be.doeraene" %%% "scalajs-jquery" % "0.9.2")
  .settings(jsDependencies += "org.webjars" % "jquery" % "2.1.3" / "2.1.3/jquery.js")
  .dependsOn(coreJS, circeJS)

lazy val exampleNode = project
  .in(file("example-node"))
  .enablePlugins(ScalaJSPlugin)
  .settings(buildSettings)
  .settings(noPublishSettings)
  .settings(compilerPlugins)
  .settings(
    scalacOptions ~= (_ filterNot Set("-Xfatal-warnings", "-Ywarn-unused-import", "-Xlint").contains),
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    scalaJSUseMainModuleInitializer := true
  )
  .dependsOn(coreJS, circeJS)

addCommandAlias("formatAll", ";sbt:scalafmt;test:scalafmt;compile:scalafmt")
addCommandAlias("validateScalafmt", ";sbt:scalafmt::test;test:scalafmt::test;compile:scalafmt::test")
addCommandAlias("validateDoc", ";docs/tut;readme/tut")
addCommandAlias(
  "validateJVM",
  ";validateScalafmt;coreJVM/test;circeJVM/test;akka/test;asynchttpclient/test;validateDoc")
addCommandAlias("validateJS", ";validateScalafmt;coreJS/test;circeJS/test")
addCommandAlias("validate", ";clean;validateScalafmt;validateJS;validateJVM;validateDoc")
