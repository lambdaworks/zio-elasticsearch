import BuildHelper._

Global / onChangedBuildSource := ReloadOnSourceChanges

inThisBuild(
  List(
    homepage         := Some(url("https://github.com/lambdaworks/zio-elasticsearch/")),
    licenses         := List("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")),
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
    .aggregate(library, integration, example, docs)

lazy val library =
  project
    .in(file("modules/library"))
    .disablePlugins(RevolverPlugin)
    .enablePlugins(BuildInfoPlugin)
    .settings(stdSettings("zio-elasticsearch"))
    .settings(buildInfoSettings("zio.elasticsearch"))
    .settings(scalacOptions += "-language:higherKinds")
    .settings(
      libraryDependencies ++= List(
        "com.softwaremill.sttp.client4" %% "zio"             % "4.0.0-RC2",
        "com.softwaremill.sttp.client4" %% "zio-json"        % "4.0.0-RC2",
        "dev.zio"                       %% "zio-json"        % "0.7.39",
        "dev.zio"                       %% "zio-prelude"     % "1.0.0-RC39",
        "dev.zio"                       %% "zio-schema"      % "1.6.6",
        "dev.zio"                       %% "zio-schema-json" % "1.6.6",
        "org.apache.commons"             % "commons-lang3"   % "3.17.0",
        "dev.zio"                       %% "zio-test"        % "2.1.16" % Test,
        "dev.zio"                       %% "zio-test-sbt"    % "2.1.16" % Test
      ),
      testFrameworks := List(new TestFramework("zio.test.sbt.ZTestFramework"))
    )

lazy val integration =
  project
    .in(file("modules/integration"))
    .disablePlugins(RevolverPlugin)
    .settings(stdSettings("integration"))
    .dependsOn(library % "test->test")
    .settings(
      publish / skip := true
    )

lazy val example =
  project
    .in(file("modules/example"))
    .settings(stdSettings("example"))
    .settings(scalacOptions += "-language:higherKinds")
    .settings(
      crossScalaVersions := List(Scala213),
      publish / skip     := true,
      libraryDependencies ++= List(
        "dev.zio" %% "zio"                 % "2.1.16",
        "dev.zio" %% "zio-config"          % "4.0.4",
        "dev.zio" %% "zio-config-magnolia" % "4.0.4",
        "dev.zio" %% "zio-config-typesafe" % "4.0.4",
        "dev.zio" %% "zio-http"            % "3.1.0",
        "dev.zio" %% "zio-json"            % "0.7.39",
        "dev.zio" %% "zio-schema"          % "1.6.6",
        "dev.zio" %% "zio-schema-json"     % "1.6.6"
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
