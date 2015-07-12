/**
 * Created by lian on 7/3/15.
 */

import java.io.File

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.stage.{SyncDirective, Context, StatefulStage}
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
    val future : Future[Unit] = Source.single(5).runForeach(i => print(i.toString()))

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

  "iterator source" should "work" in {
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
}