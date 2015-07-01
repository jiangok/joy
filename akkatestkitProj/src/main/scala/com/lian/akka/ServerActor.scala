package com.lian.akka

import akka.actor.{LoggingFSM}
import akka.persistence.PersistentActor

/**
 * Created by lian on 6/30/15.
 */

sealed trait ServerState
case object ServerState_Uninitialized extends ServerState
case object ServerState_Initialized extends ServerState
case object ServerState_Connected extends ServerState
case object ServerState_Disconnected extends ServerState


sealed trait ServerData
case object ServerData_Uninitialized extends ServerData
case class ServerData_Message(message: String) extends ServerData

//case class Cmd(data: String)
//case class Evt(data: String)


class ServerActor extends PersistentActor with LoggingFSM[ServerState, ServerData]{

  override def persistenceId = "serverActorPersistentId"

  override def receiveCommand = { case _ => print() }

  override def receiveRecover = { case _ => print()}

  startWith(ServerState_Uninitialized, ServerData_Uninitialized)
}
