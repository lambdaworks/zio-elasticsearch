import BuildHelper._

Global / onChangedBuildSource := ReloadOnSourceChanges

inThisBuild(
  List(
    homepage         := Some(uri("https://github.com/lambdaworks/zio-elasticsearch/")),
    licenses         := List("Apache-2.0" -> uri("https://www.apache.org/licenses/LICENSE-2.0")),
    organization     := "io.lambdaworks",
    organizationName := "LambdaWorks",
    startYear        := Some(2022),
    developers       := List(
      Developer(
        "lambdaworks",
        "LambdaWorks' Team",
        "admin@lambdaworks.io",
        uri("https://github.com/lambdaworks")
      )
    )
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
    .aggregate(library, integration, benchmarks, example, docs)

lazy val library =
  project
    .in(file("modules/library"))
    .enablePlugins(BuildInfoPlugin)
    .settings(stdSettings("zio-elasticsearch"))
    .settings(buildInfoSettings("zio.elasticsearch"))
    .settings(scalacOptions += "-language:higherKinds")
    .settings(
      libraryDependencies ++= List(
        "com.softwaremill.sttp.client4" %% "zio"             % "4.1.3",
        "com.softwaremill.sttp.client4" %% "zio-json"        % "4.1.3",
        "dev.zio"                       %% "zio-json"        % "1.1.2",
        "dev.zio"                       %% "zio-prelude"     % "1.0.0-RC49",
        "dev.zio"                       %% "zio-schema"      % "1.9.2",
        "dev.zio"                       %% "zio-schema-json" % "1.9.2",
        "org.apache.commons"             % "commons-lang3"   % "3.22.0",
        "dev.zio"                       %% "zio-test"        % "2.1.27" % Test,
        "dev.zio"                       %% "zio-test-sbt"    % "2.1.27" % Test
      ),
      testFrameworks := List(new TestFramework("zio.test.sbt.ZTestFramework"))
    )

lazy val integration =
  project
    .in(file("modules/integration"))
    .settings(stdSettings("integration"))
    .dependsOn(library % "test->test")
    .settings(
      publish / skip := true
    )

lazy val benchmarks =
  project
    .in(file("modules/benchmarks"))
    .enablePlugins(JmhPlugin)
    .settings(stdSettings("benchmarks"))
    .settings(
      crossScalaVersions := List(Scala213),
      publish / skip     := true,
      libraryDependencies ++= List(
        "co.elastic.clients"            % "elasticsearch-java"                   % "8.19.25",
        "com.fasterxml.jackson.module" %% "jackson-module-scala"                 % "2.22.4",
        "nl.gn0s1s"                    %% "elastic4s-client-esjava"              % "8.19.3",
        "nl.gn0s1s"                    %% "elastic4s-effect-zio"                 % "8.19.3",
        "nl.gn0s1s"                    %% "elastic4s-json-jackson"               % "8.19.3",
        "org.elasticsearch.client"      % "elasticsearch-rest-high-level-client" % "7.17.29"
      )
    )
    .dependsOn(library)

lazy val example =
  project
    .in(file("modules/example"))
    .settings(stdSettings("example"))
    .settings(scalacOptions += "-language:higherKinds")
    .settings(
      crossScalaVersions := List(Scala213),
      publish / skip     := true,
      libraryDependencies ++= List(
        "dev.zio" %% "zio"                 % "2.1.27",
        "dev.zio" %% "zio-config"          % "4.1.2",
        "dev.zio" %% "zio-config-magnolia" % "4.1.2",
        "dev.zio" %% "zio-config-typesafe" % "4.1.2",
        "dev.zio" %% "zio-http"            % "3.11.6",
        "dev.zio" %% "zio-json"            % "1.1.2",
        "dev.zio" %% "zio-schema"          % "1.9.2",
        "dev.zio" %% "zio-schema-json"     % "1.9.2"
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
      mdocOut        := baseDirectory.value / "target" / "mdoc",
      scalacOptions -= "-Yno-imports",
      scalacOptions -= "-Xfatal-warnings",
      ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(library),
      ScalaUnidoc / unidoc / target              := (LocalRootProject / baseDirectory).value / "website" / "static" / "api",
      cleanFiles += (ScalaUnidoc / unidoc / target).value,
      docusaurusCreateSite     := docusaurusCreateSite.dependsOn(Compile / unidoc).value,
      docusaurusPublishGhpages := docusaurusPublishGhpages.dependsOn(Compile / unidoc).value
    )
