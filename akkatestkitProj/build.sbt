name := "akkatestkitProj"

version := "1.0"

scalaVersion := "2.11.7"

//resolvers +=  "Akka Repo" at "http://repo.akka.io/releases"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.11",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.11",
  "com.typesafe.akka" %% "akka-persistence-experimental" % "2.3.11",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.iq80.leveldb"            % "leveldb"          % "0.7",
  "org.fusesource.leveldbjni"   % "leveldbjni-all"   % "1.8"
)