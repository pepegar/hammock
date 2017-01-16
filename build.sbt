organization in ThisBuild := "hammock"
scalaVersion := "2.11.8"


lazy val core = project.in(file("core"))
  .settings(moduleName := "hammock")
  .settings(version := "0.1-SNAPSHOT")
  .settings(libraryDependencies ++= Seq(
    "org.typelevel" %% "cats" % "0.8.1",
    "org.apache.httpcomponents" % "httpclient" % "4.5.2",
    compilerPlugin("org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.full),
    compilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3"),
    "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  ))
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


lazy val example = project.in(file("example"))
  .settings(scalaVersion := "2.11.8")
  .dependsOn(core)
