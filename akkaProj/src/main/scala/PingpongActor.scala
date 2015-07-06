package com.lian.akka.pingpong

import akka.actor.{Props, ActorSystem, FSM}

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
}