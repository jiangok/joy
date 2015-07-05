package akkatestkitProj

// to create docker - docker:publishLocal

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
      "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
      Resolver.sonatypeRepo("snapshots")
    ),

    libraryDependencies ++= Seq(akka_actor, junit,
      slf4j, log4j2_slf4j, log4j2_core, log4j2_api,
      akka_testkit, scalatest, leveldb, leveldbjni, reactiveDocker)
  )

  val akka_actor = "com.typesafe.akka" %% "akka-actor" % "2.3.11"
  val akka_testkit = "com.typesafe.akka" %% "akka-testkit" % "2.3.11"
  val akka_persistence = "com.typesafe.akka" %% "akka-persistence-experimental" % "2.3.11"
  val junit = "junit" % "junit" % "4.12" % "test"
  val slf4j = "org.slf4j" % "slf4j-api" % "1.7.12" % "provided"
  val log4j2_api = "org.apache.logging.log4j" % "log4j-api" % "2.3" % "provided"
  val log4j2_core = "org.apache.logging.log4j" % "log4j-core" % "2.3" % "provided"
  val log4j2_slf4j = "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.3" % "provided"
  val scalatest = "org.scalatest" %% "scalatest" % "2.2.4" % "test"
  val leveldb = "org.iq80.leveldb" % "leveldb" % "0.7"
  val leveldbjni = "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"
  val reactiveDocker = "org.almoehi" %% "reactive-docker" % "0.1-SNAPSHOT"

  lazy val app = Project("app", file(".")).
    enablePlugins(JavaServerAppPackaging, DockerPlugin, UniversalPlugin).
    settings(basicSettings: _*).
    settings(
      assemblyJarName in assembly := "fatapp.jar",

      // docker creation
      packageName in Docker := "fatapp",
      version in Docker := version.value,
      dockerRepository := Some("jiangok"),
      dockerCommands in Docker <<= dockerCommands dependsOn assembly,
      dockerCommands := Seq(
        // enable ssh
        Cmd("FROM", "ubuntu:14.04"),
        Cmd("MAINTAINER", "Lian Jiang <abc@gmail.com"),
        ExecCmd("RUN", "apt-get", "update"),
        ExecCmd("RUN", "apt-get", "install", "openssh-server", "-y"),
        ExecCmd("RUN", "mkdir", "/var/run/sshd"),
        Cmd("RUN", "echo 'root:root' | chpasswd"),
        ExecCmd("RUN", "sed", "-i", "s/PermitRootLogin without-password/PermitRootLogin yes/", "/etc/ssh/sshd_config"),
        Cmd("RUN", "sed 's@session\\s*required\\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd"),
        ExecCmd("ENV", "NOTVISIBLE", "\"in users profile\""),
        Cmd("RUN", "echo \"export VISIBLE=now\" >> /etc/profile"),
        Cmd("EXPOSE", "22 5099"),

        // install openjdk instead of oracle jdk to avoid license issue.
        Cmd("RUN", "apt-get install -y openjdk-7-jre -y"),

        // install curl
        Cmd("RUN", "apt-get install curl libc6 libcurl3 zlib1g -y"),

        Cmd("ADD", s"opt/docker/lib/FatDockerEntryPoint.sh /app/"),

        // install service, base path of the src is ./can/target/docker/stage/
        Cmd("ADD", s"opt/docker/lib/${(assemblyJarName in assembly).value} /app/"),

        Cmd("CMD", "[\"/app/FatDockerEntryPoint.sh\"]")
      ),

      // removes all jar mappings in universal and appends the fat jar
      mappings in Universal := {
        // universalMappings: Seq[(File,String)]
        val universalMappings = (mappings in Universal).value
        println(s"universalMappings: $universalMappings, assemblyJarName: $assemblyJarName")
        val fatJar = (assembly in Compile).value
        println(s"fatJar: $fatJar")
        // removing means filtering
        val filtered = universalMappings filter {
          case (file, name) => !name.endsWith(".jar")
        }

        // add the fat jar
        val entryPointScript = new File("project/FatDockerEntryPoint.sh")
        if (entryPointScript.exists() == false)
          throw new IllegalArgumentException(s"$entryPointScript not exist! current folder: ${new File(".").getCanonicalPath()}")

        filtered :+ (fatJar -> ("lib/" + fatJar.getName)) :+
          (entryPointScript -> "lib/FatDockerEntryPoint.sh")

      },

      // the bash scripts classpath only needs the fat jar
      scriptClasspath := Seq( (assemblyJarName in assembly).value )
    )
}

