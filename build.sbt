import ReleaseTransformations._

val Versions = Map(
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


val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

val publishSettings = Seq(
  publishMavenStyle := true,
    publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false,
  homepage := Some(url("https://github.com/pepegar/hammock")),
  pomIncludeRepository := Function.const(false),
  pomExtra := (
    <scm>
      <url>git@github.com:pepegar/hammock.git</url>
      <connection>scm:git:git@github.com:pepegar/hammock.git</connection>
    </scm>
    <developers>
      <developer>
        <id>pepegar</id>
        <name>Pepe García</name>
        <url>http://pepegar.com</url>
      </developer>
    </developers>
  ),
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
    ReleaseStep(action = Command.process("publishSigned", _)),
    setNextVersion,
    commitNextVersion,
    ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
    pushChanges
  )
)

val buildSettings = Seq(
  organization  := "com.pepegar",
  scalaVersion  := "2.12.1",
  licenses  := Seq(("MIT", url("http://opensource.org/licenses/MIT"))),
  crossScalaVersions := Seq("2.11.8", "2.12.1"),
  scalacOptions ++= Seq(
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
  )
)

val commonDependencies = Seq(
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats" % Versions("cats"),
    "com.github.julien-truffaut" %%  "monocle-core"  % Versions("monocle"),
    "com.github.julien-truffaut" %%  "monocle-macro" % Versions("monocle"),
    "org.tpolecat" %% "atto-core" % Versions("atto"),
    "org.tpolecat" %% "atto-compat-cats" % Versions("atto"),
    "org.scalatest" %% "scalatest" % Versions("scalatest") % "test",
    "org.scalacheck" %% "scalacheck" % Versions("scalacheck") % "test",
    "org.typelevel" %% "discipline" % Versions("discipline") % "test",
    compilerPlugin("org.scalamacros" %% "paradise" % Versions("macro-paradise") cross CrossVersion.full),
    compilerPlugin("org.spire-math" %% "kind-projector" % Versions("kind-projector"))
  )
)

lazy val hammock = project.in(file("."))
  .settings(buildSettings)
  .settings(noPublishSettings)
  .dependsOn(coreJVM, coreJS, circeJVM, circeJS)
  .aggregate(coreJVM, coreJS, circeJVM, circeJS)
  .enablePlugins(ScalaUnidocPlugin)


lazy val core = crossProject.in(file("core"))
  .settings(moduleName := "hammock-core")
  .settings(buildSettings: _*)
  .settings(commonDependencies: _*)
  .settings(publishSettings: _*)
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.apache.httpcomponents" % "httpclient" % "4.5.2",
      "org.mockito" % "mockito-all" % "1.10.18" % "test"
    )
  )
  .jsSettings(libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.1")

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val circe = crossProject.in(file("hammock-circe"))
  .settings(moduleName := "hammock-circe")
  .settings(buildSettings: _*)
  .settings(commonDependencies: _*)
  .settings(publishSettings: _*)
  .settings(libraryDependencies ++= Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % Versions("circe")))
  .dependsOn(core)

lazy val circeJVM = circe.jvm
lazy val circeJS = circe.js

lazy val docs = project.in(file("docs"))
  .dependsOn(coreJVM, circeJVM)
  .settings(moduleName := "hammock-docs")
  .settings(buildSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(
    micrositeName := "Hammock",
    micrositeDescription := "Purely functional HTTP client",
    micrositeBaseUrl := "hammock",
    micrositeDocumentationUrl := "/hammock/docs.html",
    micrositeGithubOwner := "pepegar",
    micrositeGithubRepo := "hammock",
    micrositeHighlightTheme := "tomorrow"
  )
  .enablePlugins(MicrositesPlugin)

lazy val readme = (project in file("tut"))
  .settings(moduleName := "hammock-readme")
  .dependsOn(coreJVM, circeJVM)
  .settings(tutSettings: _*)
  .settings(buildSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(
    tutSourceDirectory := baseDirectory.value,
    tutTargetDirectory := baseDirectory.value.getParentFile,
    tutScalacOptions ~= (_.filterNot(Set("-Ywarn-unused-import", "-Ywarn-dead-code"))),
    tutScalacOptions ++= (scalaBinaryVersion.value match {
      case "2.10" => Seq("-Xdivergence211")
      case _      => Nil
    }),
    tutNameFilter := """README.md""".r
  )

lazy val example = project.in(file("example"))
  .settings(buildSettings: _*)
  .settings(noPublishSettings: _*)
  .dependsOn(coreJVM, circeJVM)

lazy val exampleJS = project.in(file("example-js"))
  .enablePlugins(ScalaJSPlugin)
  .settings(buildSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(libraryDependencies ++= Seq(
    "org.typelevel" %%% "cats" % Versions("cats"),
    "io.circe" %%% "circe-core" % Versions("circe"),
    "io.circe" %%% "circe-generic" % Versions("circe"),
    "io.circe" %%% "circe-parser" % Versions("circe"),
    "org.tpolecat" %%% "atto-core" % Versions("atto"),
    "org.tpolecat" %%% "atto-compat-cats" % Versions("atto"),
    "org.scala-js" %%% "scalajs-dom" % "0.9.1",
    "be.doeraene" %%% "scalajs-jquery" % "0.9.1"
  ))
  .settings(jsDependencies += "org.webjars" % "jquery" % "2.1.3" / "2.1.3/jquery.js")
  .dependsOn(coreJS, circeJS)
