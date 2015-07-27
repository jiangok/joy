package com.jiangok.marathon.client

import java.io.IOException
import akka.actor.ActorSystem
import akka.http.impl.util.JavaMapping.HttpHeader
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ResponseEntity, HttpResponse, HttpRequest}
import akka.http.scaladsl.unmarshalling.{Unmarshaller, Unmarshal}
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source, Flow}
import com.jiangok.marathon.client.MarathonRest.MarathonRest
import scala.collection.immutable.HashMap
import scala.concurrent.{Await, ExecutionContextExecutor, Future}

// MUST have for marshalling
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import scala.concurrent.duration._


object MarathonRest extends Enumeration {
  type MarathonRest = Value
  val createApp, listApps, listApp, listVersions, listAppConfig, changeAppConfig, restartApp,
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
    MarathonRest.listApp->(s"/apps/%s", 14),
    MarathonRest.listAppConfig->(s"/apps/%d/versions/%d", 15),
    MarathonRest.listApps->(s"/apps", 16),
    MarathonRest.listAppTasks->(s"/apps/%s/tasks", 17),
    MarathonRest.listVersions->(s"/apps/%s/versions", 18),
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
    MarathonRest.restartApp->(s"/apps/%s/restart", 29),
    MarathonRest.subscribEvents->(s"/eventSubscriptions", 30),
    MarathonRest.unsubscribEvents->(s"/eventSubscriptions", 31)
  )
}

trait MClient extends MarathonApiProtocol {
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

  def getVersionedUrl(template: String, parameters: Any*) = {
    var url = s"/$version$template"
    url = url.format(parameters: _*)
    println(url)
    url
  }

  def getUrl(template: String, parameters: Any*) = {
    var url = s"$template"
    url = url.format(parameters: _*)
    println(url)
    url
  }


  //
  // get calls
  //

  def listApps(): Future[Either[String, Apps]] = {
    getStuff[Apps](MarathonRest.listApps)
  }

  def listApp(appId: String): Future[Either[String, App2]] = {
    getStuff[App2](MarathonRest.listApp, appId)
  }

  def getLeader(): Future[Either[String, Leader]] = {
    getStuff[Leader](MarathonRest.getLeader)
  }

  def listDeployments(): Future[Either[String, Deployment]] = {
    getStuff[Deployment](MarathonRest.listDeployments)
  }

  def listTasks(): Future[Either[String, Tasks]] = {
    getStuff[Tasks](MarathonRest.listTasks)
  }

  def listVersions(appId: String) : Future[Either[String, Versions]] = {
    getStuff[Versions](MarathonRest.listVersions, appId)
  }

  def listGroups() : Future[Either[String, Groups]] = {
    getStuff[Groups](MarathonRest.listGroups)
  }

  def ping() : Future[Either[String, String]] = {
    val (path, context) = MarathonRest.apiMap(MarathonRest.ping)
    getStuff[String](getUrl(path))
  }

  /*
  // very complicate schema
  def metrics() : Future[Either[String, String]] = {
    val (path, context) = MarathonRest.apiMap(MarathonRest.ping)
    getStuff[String](getUrl(path))
  }*/

  def getStuff[R](op: MarathonRest.MarathonRest, params: Any*)
  (implicit um: Unmarshaller[ResponseEntity, R]): Future[Either[String, R]] =
  {
    val (path, context) = MarathonRest.apiMap(op)

    getStuff[R](getVersionedUrl(path, params: _*))
  }

  def getStuff[R](url: String)
                 (implicit um: Unmarshaller[ResponseEntity, R]): Future[Either[String, R]] = {

    marathonRequest(
      RequestBuilding
        .Get(url)
        .withHeaders(RawHeader("accept", "application/json")))
      .flatMap {
      response =>
        response.status match {
          case OK =>
            //response.entity.dataBytes.runForeach(bs => println("!!!!!!" + bs.decodeString("utf-8")))
            Unmarshal(response.entity).to[R].map(Right(_))
          case _ =>
            Unmarshal(response.entity).to[String].flatMap { entity =>
              val error = s"marathon request failed with status code ${response.status} and entity $entity"
              Future.failed(new IOException(error))
            }
        }
    }
  }


  //
  // post calls
  //

  def restartApp(appId: String) = {
    postStuff[RestartAppResponse](MarathonRest.restartApp, appId)
  }


  def postStuff[R](op: MarathonRest.MarathonRest, params: Any*)
                 (implicit um: Unmarshaller[ResponseEntity, R]): Future[Either[String, R]] = {

    val (path, context) = MarathonRest.apiMap(op)

    marathonRequest(
      RequestBuilding
        .Post(getUrl(path, params: _*))
        .withHeaders(RawHeader("accept", "application/json")))
      .flatMap {
      response =>
        response.status match {
          case OK =>
            //response.entity.dataBytes.runForeach(bs => println("!!!!!!" + bs.decodeString("utf-8")))
            Unmarshal(response.entity).to[R].map(Right(_))
          case _ =>
            Unmarshal(response.entity).to[String].flatMap { entity =>
              val error = s"marathon request failed with status code ${response.status} and entity $entity"
              Future.failed(new IOException(error))
            }
        }
    }
  }
}

