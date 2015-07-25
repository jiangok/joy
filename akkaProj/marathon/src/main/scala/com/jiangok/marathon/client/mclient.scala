package com.jiangok.marathon.client

import java.io.IOException
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source, Flow}
import scala.collection.immutable.HashMap
import scala.concurrent.{ExecutionContextExecutor, Future}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._


object MarathonRest extends Enumeration {
  type MarathonRest = Value
  val createApp, listApps, listApp, listAppVersion, listAppConfig, changeAppConfig, rollingRestartAppTasks,
  destroyApp,  listAppTasks, killAppTasks, killAppTask, listGroups, listGroup, createGroups, changeGroup,
  destroyGroup, listTasks, killTasks, listDeployments, deleteDeployment, attachEventStream, subscribEvents,
  listSubscriptions, unsubscribEvents, listStagingQueue, getMarathonInst, getLeader, reelectLeader,
  ping, logging, help, metrics = Value

  val apiMap = HashMap(
    MarathonRest.attachEventStream->(s"/events", 0),
    MarathonRest.changeAppConfig->(s"/apps/%d", 1),
    MarathonRest.changeGroup->(s"/groups/%d", 2),
    MarathonRest.createApp->(s"/apps", 3),
    MarathonRest.createGroups->(s"/groups", 4),
    MarathonRest.deleteDeployment->(s"/deployments/%d", 5),
    MarathonRest.destroyApp->(s"/apps/%d", 6),
    MarathonRest.destroyGroup->(s"/groups/%d", 7),
    MarathonRest.getLeader->(s"/leader", 8),
    MarathonRest.getMarathonInst->(s"/info", 9),
    MarathonRest.help->(s"/help", 10),
    MarathonRest.killAppTask->(s"/apps/%d/tasks/%d", 11),
    MarathonRest.killAppTasks->(s"/apps/%d/tasks", 12),
    MarathonRest.killTasks->(s"/tasks/delete", 13),
    MarathonRest.listApp->(s"/apps/%d", 14),
    MarathonRest.listAppConfig->(s"/apps/%d/versions/%d", 15),
    MarathonRest.listApps->(s"/apps", 16),
    MarathonRest.listAppTasks->(s"/apps/%d/tasks", 17),
    MarathonRest.listAppVersion->(s"/apps/%d/versions", 18),
    MarathonRest.listDeployments->(s"/deployments", 19),
    MarathonRest.listGroup->(s"/groups/%d", 20),
    MarathonRest.listGroups->(s"/groups", 21),
    MarathonRest.listStagingQueue->(s"/queue", 22),
    MarathonRest.listSubscriptions->(s"/eventSubscriptions", 23),
    MarathonRest.listTasks->(s"/tasks", 24),
    MarathonRest.logging->(s"/logging", 25),
    MarathonRest.metrics->(s"/metrics", 26),
    MarathonRest.ping->(s"/ping", 27),
    MarathonRest.reelectLeader->(s"/leader", 28),
    MarathonRest.rollingRestartAppTasks->(s"/apps/%d/restart", 29),
    MarathonRest.subscribEvents->(s"/eventSubscriptions", 30),
    MarathonRest.unsubscribEvents->(s"/eventSubscriptions", 31)
  )
}

trait MClient extends MarathonApiProtocol
{
  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: Materializer

  var host: String = null
  var port: Int = -1
  var version: String = "v2"

  lazy val connectionFlow: Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnection(host, port)

  def marathonRequest(request: HttpRequest): Future[HttpResponse] =
    Source.single(request).via(connectionFlow).runWith(Sink.head)

  def listApps() : Future[Either[String, Apps]] = {
    val (path, context) = MarathonRest.apiMap(MarathonRest.listApps)
    val url =  s"/${version}$path"
    //print("!!!!!" + url)

    marathonRequest(RequestBuilding.Get(url))
      .flatMap {
        response =>
          response.status match {
            case OK =>
             // response.entity.dataBytes.runForeach(bs => println("!!!!!!" + bs.decodeString("utf-8")))
              Unmarshal(response.entity).to[Apps].map(Right(_))
            case _ =>
              Unmarshal(response.entity).to[String].flatMap { entity =>
                val error = s"marathon request failed with status code ${response.status} and entity $entity"
                Future.failed(new IOException(error))
            }
        }
    }
  }
}

