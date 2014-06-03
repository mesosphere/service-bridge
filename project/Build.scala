import sbt._
import Keys._

import net.virtualvoid.sbt.graph.Plugin.graphSettings
import sbtassembly.Plugin._
import sbtassembly.Plugin.AssemblyKeys._
import com.typesafe.sbt.SbtScalariform._
import ohnosequences.sbt.SbtS3Resolver.S3Resolver
import ohnosequences.sbt.SbtS3Resolver.{ s3, s3resolver }
import scalariform.formatter.preferences._
import sbtunidoc.Plugin._

object ServiceBridgeBuild extends Build {

  //////////////////////////////////////////////////////////////////////////////
  // PROJECT INFO
  //////////////////////////////////////////////////////////////////////////////

  val ORGANIZATION = "mesosphere"
  val PROJECT_NAME = "service-bridge"
  val PROJECT_VERSION = "0.1.0-SNAPSHOT"
  val SCALA_VERSION = "2.10.4"


  //////////////////////////////////////////////////////////////////////////////
  // DEPENDENCY VERSIONS
  //////////////////////////////////////////////////////////////////////////////

  val AKKA_VERSION            = "2.3.2"
  val DISPATCH_VERSION        = "0.11.1"
  val LOGBACK_VERSION         = "1.1.2"
  val PLAY_JSON_VERSION       = "2.2.3"
  val SCALATEST_VERSION       = "2.1.5"
  val SERVICE_NET_VERSION     = "0.1.0-SNAPSHOT"
  val SLF4J_VERSION           = "1.7.6"
  val UNFILTERED_VERSION      = "0.7.1"


  //////////////////////////////////////////////////////////////////////////////
  // PROJECTS
  //////////////////////////////////////////////////////////////////////////////

  lazy val root = Project(
    id = PROJECT_NAME,
    base = file("."),
    settings = commonSettings ++
      Seq(
        aggregate in update := false,
        mainClass in (Compile, packageBin) :=
          Some("mesosphere.servicebridge.daemon.ServiceBridge"),
        mainClass in (Compile, run) :=
          Some("mesosphere.servicebridge.daemon.ServiceBridge")
      ) ++
      assemblySettings ++
      graphSettings
  ).dependsOn(config, daemon, http)
   .aggregate(config, daemon, http)

  def subproject(suffix: String) = s"${PROJECT_NAME}-$suffix"

  lazy val config = Project(
    id = subproject("config"),
    base = file("config"),
    settings = commonSettings
  )

  lazy val daemon = Project(
    id = subproject("daemon"),
    base = file("daemon"),
    settings = commonSettings ++ Seq(
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-actor"     % AKKA_VERSION,
        "com.typesafe.akka" %% "akka-slf4j"     % AKKA_VERSION,
        "com.typesafe.akka" %% "akka-testkit"   % AKKA_VERSION % "test"
      )
    )
  ).dependsOn(config, http)

  lazy val http = Project(
    id = subproject("http"),
    base = file("http"),
    settings = commonSettings ++ Seq(
      libraryDependencies ++= Seq(
        "mesosphere"        %% "service-net-http"  % SERVICE_NET_VERSION
                    exclude("com.typesafe", "config"),
        "com.typesafe.play" %% "play-json"         % PLAY_JSON_VERSION
            exclude("com.typesafe", "config"),
        "net.databinder"    %% "unfiltered-filter" % UNFILTERED_VERSION,
        "net.databinder"    %% "unfiltered-jetty"  % UNFILTERED_VERSION
      )
    )
  ).dependsOn(config)


  //////////////////////////////////////////////////////////////////////////////
  // SHARED SETTINGS
  //////////////////////////////////////////////////////////////////////////////

  lazy val commonSettings =
    Project.defaultSettings ++
    basicSettings ++
    formatSettings ++
    unidocSettings ++
    publishSettings

  lazy val basicSettings = Seq(
    version := PROJECT_VERSION,
    organization := ORGANIZATION,
    scalaVersion := SCALA_VERSION,

    resolvers ++= Seq(
      "Mesosphere Repo"     at "http://downloads.mesosphere.io/maven",
      "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
    ),

    libraryDependencies ++= Seq(
      "mesosphere"    %% "service-net-dsl"  % SERVICE_NET_VERSION,
      "mesosphere"    %% "service-net-util" % SERVICE_NET_VERSION,
      "mesosphere"    %% "service-net-util" % SERVICE_NET_VERSION % "test" classifier "tests",
      "org.scalatest" %% "scalatest"        % SCALATEST_VERSION   % "test"
    ),

    scalacOptions in Compile ++= Seq(
      "-unchecked",
      "-deprecation",
      "-feature"
    ),

    javacOptions in Compile ++= Seq(
      "-Xlint:unchecked",
      "-source", "1.7",
      "-target", "1.7"
    ),

    fork in Test := false
  )

  lazy val publishSettings = S3Resolver.defaults ++ Seq(
    publishTo := Some(s3resolver.value(
      "Mesosphere Public Repo",
      s3("downloads.mesosphere.io/maven")
    ))
  )

  lazy val formatSettings = scalariformSettings ++ Seq(
    ScalariformKeys.preferences := FormattingPreferences()
      .setPreference(IndentWithTabs, false)
      .setPreference(IndentSpaces, 2)
      .setPreference(AlignParameters, true)
      .setPreference(DoubleIndentClassDeclaration, true)
      .setPreference(MultilineScaladocCommentsStartOnFirstLine, false)
      .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, true)
      .setPreference(PreserveDanglingCloseParenthesis, true)
      .setPreference(CompactControlReadability, true)
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(PreserveSpaceBeforeArguments, true)
      .setPreference(SpaceBeforeColon, false)
      .setPreference(SpaceInsideBrackets, false)
      .setPreference(SpaceInsideParentheses, false)
      .setPreference(SpacesWithinPatternBinders, true)
      .setPreference(FormatXml, true)
    )

}
