import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, FlatSpec}
import scala.concurrent.{Future}
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.ContentTypes._
import spray.json.DefaultJsonProtocol

// THIS MUST BE IMPORTED FOR APair TO FIND THE IMPLICIT MARSHALLER
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

case class APair(ip1: String, ip2: String)

trait APairProtocol extends DefaultJsonProtocol {
  implicit val APairFormat = jsonFormat2(APair.apply)
}

class Service2Spec extends FlatSpec with Matchers with ScalatestRouteTest with APairProtocol {

  val map = Map("red" -> 1, "yellow" -> 2)

  val routes = {
    (get & path("a1" / IntNumber / "edf")) { s =>
      complete {
        Future[Unit] {}.map[ToResponseMarshallable] { case _ => OK -> s.toString }
      }
    } ~
      (get & path("a2" / Segments)) { list =>
        complete {
          Future[Unit] {}.map[ToResponseMarshallable] { case _ => OK -> list(0) }
        }
      } ~
      (get & path("a3" / RestPath)) { s =>
        complete {
          Future[Unit] {}.map[ToResponseMarshallable] { case _ => OK -> s.toString }
        }
      } ~ (get & path("a4" / Segment / "ddd")) { s =>
      complete {
        Future[Unit] {}.map[ToResponseMarshallable] { case _ => OK -> s }
      }
    } ~ (get & path("a5" / map)) { s =>
      complete {
        Future[Unit] {}.map[ToResponseMarshallable] { case _ => OK -> s.toString }
      }
    } ~ (get & path("a6" / map / map)) { (firstMatch, secondMatch) =>
      complete {
        Future[Unit] {}.map[ToResponseMarshallable] { case _ => OK -> secondMatch.toString }
      }
    } ~ (post & path("a7") & entity(as[String])) { s =>
      complete {
        Future[Unit] {}.map[ToResponseMarshallable] { case _ => OK -> s }
      }
    }  ~ (post & path("a8") & entity(as[APair])) { pair =>
      complete {
        Future[Unit] {}.map[ToResponseMarshallable] { case _ => OK -> pair.ip1 }
      }
    }
  }

  Get("/a1/10/edf") ~> routes ~> check {
    status shouldBe OK
    contentType shouldBe `text/plain(UTF-8)`
    responseAs[String] shouldBe "10"
  }

  Get("/a2/hello/abc") ~> routes ~> check {
    responseAs[String] shouldBe "hello"
  }

  Get("/a3/ddd/hello") ~> routes ~> check {
    responseAs[String] shouldBe "ddd/hello"
  }

  Get("/a4/hello/ddd") ~> routes ~> check {
    responseAs[String] shouldBe "hello"
  }

  Get("/a5/yellow") ~> routes ~> check {
    responseAs[String] shouldBe "2"
  }

  Get("/a6/yellow/red") ~> routes ~> check {
    responseAs[String] shouldBe "1"
  }

  Post("/a7", "content") ~> routes ~> check {
    responseAs[String] shouldBe "content"
  }

  Post("/a8", APair("ip1", "ip2")) ~> routes ~> check {
    responseAs[String] shouldBe "ip1"
  }
}