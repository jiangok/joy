package com.lian.akka.pingpong

import akka.actor.{Props, Actor, ActorSystem}
import akka.persistence.PersistentActor
import com.lian.akka.fsm.FsmActor

/**
 * Created by lian on 6/30/15.
 */

sealed trait message
final case class startGame(val otherPlayer: String) extends message

class PingpongActor(playerName: String) extends Actor {
  val system = ActorSystem("ppActorSystem")

  def receive = {
    case "startGame" => {
      println(s"$playerName started a game")
      val elaine = system.actorOf(PingpongActor.props("Elaine"), "elaine")
      elaine ! "ping"
    }
    case "ping" => {
      println(s"$playerName: received ping")
      sender ! "pong"
    }
    case "pong" => {
      println(s"$playerName: received pong")
      sender ! "ping"
    }
    case _ => {
      println(s"waiting for the other player")
    }
  }
}

object PingpongActor {
  def props(playerName: String) : Props = Props(new PingpongActor(playerName))
}

object PingpongApp extends App {

  val system = ActorSystem("ppActorSystem")

  val andrew = system.actorOf(PingpongActor.props("Andrew"), "andrew")
  andrew ! "startGame"
}