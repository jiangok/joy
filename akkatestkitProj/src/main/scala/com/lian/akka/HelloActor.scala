
package com.lian.akka.helloActor

import akka.actor.{Actor}


case object SayHi

class HelloActor extends Actor
{
  def receive = {
    case SayHi => sender ! "Hi"
  }
}
