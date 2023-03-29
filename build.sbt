import BuildHelper._
import sbt.librarymanagement.Configurations.IntegrationTest

Global / onChangedBuildSource := ReloadOnSourceChanges

inThisBuild(
  List(
    homepage         := Some(url("https://github.com/lambdaworks/zio-elasticsearch/")),
    licenses         := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    organization     := "io.lambdaworks",
    organizationName := "LambdaWorks",
    startYear        := Some(2022),
    developers := List(
      Developer(
        "lambdaworks",
        "LambdaWorks' Team",
        "admin@lambdaworks.io",
        url("https://github.com/lambdaworks")
      )
    ),
    sonatypeCredentialHost := "s01.oss.sonatype.org",
    sonatypeRepository     := "https://s01.oss.sonatype.org/service/local"
  )
)

addCommandAlias("check", "fixCheck; fmtCheck; headerCheck")
addCommandAlias("fix", "scalafixAll")
addCommandAlias("fixCheck", "scalafixAll --check")
addCommandAlias("fmt", "all scalafmtSbt scalafmtAll")
addCommandAlias("fmtCheck", "all scalafmtSbtCheck scalafmtCheckAll")
addCommandAlias("prepare", "fix; fmt; headerCreate")

lazy val root =
  project
    .in(file("."))
    .settings(publish / skip := true)
    .aggregate(library, example, docs)

lazy val library =
  project
    .in(file("modules/library"))
    .disablePlugins(RevolverPlugin)
    .settings(stdSettings("zio-elasticsearch"))
    .settings(scalacOptions += "-language:higherKinds")
    .configs(IntegrationTest)
    .settings(
      Defaults.itSettings,
      libraryDependencies ++= List(
        "com.softwaremill.sttp.client4" %% "zio"             % "4.0.0-M1",
        "com.softwaremill.sttp.client4" %% "zio-json"        % "4.0.0-M1",
        "dev.zio"                       %% "zio-json"        % "0.5.0",
        "dev.zio"                       %% "zio-prelude"     % "1.0.0-RC18",
        "dev.zio"                       %% "zio-schema"      % "0.4.9",
        "dev.zio"                       %% "zio-schema-json" % "0.4.9",
        "org.apache.commons"             % "commons-lang3"   % "3.12.0",
        "dev.zio"                       %% "zio-test"        % "2.0.10" % Tests,
        "dev.zio"                       %% "zio-test-sbt"    % "2.0.10" % Tests
      ),
      testFrameworks := List(new TestFramework("zio.test.sbt.ZTestFramework"))
    )

lazy val example =
  project
    .in(file("modules/example"))
    .settings(stdSettings("example"))
    .settings(scalacOptions += "-language:higherKinds")
    .settings(
      publish / skip := true,
      libraryDependencies ++= List(
        "dev.zio" %% "zio"                 % "2.0.10",
        "dev.zio" %% "zio-config"          % "3.0.7",
        "dev.zio" %% "zio-config-magnolia" % "3.0.7",
        "dev.zio" %% "zio-config-typesafe" % "3.0.7",
        "dev.zio" %% "zio-http"            % "0.0.5",
        "dev.zio" %% "zio-json"            % "0.5.0",
        "dev.zio" %% "zio-schema"          % "0.4.9",
        "dev.zio" %% "zio-schema-json"     % "0.4.9"
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
    .disablePlugins(RevolverPlugin)
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
