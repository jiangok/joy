package com.jiangok.marathon.client

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest._
import scala.concurrent.Await
import scala.concurrent.duration._

class MClientSpec
  extends FlatSpec with MClient with Matchers with ScalatestRouteTest {

  host = "10.141.141.10"
  port = 8080

  "listApps" should "work" in {

    val future = listApps()
    Await.result(future, 10 seconds)
    assert(future.value.get.get.right.get.apps.length == 1)
    assert(future.value.get.get.right.get.apps(0).id == "/andrew")

  }
}