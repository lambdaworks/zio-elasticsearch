import sbt._
import Keys._
import scalafix.sbt.ScalafixPlugin.autoImport._

object BuildHelper {

  val Scala212: String   = "2.12.17"
  val Scala213: String   = "2.13.10"
  val ScalaDotty: String = "3.2.2"

  def stdSettings(prjName: String) =
    List(
      name                     := s"$prjName",
      crossScalaVersions       := List(Scala212, Scala213, ScalaDotty),
      ThisBuild / scalaVersion := Scala213,
      scalacOptions            := stdOptions ++ extraOptions(scalaVersion.value),
      semanticdbEnabled        := scalaVersion.value != ScalaDotty, // enable SemanticDB
      semanticdbOptions += "-P:semanticdb:synthetics:on",
      semanticdbVersion                                          := scalafixSemanticdb.revision,
      ThisBuild / scalafixScalaBinaryVersion                     := CrossVersion.binaryScalaVersion(scalaVersion.value),
      ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0",
      Test / parallelExecution                                   := true,
      incOptions ~= (_.withLogRecompileOnMacro(false)),
      autoAPIMappings := true
    )

  private val stdOptions = Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-unchecked"
  )

  private val std2xOptions = Seq(
    "-language:higherKinds",
    "-language:existentials",
    "-explaintypes",
    "-Yrangepos",
    "-Xlint:_,-missing-interpolator,-type-parameter-shadow",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard"
  )

  def extraOptions(scalaVersion: String) =
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((3, 2)) =>
        Seq(
          "-language:implicitConversions",
          "-Xignore-scala2-macros"
        )
      case Some((2, 13)) =>
        Seq(
          "-Ywarn-unused:params,-implicits"
        ) ++ std2xOptions
      case Some((2, 12)) =>
        Seq(
          "-opt-warnings",
          "-Ywarn-extra-implicit",
          "-Ywarn-unused:_,imports",
          "-Ywarn-unused:imports",
          "-Ypartial-unification",
          "-Yno-adapted-args",
          "-Ywarn-inaccessible",
          "-Ywarn-infer-any",
          "-Ywarn-nullary-override",
          "-Ywarn-nullary-unit",
          "-Ywarn-unused:params,-implicits",
          "-Xfuture",
          "-Xsource:2.13",
          "-Xmax-classfile-name",
          "242"
        )
      case _ => Seq()
    }
}
