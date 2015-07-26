package com.jiangok.marathon.client

import spray.json.DefaultJsonProtocol

case class Container (image: Option[String], options: Option[List[String]])

case class Constraint (
  field        : Option[String],
  operator     : Option[String],
  value        : Option[String]
)

case class App (
  id           : Option[String],
  cmd          : Option[String],
  env          : Option[Map[String, String]],
  instances    : Option[Int],
  cpus         : Option[Double],
  mem          : Option[Double],
  executor     : Option[String],
  constraints  : Option[List[Constraint]],
  uris         : Option[List[String]],
  ports        : Option[List[Int]],
  requirePorts : Option[Boolean],
  container    : Option[Container],
  backoffSeconds : Option[Int],
  backoffFactor  : Option[Double],
  maxLaunchDelaySeconds : Option[Int]
)

case class App2 (app: App)

case class Apps(apps: Option[List[App]])

case class Leader(leader: Option[String])

case class Deployment(id: Option[String])

trait MarathonApiProtocol extends DefaultJsonProtocol {
  implicit val containerFormat = jsonFormat2(Container.apply)
  implicit val constraintFormat = jsonFormat3(Constraint.apply)
  implicit val appFormat = jsonFormat15(App.apply)
  implicit val app2Format = jsonFormat1(App2.apply)
  implicit val appsFormat = jsonFormat1(Apps.apply)
  implicit val leaderFormat = jsonFormat1(Leader.apply)
  implicit val deploymentFormat = jsonFormat1(Deployment.apply)
  implicit val deploymentsFormat = listFormat(deploymentFormat)
}