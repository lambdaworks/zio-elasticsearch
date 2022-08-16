ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

Global / onChangedBuildSource := ReloadOnSourceChanges

inThisBuild(
  List(
    homepage         := Some(url("https://github.com/lambdaworks/zio-elasticsearch/")),
    licenses         := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    organization     := "io.lambdaworks",
    startYear        := Some(2022)
  )
)

addCommandAlias("fmt", "all scalafmtSbt scalafmtAll")
addCommandAlias("fmtCheck", "all scalafmtSbtCheck scalafmtCheckAll")

lazy val root = (project in file("."))
  .settings(
    name := "zio-elasticsearch"
  )
