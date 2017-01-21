organization in ThisBuild := "hammock"
scalaVersion := "2.11.8"

wartremoverErrors in (Compile, compile) ++= Warts.all


lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats" % "0.8.1",
    compilerPlugin("org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.full),
    compilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3"),
    "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  ),
  wartremoverWarnings in Compile ++= Warts.all
)

lazy val core = project.in(file("core"))
  .settings(moduleName := "hammock-core")
  .settings(version := "0.1-SNAPSHOT")
  .settings(commonSettings: _*)
  .settings(libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.5.2")
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

val circeVersion = "0.6.1"

lazy val `hammock-circe` = project.in(file("hammock-circe"))
  .settings(scalaVersion := "2.11.8")
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % circeVersion))
  .dependsOn(core)

lazy val example = project.in(file("example"))
  .settings(scalaVersion := "2.11.8")
  .dependsOn(core, `hammock-circe`)
