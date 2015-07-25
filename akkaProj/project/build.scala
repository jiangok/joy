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
      Resolver.bintrayRepo("mfglabs", "maven"),
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("snapshots")
     //"spray repo" at "http://repo.spray.io/",
     // "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
      //Resolver.sonatypeRepo("snapshots")
    ),

    libraryDependencies ++= Seq(akka_actor, junit,
      slf4j, log4j2_slf4j, log4j2_core, log4j2_api,
      akka_testkit, scalatest, leveldb, leveldbjni,
      akka_http_core, akka_http_spray, akka_http_testkit,
      apache_commons_lang, apache_common_io, akka_stream, json4sNative,
      mfglabs, shapeless, shapelessStream, rx, scalaz)
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
  val mfglabs = "com.mfglabs" %% "akka-stream-extensions" % "0.7.3"
  val shapeless = "com.chuusai" %% "shapeless" % "2.2.4"
  val shapelessStream = "com.mfglabs" %% "akka-stream-extensions-shapeless" % "0.8.0"
  val rx = "io.reactivex" %% "rxscala" % "0.25.0"

  val json4sNative = "org.json4s" %% "json4s-native" % "3.2.11"
  val akkaStreamV = "1.0"
  val akka_http_core = "com.typesafe.akka" %% "akka-http-core-experimental"          % akkaStreamV
  val akka_http_spray = "com.typesafe.akka" %% "akka-http-spray-json-experimental"    % akkaStreamV
  val akka_http_testkit = "com.typesafe.akka" %% "akka-http-testkit-experimental" % akkaStreamV
  val akka_stream = "com.typesafe.akka" %% "akka-stream-experimental"             % akkaStreamV
  val apache_commons_lang = "org.apache.commons" % "commons-lang3" % "3.3.2"
  val apache_common_io = "org.apache.commons" % "commons-io" % "1.3.2"
  val scalaz = "org.scalaz" %% "scalaz-core" % "7.1.3"

  lazy val root = Project("root", file(".")).
    aggregate(app, marathonClient).
    settings(basicSettings: _*).
    settings(
      name := "testsbt"
    )

  lazy val app = Project("app", file("root")).
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

  lazy val marathonClient = Project("marathonClient", file("marathon")).
    settings(basicSettings: _*)
}

