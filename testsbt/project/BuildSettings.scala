import sbt._
import Keys._

object BuildSettings {
  lazy val basicSettings = Seq(
    organization := "com.lian",
    version := "0.1.0",
    scalaVersion := "2.11.6"
  )
}
