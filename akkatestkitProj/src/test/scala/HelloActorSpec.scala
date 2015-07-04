import com.lian.akka.helloActor.{HelloActor, SayHi}
import org.scalatest.{ BeforeAndAfterAll, FlatSpecLike, Matchers }
import akka.actor.{ Actor, Props, ActorSystem, FSM }
import akka.testkit.{ ImplicitSender, TestKit, TestActorRef }
import scala.concurrent.duration._

class HelloActorSpec(_system: ActorSystem)
  extends TestKit(_system)
  with ImplicitSender
  with Matchers
  with FlatSpecLike
  with BeforeAndAfterAll
{
  def this() = this(ActorSystem("HelloActorSpec"))

  override def afterAll: Unit = {
    system.shutdown()
    system.awaitTermination(10.seconds)
  }

  "An HelloActor" should "response Hi when greeted with Hi" in {
    val greeter = system.actorOf(Props[HelloActor], "greeter")
    greeter ! SayHi
    expectMsgType[String] should be("Hi")
  }
}