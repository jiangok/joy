package com.lian.mesos

// before run "run-main com.lian.mesos.MarathonApi http://10.141.141.10:8080/ listApps" in sbt
// deploy a docker to mesos using deploy2mesos.sh
// see the instructions in deploy2mesos.sh to see how.


import java.util.concurrent.Executors
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer}
import scala.concurrent._
import scala.concurrent.duration._
import com.jiangok.marathon.{ client => mc }


/**
 * Created by lian on 7/6/15.
 */
object MarathonApi extends App with mc.MClient {

  implicit val system = ActorSystem("MarathonApi")
  implicit val materializer = ActorMaterializer()

  val executionService = Executors.newFixedThreadPool(1)
  implicit def executor: ExecutionContextExecutor =
    ExecutionContext.fromExecutorService(executionService)

  val url = new java.net.URL(args(0))

  url match {
    case UrlParser(_protocol, _host, _port, _path) =>
      host = _host
      port = _port
  }

  val command = args(1)
  command match {
    case "createApp" =>

    case "listApps" =>
      val f = listApps()
      Await.ready(f, 10 seconds)
      println(">>>" + f.value.get.get.right.get.apps(0).id)

    case "listApp" =>
    case "listAppVersion" =>
    case "listAppConfig" =>
    case "changeAppConfig" =>
    case "rollingRestartAppTasks" =>
    case "destroyApp" =>
    case "listAppTasks" =>
    case "killAppTasks" =>
    case "killAppTask" =>
    case "listGroups" =>
    case "listGroup" =>
    case "createGroups" =>
    case "changeGroup" =>
    case "destroyGroup" =>
    case "listTasks" =>
    case "killTasks" =>
    case "listDeployments" =>
    case "deleteDeployment" =>
    case "attachEventStream" =>
    case "subscribEvents" =>
    case "listSubscribers" =>
    case "unsubscribEvents" =>
    case "listStagingQueue" =>
    case "getMarathonInst" =>
    case "getLeader" =>
    case "reelectLeader" =>
    case "ping" =>
    case "logging" =>
    case "help" =>
    case "metrics" =>
  }

  println("exiting main")
  materializer.shutdown()
  system.shutdown()
}

object UrlParser {
  def unapply(in: java.net.URL) = Some((
    in.getProtocol,
    in.getHost,
    in.getPort,
    in.getPath
    ))
}