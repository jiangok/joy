
lazy val commonSettings = Seq(
  organization := "com.lian.scalazstream",
  version := "0.1.0",
  scalaVersion := "2.11.6",
  resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
  libraryDependencies ++= Seq(
    "org.scalaz.stream" %% "scalaz-stream" % "0.7a",
    "org.scalaz" %% "scalaz-core" % "6.0.3"
  )
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "scalazstream"
  )