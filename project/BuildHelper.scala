import sbt._
import Keys._
import scalafix.sbt.ScalafixPlugin.autoImport._

object BuildHelper {

  val Scala212: String = "2.12.16"
  val Scala213: String = "2.13.8"

  def stdSettings(prjName: String) =
    List(
      name                     := s"$prjName",
      crossScalaVersions       := List(Scala212, Scala213),
      ThisBuild / scalaVersion := Scala213,
      scalacOptions            := stdOptions,
      semanticdbEnabled        := true,
      semanticdbOptions += "-P:semanticdb:synthetics:on",
      semanticdbVersion                                          := scalafixSemanticdb.revision,
      ThisBuild / scalafixScalaBinaryVersion                     := CrossVersion.binaryScalaVersion(scalaVersion.value),
      ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0",
      Test / parallelExecution                                   := true,
      incOptions ~= (_.withLogRecompileOnMacro(false)),
      autoAPIMappings := true
    )

  private val stdOptions =
    List("-deprecation", "-encoding", "UTF-8", "-feature", "-unchecked", "-Xfatal-warnings")

}
