import java.io.IOException
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Sink, Flow, Source}
import akka.util.ByteString
import org.scalatest._
import spray.json.DefaultJsonProtocol
import scala.concurrent.duration._

import scala.concurrent.{Await, Future}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

// this makes the compile OBVIOUSLY slower
// also it has trouble to deserialize marathon /v2/apps api return
import fommil.sjs.FamilyFormats._

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

////////////////

case class Dog (name : String)
case class Dogs (dogs : List[Dog])
case class DogArray (dogs : Array[Dog])
case class Dog2 (dog: Dog)
case class Dogs2 (dogs : Dogs)

trait DogProtocol extends DefaultJsonProtocol {
  implicit val dogFormat = jsonFormat1(Dog.apply)
  implicit val dogsFormat = jsonFormat1(Dogs.apply)
  implicit val dogListFormat = listFormat(dogFormat)
  implicit val dogArrayFormat = arrayFormat(dogFormat, implicitly[ClassManifest[Dog]])
  implicit val dog2Format = jsonFormat1(Dog2.apply)
}

class DogSpec
  extends FlatSpec with Matchers with ScalatestRouteTest with DogProtocol {

  "dog list" should "be serialized/deserialized" in {
    val d1 = Dog("tom")
    val d2 = Dog("mike")
    val dogList = List(d1, d2)
    val dogArray = Array(d1, d2)
    val dg2 = Dog2(d1)
    val dogs = Dogs(dogList)
    assert(dogs.dogs.length == 2)

    val d1Entity = marshal(d1)
    val dogsEntity = marshal(dogList)
    val dogArrayEntity = marshal(dogArray)
    val dg2Entity = marshal(dg2)

    //print("!!!!!" + dogArrayEntity.data.decodeString("utf-8"))
    /*
    both dog list abd dog array has json: [{"name": "tom"}, {"name": "mike"}]
     */

    val future1 = Unmarshal(d1Entity).to[Dog]
    val future2 = Unmarshal(dogsEntity).to[List[Dog]]
    val dg2future = Unmarshal(dg2Entity).to[Dog2]

    Await.result(future1, 5 seconds)
    Await.result(future2, 5 seconds)
    Await.result(dg2future, 5 seconds)

    assert(future1.value.get.get == d1)
    assert(future2.value.get.get.head == d1)
    assert(future2.value.get.get.last == d2)
    assert(dg2future.value.get.get.dog == d1)
  }

  "dog json" should "be deserialized" in {
    val d1 = Dog("tom")
    val d1Entity = marshal(d1)
    val d1EntityChanged = d1Entity.copy(data = ByteString.apply("{\"name\": \"mike\"}") )
    val future1 = Unmarshal(d1EntityChanged).to[Dog]
    Await.result(future1, 5 seconds)
    assert(future1.value.get.get.name == "mike")
  }

  "array value" should "be deserialized" in {
    val d1 = Dog("tom")
    val d1Entity = marshal(d1)
    val d1EntityChanged = d1Entity.copy(
      data = ByteString.apply("{\"dogs\":[{\"name\": \"mike\"},{\"name\": \"tom\"}]}"))
    val future1 = Unmarshal(d1EntityChanged).to[Dogs]
    Await.result(future1, 5 seconds)
    assert(future1.value.get.get.dogs.length == 2)
  }
}





//////////////

case class Book (chapter1: String, chapter2: String)
case class PartialBook(chapter1: String) // the member name must match Book's

trait BookProtocol extends DefaultJsonProtocol {
  implicit val book = jsonFormat2(Book.apply)
  implicit val partialBook = jsonFormat1(PartialBook)
}

class BookSpec
  extends FlatSpec with Matchers with ScalatestRouteTest with BookProtocol {

  "book" should "be partially deserialized" in {
    val b1 = Book("ch1", "ch2")

    val b1Entity = marshal(b1)

    val future1 = Unmarshal(b1Entity).to[PartialBook]

    Await.result(future1, 5 seconds)

    assert(future1.value.get.get.chapter1 == "ch1")
  }
}

///////////////// spray json shapeless

package domain {

sealed trait MyTrait

case class Duck(name: String) extends MyTrait

case class Chick(name: String) extends MyTrait

}

class BirdSpec
  extends FlatSpec with Matchers with ScalatestRouteTest {

  import domain._

  "SprayJsonShapeless" should "work" in {
    val d1 = Duck("d1")
    val c1 = Chick("c1")

    val d1Entity = marshal(d1)
    val c1Entity = marshal(c1)

    val future1 = Unmarshal(d1Entity).to[Duck]
    val future2 = Unmarshal(c1Entity).to[Chick]

    Await.result(future1, 5 seconds)
    Await.result(future2, 5 seconds)

    assert(future1.value.get.get.name == "d1")
    assert(future2.value.get.get.name == "c1")
  }
}


