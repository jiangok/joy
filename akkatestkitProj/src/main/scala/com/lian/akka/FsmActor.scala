package com.lian.akka.fsm

import akka.actor.{Actor, FSM}

/**
 * Created by lian on 7/3/15.
 */

sealed trait Message
case object Initialize extends Message
case object Uninitialize extends Message

sealed trait State
case object Uninitialized extends State
case object Initialized extends State

sealed trait Data
case class StringData(msg: String) extends Data

class FsmActor extends FSM[State, Data] {
  startWith(Uninitialized, StringData(null))

  when(Uninitialized) {
    case Event(Initialize, t @ StringData(m)) =>
      goto(Initialized) using StringData(m) replying "yes"
  }

  when(Initialized) {
    case Event(Uninitialize, _) =>
      goto(Uninitialized) using StringData(null) replying "yes"
  }

  whenUnhandled {
    case Event(e, s) => stay
  }

  initialize()
}
