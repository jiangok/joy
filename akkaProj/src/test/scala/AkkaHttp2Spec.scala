//import java.io.IOException

//import akka.actor.ActorSystem
//import akka.event.NoLogging

import java.io.IOException

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.Unmarshal
//import spray.json.DefaultJsonProtocol

//import akka.http.scaladsl.unmarshalling.Unmarshal
//import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Flow, Source}
import org.scalatest._
import scala.concurrent.duration._

import scala.concurrent.{Await, Future}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._



class AkkaHttp2Spec
  extends FlatSpec with Matchers with ScalatestRouteTest with Protocols {

  "Service" should "respond to single IP query" in {
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
