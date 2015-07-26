package com.jiangok.marathon.client

import java.net.Socket

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest._
import scala.concurrent.Await
import scala.concurrent.duration._

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._


class MClientSpec
  extends FlatSpec with MClient with Matchers with ScalatestRouteTest {

  host = "10.141.141.10"
  port = 8080

  def pingable() : Boolean = {
    var s: Socket = null
    try {
      s = new Socket(host, port)
      s.isConnected
    } catch {
      case iae: IllegalArgumentException =>
        fail(iae)
        false
      case _: Throwable =>
        false
    } finally {
      if (s ne null)
        s.close()
    }
  }

  lazy val isPingable = pingable()

  "listApps" should "work" in {
    //assume(isPingable) this ping takes long time.
    //Manual disable/enable these test for now.
    val future = getStuff[Apps](MarathonRest.listApps)
    Await.result(future, 10 seconds)
    assert(future.value.get.get.right.get.apps.length > 0)
    assert(future.value.get.get.right.get.apps.filter(_.id == Some("/andrew")).length > 0)
  }
/*
  "listApp" should "work" in {
    val future = listApp("/andrew")
    Await.result(future, 10 seconds)
    assert(future.value.get.get.right.get.id == Some("/andrew"))
  }
*/
  "getLeader" should "work" in {
    val future = getStuff[Leader](MarathonRest.getLeader)
    Await.result(future, 10 seconds)
    assert(future.value.get.get.right.get.leader == Some("mesos:8080"))
  }
}