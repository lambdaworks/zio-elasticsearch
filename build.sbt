import BuildHelper._

Global / onChangedBuildSource := ReloadOnSourceChanges

inThisBuild(
  List(
    homepage         := Some(url("https://github.com/lambdaworks/zio-elasticsearch/")),
    licenses         := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    organization     := "io.lambdaworks",
    organizationName := "LambdaWorks",
    startYear        := Some(2022)
  )
)

addCommandAlias("check", "fixCheck; fmtCheck")
addCommandAlias("fix", "scalafixAll")
addCommandAlias("fixCheck", "scalafixAll --check")
addCommandAlias("fmt", "all scalafmtSbt scalafmtAll")
addCommandAlias("fmtCheck", "all scalafmtSbtCheck scalafmtCheckAll")
addCommandAlias("prepare", "fix; fmt")

lazy val root =
  project
    .in(file("."))
    .aggregate(library, example)

lazy val library =
  project
    .in(file("modules/library"))
    .settings(stdSettings("zio-elasticsearch"))

lazy val example =
  project
    .in(file("modules/example"))
    .settings(stdSettings("example"))
    .dependsOn(library)
    .settings(
      publish / skip := true
    )
