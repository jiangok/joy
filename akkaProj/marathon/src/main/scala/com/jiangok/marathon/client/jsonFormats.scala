package com.jiangok.marathon.client

import spray.json.DefaultJsonProtocol

case class Container (image: Option[String], options: Option[List[String]])

case class Constraint (
  field        : Option[String],
  operator     : Option[String],
  value        : Option[String]
)

case class App (
  id           : String,
  cmd          : Option[String],
  env          : Option[Map[String, String]],
  instances    : Int,
  cpus         : Double,
  mem          : Double,
  executor     : Option[String],
  constraints  : Option[List[Constraint]],
  uris         : Option[List[String]],
  ports        : Option[List[Int]],
  requirePorts : Boolean,
  container    : Option[Container],
  backoffSeconds : Int,
  backoffFactor  : Double,
  maxLaunchDelaySeconds : Int
)

case class Apps(apps: List[App])

trait MarathonApiProtocol extends DefaultJsonProtocol {
  implicit val containerFormat = jsonFormat2(Container.apply)
  implicit val constraintFormat = jsonFormat3(Constraint.apply)
  implicit val appFormat = jsonFormat15(App.apply)
  implicit val appsFormat = jsonFormat1(Apps.apply)
}