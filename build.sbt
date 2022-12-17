import BuildHelper._
import sbt.librarymanagement.Configurations.IntegrationTest

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
    .settings(scalacOptions += "-language:higherKinds")
    .configs(IntegrationTest)
    .settings(
      Defaults.itSettings,
      libraryDependencies ++= List(
        "com.softwaremill.sttp.client3" %% "zio"             % "3.8.3",
        "com.softwaremill.sttp.client3" %% "zio-json"        % "3.8.3",
        "dev.zio"                       %% "zio-json"        % "0.3.0",
        "dev.zio"                       %% "zio-prelude"     % "1.0.0-RC16",
        "dev.zio"                       %% "zio-schema"      % "0.3.1",
        "dev.zio"                       %% "zio-schema-json" % "0.3.1",
        "org.apache.commons"             % "commons-lang3"   % "3.12.0",
        "dev.zio"                       %% "zio-test"        % "2.0.5" % Tests,
        "dev.zio"                       %% "zio-test-sbt"    % "2.0.5" % Tests
      ),
      testFrameworks := List(new TestFramework("zio.test.sbt.ZTestFramework"))
    )

lazy val example =
  project
    .in(file("modules/example"))
    .settings(stdSettings("example"))
    .settings(
      libraryDependencies ++= List(
        "dev.zio" %% "zio" % "2.0.5"
      )
    )
    .dependsOn(library)
    .settings(
      publish / skip := true
    )

lazy val docs =
  project
    .in(file("modules/docs"))
    .enablePlugins(MdocPlugin, DocusaurusPlugin, ScalaUnidocPlugin)
    .dependsOn(library)
    .settings(
      publish / skip := true,
      moduleName     := "docs",
      scalacOptions -= "-Yno-imports",
      scalacOptions -= "-Xfatal-warnings",
      ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(library),
      ScalaUnidoc / unidoc / target              := (LocalRootProject / baseDirectory).value / "website" / "static" / "api",
      cleanFiles += (ScalaUnidoc / unidoc / target).value,
      docusaurusCreateSite     := docusaurusCreateSite.dependsOn(Compile / unidoc).value,
      docusaurusPublishGhpages := docusaurusPublishGhpages.dependsOn(Compile / unidoc).value
    )

val Tests = List(IntegrationTest, Test).mkString(",")
