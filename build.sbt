organization in ThisBuild := "com.pepegar"
scalaVersion in ThisBuild := "2.11.8"
licenses in ThisBuild := Seq(("MIT", url("http://opensource.org/licenses/MIT")))

val scalaVersions = Seq("2.11.8", "2.12.0")

val versions = Map(
  "circe" -> "0.7.0",
  "monocle" -> "1.4.0",
  "atto" -> "0.5.2",
  "cats" -> "0.9.0",
  "scalatest" -> "3.0.1",
  "scalacheck" -> "1.13.4",
  "discipline" -> "0.7.3",
  "macro-paradise" -> "2.1.0",
  "kind-projector" -> "0.9.4"
)

val micrositeSettings = Seq(
  micrositeName := "Hammock",
  micrositeDescription := "Purely functional HTTP client",
  micrositeBaseUrl := "hammock",
  micrositeDocumentationUrl := "/hammock/docs.html",
  micrositeGithubOwner := "pepegar",
  micrositeGithubRepo := "hammock",
  micrositeHighlightTheme := "tomorrow"
)

val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats" % versions("cats"),
    "com.github.julien-truffaut" %%  "monocle-core"  % versions("monocle"),
    "com.github.julien-truffaut" %%  "monocle-macro" % versions("monocle"),
    "org.tpolecat" %% "atto-core" % versions("atto"),
    "org.tpolecat" %% "atto-compat-cats" % versions("atto"),
    compilerPlugin("org.scalamacros" %% "paradise" % versions("macro-paradise") cross CrossVersion.full),
    compilerPlugin("org.spire-math" %% "kind-projector" % versions("kind-projector")),
    "org.scalatest" %% "scalatest" % versions("scalatest") % "test",
    "org.scalacheck" %% "scalacheck" % versions("scalacheck") % "test",
    "org.typelevel" %% "discipline" % versions("discipline") % "test"
  ),
  bintrayRepository := "com.pepegar"
)

lazy val docs = project.in(file("docs"))
  .dependsOn(coreJVM, hammockCirceJVM)
  .settings(moduleName := "hammock-docs")
  .settings(micrositeSettings: _*)
  .settings(noPublishSettings: _*)
  .enablePlugins(MicrositesPlugin)

lazy val readmeSettings = tutSettings ++ Seq(
    tutSourceDirectory := baseDirectory.value,
    tutTargetDirectory := baseDirectory.value.getParentFile,
    tutScalacOptions ~= (_.filterNot(Set("-Ywarn-unused-import", "-Ywarn-dead-code"))),
    tutScalacOptions ++= (scalaBinaryVersion.value match {
      case "2.10" => Seq("-Xdivergence211")
      case _      => Nil
    }),
    tutNameFilter := """README.md""".r
  )

lazy val readme = (project in file("tut"))
  .settings(
    moduleName := "hammock-readme"
  )
  .dependsOn(coreJVM, hammockCirceJVM)
  .settings(readmeSettings: _*)
  .settings(noPublishSettings)


lazy val core = crossProject.in(file("core"))
  .settings(moduleName := "hammock-core")
  .settings(commonSettings: _*)
  .settings(crossScalaVersions := scalaVersions)
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.apache.httpcomponents" % "httpclient" % "4.5.2",
      "org.mockito" % "mockito-all" % "1.10.18" % "test"
    )
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
  .settings(crossScalaVersions := scalaVersions)
  .settings(libraryDependencies ++= Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % versions("circe")))
  .dependsOn(core)

lazy val hammockCirceJVM = hammockCirce.jvm
lazy val hammockCirceJS = hammockCirce.js

lazy val example = project.in(file("example"))
  .settings(scalaVersion := "2.11.8")
  .settings(noPublishSettings: _*)
  .dependsOn(coreJVM, hammockCirceJVM)

lazy val exampleJS = project.in(file("example-js"))
  .enablePlugins(ScalaJSPlugin)
  .settings(scalaVersion := "2.11.8")
  .settings(noPublishSettings: _*)
  .settings(libraryDependencies ++= Seq(
    "org.typelevel" %%% "cats" % versions("cats"),
    "io.circe" %%% "circe-core" % versions("circe"),
    "io.circe" %%% "circe-generic" % versions("circe"),
    "io.circe" %%% "circe-parser" % versions("circe"),
    "org.tpolecat" %%% "atto-core" % versions("atto"),
    "org.tpolecat" %%% "atto-compat-cats" % versions("atto"),
    "org.scala-js" %%% "scalajs-dom" % "0.9.1",
    "be.doeraene" %%% "scalajs-jquery" % "0.9.1"
  ))
  .settings(jsDependencies += "org.webjars" % "jquery" % "2.1.3" / "2.1.3/jquery.js")
  .dependsOn(coreJS, hammockCirceJS)

val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)
