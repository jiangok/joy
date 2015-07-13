/**
 * Created by lian on 7/3/15.
 */

import java.io.File

import akka.stream.{OverflowStrategy, ActorMaterializer}
import akka.stream.scaladsl.{Keep, Flow, Sink, Source}
import akka.stream.stage._
import akka.util.ByteString
import org.scalatest.{ BeforeAndAfterAll, FlatSpecLike, Matchers }
import akka.actor.{ Actor, Props, ActorSystem, FSM }
import akka.testkit.{ ImplicitSender, TestKit, TestActorRef, TestFSMRef }
import scala.concurrent.{Future, Await, ExecutionContext}
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import ExecutionContext.Implicits.global

class StreamSpec(_system: ActorSystem)
  extends TestKit(_system)
  with ImplicitSender
  with Matchers
  with FlatSpecLike
  with BeforeAndAfterAll
{
  def this() = this(ActorSystem("StreamSpec"))
  def print(log: String) = _system.log.debug(log)

  implicit val materializer = ActorMaterializer()

  override def afterAll: Unit = {
    system.shutdown()
    system.awaitTermination(10.seconds)
  }

  "Source.single" should "work with runForeach" in {
    val future : Future[Unit] = Source.single(5).runForeach{i => }

    future onComplete({
        case Success(_) => assert(true)
        case Failure(failure) => assert(false, failure.getMessage)
    })

    Await.result(future, 5.seconds)
  }

  "Source.single" should "work with runFold" in {
    val future : Future[Int] = Source.single(5).runFold(1)((a, b) => a+b)

    future onComplete({
      case Success(sum) => assert(sum == 6)
      case Failure(failure) => assert(false, failure.getMessage)
    })

    Await.result(future, 5.seconds)
  }

  "Source.single" should "work with runWith" in {
    val future : Future[Int] = Source.single(5).runWith(Sink.head)

    future onComplete({
      case Success(head) => assert(head == 5)
      case Failure(failure) => assert(false, failure.getMessage)
    })

    Await.result(future, 5.seconds)
  }

  "groupBy flowOp" should "work" in {
    val source = Source(List(1, 2, 3, 4))

    val afterGroupBy = source.groupBy{i=>
      if(i%2==0) "even" else "odd"}

    val future  = afterGroupBy.runForeach{
      case (key, source) => {
        source.runWith(Sink.head)
      }
    }

    Await.result(future, 5.seconds)
  }

  "groupBy flowOps and runFold for aggregate" should "work" in {
    val future = Source(List(1, 2, 3, 4))
      .groupBy{ i => if(i%2 == 0) "even" else "odd" }
      .runForeach{
        case (key, source) => {
          // use runFold to aggregate each source
          source.runFold(0)((a, b) => a+b).onComplete {
            case Success(sum) => if(key=="even") assert(sum==6) else assert(sum==4)
            case Failure(failure) => print(failure.getMessage)
          }
        }
      }

    Await.result(future, 5.seconds)
  }

  "Source" should "be transformed by stage" in {
    val stage = new StatefulStage[Int, (Int, Int)] {
      override def initial = new State {
        override def onPush(elem: Int, ctx: Context[(Int, Int)]) = {
          emit(List((elem, elem * 10)).iterator, ctx)
        }
      }
    }

    val future = Source(List(1, 2, 3, 4))
      .transform(() => stage)
      .runWith(Sink.foreach{
        case (orig, transformed) => assert(orig * 10 == transformed)
    })

    Await.result(future, 5.seconds)
  }

  "Source" should "concat with Flow using via" in {
    val flow = Flow[Int].map{ i => (i, i * 10) }

    val future = Source(List(1,2,3,4)).via(flow).runForeach{
      case (orig, transformed) => assert(orig*10 == transformed)}

    Await.result(future, 5.seconds)
  }

  "buffer FlowOp" should "work with back pressure strategy" in {

    //"Elements are pulled out of the iterator in accordance with the demand coming
    //from the downstream transformation steps."

    //Note: Must use toMat() instead of to() to use the sink's mat (Future[Unit])
    //instead of source's mat (Unit)
    var sum = 0
    val future = Source(List(1, 2, 3, 4))  // Source[Int, Unit]
      .buffer(2, OverflowStrategy.backpressure) // Source[Int, Unit]
      .toMat(Sink.foreach{i=>sum+=i; Thread.sleep(500)})(Keep.right)  // RunnableGraph[Future[Unit]], slow sink.
      .run()

    Await.result(future, 5.seconds)

    // despite of slow sink, no source elements are missing from the sum.
    assert(sum == 10)
  }
}