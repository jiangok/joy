package com.lian.akka

import akka.actor.{LoggingFSM}
import akka.persistence.PersistentActor

/**
 * Created by lian on 6/30/15.
 */

sealed trait ClientState
case object ClientState_Uninitialized extends ClientState
case object ClientState_Initialized extends ClientState


sealed trait ClientData
case object ClientData_Uninitialized extends ClientData
case class ClientData_Client(message: String) extends ClientData

case class Cmd(data: String)
case class Evt(data: String)


class ClientActor extends PersistentActor with LoggingFSM[ClientState, ClientData]{

  override def persistenceId = "clientActorPersistentId"

  override def receiveCommand = { case _ => print() }

  override def receiveRecover = { case _ => print()}

  startWith(ClientState_Uninitialized, ClientData_Uninitialized)


}
