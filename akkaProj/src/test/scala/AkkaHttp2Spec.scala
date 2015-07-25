import java.io.IOException
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Sink, Flow, Source}
import org.scalatest._
import spray.json.DefaultJsonProtocol
import scala.concurrent.duration._

import scala.concurrent.{Promise, Await, Future}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import scala.util.Try
import scalaz._
import Scalaz._

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

case class Dog (name : String)
case class Dogs (dogs : List[Dog])

trait DogProtocol extends DefaultJsonProtocol {
  implicit val dogFormat = jsonFormat1(Dog.apply)
  implicit val dogsFormat = listFormat(dogFormat)
}

class DogSpec
  extends FlatSpec with Matchers with ScalatestRouteTest with DogProtocol {

  "dogs" should "be serialized/deserialized" in {
    val d1 = Dog("tom")
    val d2 = Dog("mike")
    val dogs = List(d1, d2)

    val d1Entity = marshal(d1)
    val dogsEntity = marshal(dogs)

    val future1 = Unmarshal(d1Entity).to[Dog]
    val future2 = Unmarshal(dogsEntity).to[List[Dog]]

    Await.result(future1, 5 seconds)
    Await.result(future2, 5 seconds)

    assert(future1.value.get.get == d1)
    assert(future2.value.get.get.head == d1)
    assert(future2.value.get.get.last == d2)
  }
}

