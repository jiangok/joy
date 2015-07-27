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

case class Task(appId: Option[String])

case class Tasks(tasks: Option[List[Task]])

case class Versions(versions: Option[List[String]])

case class RestartAppResponse(deploymentId: Option[String], version: Option[String])

case class Groups(id: Option[String], apps: List[App], version: Option[String])

trait MarathonApiProtocol extends DefaultJsonProtocol {
  implicit val containerFormat = jsonFormat2(Container.apply)
  implicit val constraintFormat = jsonFormat3(Constraint.apply)
  implicit val appFormat = jsonFormat15(App.apply)
  implicit val app2Format = jsonFormat1(App2.apply)
  implicit val appsFormat = jsonFormat1(Apps.apply)
  implicit val leaderFormat = jsonFormat1(Leader.apply)
  implicit val taskFormat = jsonFormat1(Task.apply)
  implicit val deploymentFormat = jsonFormat1(Deployment.apply)
  implicit val deploymentsFormat = listFormat(deploymentFormat)
  implicit val tasksFormat = jsonFormat1(Tasks.apply)
  implicit val versionsFormat = jsonFormat1(Versions.apply)
  implicit val restartAppResponseFormat = jsonFormat2(RestartAppResponse.apply)
  implicit val groupsFormat = jsonFormat3(Groups.apply)
}