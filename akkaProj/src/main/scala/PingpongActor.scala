package com.lian.akka.pingpong

import akka.actor.{Props, ActorSystem, FSM}
import com.kolor.docker.api._
import com.kolor.docker.api.json.Formats._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by lian on 6/30/15.
 */

sealed trait State
final object Disconnected extends State
final object Connected extends State
final object GameStarted extends State

class PingpongActor(playerName: String) extends FSM[State, Any] {
  import scala.concurrent.duration._
  //import PingpongApp.system.dispatcher // required for scheduleOnce

  startWith(Disconnected, null)

  when(Disconnected) {
    case Event("Connect", _) =>
      goto(Connected) replying "ok"
  }

  when(Connected) {
    case Event("StartGame", _) =>
      goto(GameStarted) replying "ok"
  }

  when(GameStarted) {
    case Event("ping", _) => {
      println(s"$playerName: received ping")
      PingpongApp.system.scheduler.scheduleOnce(3 seconds, sender, "pong")
      stay

    }
    case Event("pong", _) => {
      println(s"$playerName: received pong")
      PingpongApp.system.scheduler.scheduleOnce(3 seconds, sender, "ping")
      stay
    }
  }

  whenUnhandled {
    case Event(e, s) => stay
  }

  initialize()
}

object PingpongActor {
  def props(playerName: String) : Props = Props(new PingpongActor(playerName))
}

object PingpongApp extends App {

  val system = ActorSystem("ppActorSystem")

  //val andrew = system.actorOf(PingpongActor.props("Andrew"), "andrew")
  //andrew ! "startGame"

  println("hi")

  implicit val docker = Docker("https://192.168.59.103:2376")

  val maybeImages = docker.images()
  val maybeContainers = docker.containers()

  for {
    images <- maybeImages
    containers <- maybeContainers
  } yield {
    images.map(i => println(s"Image: $i"))
    containers.map(c => println(s"Container: $c"))
  }
}