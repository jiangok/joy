import java.io.IOException
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Sink, Flow, Source}
import org.scalatest._
import scala.concurrent.duration._

import scala.concurrent.{Await, Future}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._



class AkkaHttp2Spec
  extends FlatSpec with Matchers with ScalatestRouteTest with Protocols {

  "telize" should "respond to single IP query" in {
    lazy val connectionFlow: Flow[HttpRequest, HttpResponse, Any] =
      Http().outgoingConnection("www.telize.com", 80)

    def telizeRequest(request: HttpRequest): Future[HttpResponse] =
      Source.single(request).via(connectionFlow).runWith(Sink.head)

    val ip = "12.177.21.34"

    val future = telizeRequest(RequestBuilding.Get(s"/geoip/$ip")).flatMap {
      response =>
        response.status match {
          case OK => Unmarshal(response.entity).to[IpInfo].map(Right(_))
          case BadRequest => Future.successful(Left(s"$ip: incorrect IP format"))

          case _ =>
            Unmarshal(response.entity).to[String].flatMap { entity =>
              val error = s"Telize request failed with status code ${response.status} and entity $entity"
              print(error)
              Future.failed(new IOException(error))
            }
        }
    }

    Await.result(future, 10 seconds)
    val resp = future.value.get.get.right.get
    assert(resp.ip == ip)
    //assert(resp.country == Some("United State"))
  }
}

/*

http://10.141.141.10:8080/v2/apps
{"apps":[
   {"id":"/andrew",
    "cmd":null,
     "args":[],
     "user":null,
     "env":{},
     "instances":0,
     "cpus":0.2,
     "mem":32.0,
     "disk":0.0,
     "executor":"",
     "constraints":[],
     "uris":[],
     "storeUrls":[],
     "ports":[10000,10001,10002],
     "requirePorts":false,
     "backoffSeconds":1,
     "backoffFactor":1.15,
     "maxLaunchDelaySeconds":3600,
     "container":{
         "type":"DOCKER",
         "volumes":[],
         "docker":{
             "image":"jiangok/fatapp:0.1-SNAPSHOT",
             "network":"BRIDGE",
             "portMappings":[{
                 "containerPort":5099,
                 "hostPort":0,
                 "servicePort":10000,
                 "protocol":"tcp"},
                 {"containerPort":22,
                  "hostPort":0,
                  "servicePort":10001,
                  "protocol":"tcp"},
                 {"containerPort":2480,
                  "hostPort":0,
                  "servicePort":10002,
                  "protocol":"tcp"}],
              "privileged":false,
              "parameters":[],
              "forcePullImage":false}},
      "healthChecks":[],
      "dependencies":[],
      "upgradeStrategy":{
          "minimumHealthCapacity":1.0,
          "maximumOverCapacity":1.0},
      "labels":{},
      "version":"2015-07-19T15:38:53.816Z",
      "tasksStaged":0,
      "tasksRunning":2,
      "tasksHealthy":0,
      "tasksUnhealthy":0,
      "deployments":[{"id":"d4ebc706-3214-4c2c-98b5-845ead2594eb"}]}]}
 */

class MarathonApiSpec
  extends FlatSpec with Matchers with ScalatestRouteTest
{
  "marathon" should "respond to apps query" in {

  }
}