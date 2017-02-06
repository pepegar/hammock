organization in ThisBuild := "com.pepegar"
scalaVersion in ThisBuild := "2.11.8"
licenses in ThisBuild := Seq(("MIT", url("http://opensource.org/licenses/MIT")))

val scalaVersions = Seq("2.11.8", "2.12.0")

val circeVersion = "0.6.1"
val micrositeSettings = Seq(
  micrositeName := "Hammock",
  micrositeDescription := "Purely functional HTTP client",
  micrositeBaseUrl := "hammock",
  micrositeDocumentationUrl := "/hammock/docs.html",
  micrositeGithubOwner := "pepegar",
  micrositeGithubRepo := "hammock",
  micrositeHighlightTheme := "tomorrow"
)
val monocleVersion = "1.4.0"

val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats" % "0.8.1",
    "com.github.julien-truffaut" %%  "monocle-core"  % monocleVersion,
    "com.github.julien-truffaut" %%  "monocle-macro" % monocleVersion,

    compilerPlugin("org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.full),
    compilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3"),
    "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
    "org.typelevel" %% "discipline" % "0.7.3" % "test"
  ),
  bintrayRepository := "com.pepegar"
)

lazy val docs = project.in(file("docs"))
  .dependsOn(coreJVM, hammockCirceJVM)
  .settings(moduleName := "hammock-docs")
  .settings(micrositeSettings: _*)
  .settings(noPublishSettings: _*)
  .enablePlugins(MicrositesPlugin)

lazy val core = crossProject.in(file("core"))
  .settings(moduleName := "hammock-core")
  .settings(commonSettings: _*)
  .jvmSettings(
  libraryDependencies ++= Seq(
    "org.apache.httpcomponents" % "httpclient" % "4.5.2",
    "org.mockito" % "mockito-all" % "1.10.18" % "test"
  ),
    crossScalaVersions := scalaVersions
  )
  .jsSettings(libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.1")
  .settings(scalacOptions ++= Seq(
    "-encoding", "UTF-8", // 2 args
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
  ))
  .settings(scalaVersion := "2.11.8")

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val hammockCirce = crossProject.in(file("hammock-circe"))
  .settings(moduleName := "hammock-circe")
  .settings(scalaVersion := "2.11.8")
  .settings(commonSettings: _*)
  .jvmSettings(crossScalaVersions := scalaVersions)
  .settings(libraryDependencies ++= Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % circeVersion))
  .dependsOn(core)

lazy val hammockCirceJVM = hammockCirce.jvm
lazy val hammockCirceJS = hammockCirce.js

lazy val example = project.in(file("example"))
  .settings(scalaVersion := "2.11.8")
  .settings(noPublishSettings: _*)
  .dependsOn(coreJVM, hammockCirceJVM)

val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)
