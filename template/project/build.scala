package akkatestkitProj

import sbt._
import sbt.Keys._
import scala.collection.mutable.{Map}

// for fat jar
import sbtassembly.AssemblyKeys._

import com.typesafe.sbt.SbtNativePackager.autoImport._
import com.typesafe.sbt.packager.archetypes.JavaServerAppPackaging
import com.typesafe.sbt.packager.docker.{Cmd, ExecCmd, DockerPlugin}
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._
import com.typesafe.sbt.packager.universal.UniversalPlugin
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport._
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging.autoImport._

object AkkaBuild extends Build {

  lazy val basicSettings = Seq(
    organization := "com.lian.akka",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.11.6",

    resolvers ++= Seq(
      "spray repo" at "http://repo.spray.io/",
      "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
    ),

    libraryDependencies ++= Seq(akka_actor, junit,
      slf4j, log4j2_slf4j, log4j2_core, log4j2_api,
      akka_testkit, scalatest, leveldb, leveldbjni)
  )

  val akka_actor = "com.typesafe.akka" %% "akka-actor" % "2.3.11"
  val akka_testkit = "com.typesafe.akka" %% "akka-testkit" % "2.3.11"
  val akka_persistence = "com.typesafe.akka" %% "akka-persistence-experimental" % "2.3.11"
  val junit = "junit" % "junit" % "4.12" % "test"
  val slf4j = "org.slf4j" % "slf4j-api" % "1.7.12"
  val log4j2_api = "org.apache.logging.log4j" % "log4j-api" % "2.3"
  val log4j2_core = "org.apache.logging.log4j" % "log4j-core" % "2.3"
  val log4j2_slf4j = "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.3"
  val scalatest = "org.scalatest" %% "scalatest" % "2.2.4" % "test"
  val leveldb = "org.iq80.leveldb" % "leveldb" % "0.7"
  val leveldbjni = "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"
  
  lazy val app = Project("app", file(".")).
    settings(basicSettings: _*).
    settings(
      mainClass in (Compile,run) := Some("app"),

      assemblyJarName in assembly := "fatApp.jar"
    )
}

