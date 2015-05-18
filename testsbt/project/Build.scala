import sbt._
import Keys._


object testsbtBuild extends Build {
  import BuildSettings._
  import Dependencies._

  lazy val root = Project("root", file(".")).
    aggregate(mod1, mod2).
    settings(basicSettings: _*).
    settings(
      name := "testsbt"
    )

  lazy val mod1 = Project("mod1", file("mod1")).
    settings(basicSettings: _*).
    settings(
      libraryDependencies ++= Seq(spray_routing, spray_can)
    )

  lazy val mod2 = Project("mod2", file("mod2")).
    settings(basicSettings: _*).
    settings(
      libraryDependencies ++= Seq(spray_routing, spray_can)
    )
}