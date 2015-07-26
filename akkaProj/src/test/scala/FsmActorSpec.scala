/**
 * Created by lian on 7/3/15.
 */

import com.lian.akka.fsm._
import org.scalatest.{ BeforeAndAfterAll, FlatSpecLike, Matchers }
import akka.actor.{ Actor, Props, ActorSystem, FSM }
import akka.testkit.{ ImplicitSender, TestKit, TestActorRef, TestFSMRef }
import scala.concurrent.duration._

class FsmActorSpec(_system: ActorSystem)
  extends TestKit(_system)
  with ImplicitSender
  with Matchers
  with FlatSpecLike
  with BeforeAndAfterAll
{
  def this() = this(ActorSystem("FsmActorSpec"))

  val fsm = TestFSMRef(new FsmActor)

  override def afterAll: Unit = {
    system.shutdown()
    system.awaitTermination(10.seconds)
  }

  "An FsmActor" should "reply yes when asked to do things" in {
    val fsmActor = system.actorOf(Props[FsmActor], "fsmActor")
    fsmActor ! Initialize
    expectMsgType[String] should be("yes")
  }

  "An FsmActor" should "go to initialized state when initialized" in {

    assert(fsm.stateName == Uninitialized)
    fsm ! Initialize

    assert(fsm.stateName == Initialized)

    fsm ! Uninitialize

    assert(fsm.stateName == Uninitialized)
  }
}