import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, FlatSpec}
import scala.concurrent.{Future}
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.ContentTypes._

class Service2Spec extends FlatSpec with Matchers with ScalatestRouteTest {

  // path {} need a (L => Route) => Route
  val routes = {
    (get & path(Segment)) { s =>
      complete {
        Future[Unit] {}.map[ToResponseMarshallable] { case _ => OK -> "worked" }
      }
    }
  }

  Get(s"/abc") ~> routes ~> check {
    status shouldBe OK
    contentType shouldBe `text/plain(UTF-8)`
    responseAs[String] shouldBe "worked"
  }
}