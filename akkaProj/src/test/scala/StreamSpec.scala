/**
 * Created by lian on 7/3/15.
 */

import java.io.File
import java.security.MessageDigest

import akka.stream.FanInShape.{Init, Name}
import akka.stream.actor.ActorPublisher
import akka.stream.io.{SynchronousFileSource, Framing}
import akka.stream.{Attributes, FanInShape, OverflowStrategy, ActorMaterializer}
import akka.stream.scaladsl._
import akka.stream.stage._
import akka.util.ByteString
//import akka.stream.scaladsl2._
import com.sun.xml.internal.ws.developer.MemberSubmissionAddressing.Validation
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

  it should "work with runFold" in {
    val future : Future[Int] = Source.single(5).runFold(1)((a, b) => a+b)

    future onComplete({
      case Success(sum) => assert(sum == 6)
      case Failure(failure) => assert(false, failure.getMessage)
    })

    Await.result(future, 5.seconds)
  }

  it should "work with runWith" in {
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

  it should "concat with Flow using via" in {
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

  /* This test is not stable.
  it should "work with dropHead strategy" in {
    var sum = 0
    val future = Source(List(1, 2, 3, 4))
      .buffer(2, OverflowStrategy.dropHead)
      .toMat(Sink.foreach{i=>sum+=i; Thread.sleep(500)})(Keep.right)
      .run()

    Await.result(future, 5.seconds)

    // drop the oldest elements in the buffer
    // so the buffer will only store 3 and 4.
    assert(sum == 7)
  }*/

  "~>" should "work from Source to Sink" in {

    var sum = 0
    val source = Source(List(1,2,3,4))
    val sink = Sink.foreach[Int](i => sum += i)

    val future = FlowGraph.closed(source, sink)((srcMat, snkMat) => snkMat) {
      implicit builder =>
        import FlowGraph.Implicits._
        (src, snk) => src ~> snk
    }.run()

    Await.result(future, 5.seconds)
    assert(sum == 10)
  }


  "tcp" should "work" in {
    val host = "127.0.0.1"
    val port = 6000
    val testInput = 'a' to 'z'
    val transformedInput = 'A' to 'Z'

    val serverSource = Tcp().bind(host, port)

    // Flow[ByteString] is a identity flow which just pass through
    // the input without any transformation.
    // The two maps convert input to upper case and feed back to client.
    val serverSink = Sink.foreach[Tcp.IncomingConnection] { conn =>
      conn handleWith(
        Flow[ByteString]
          .map(b => b.utf8String.toUpperCase)
          .map(ByteString(_)))
    }
    val serverFuture = serverSource.to(serverSink).run()

    val clientFuture = Source(testInput.map(ByteString(_))).via(Tcp().outgoingConnection(host, port))
      .runFold(ByteString.empty){(acc, in) => acc ++ in}

    Await.result(clientFuture, 5.seconds)
    Await.result(serverFuture, 5.seconds)

    assert(serverFuture.value.get.get.localAddress.toString == s"/${host}:${port.toString}")
    assert(clientFuture.value.get.get.utf8String == transformedInput.foldLeft("")((str, c) => str + c))
  }

  it should "work with Framing" in {
    val host = "127.0.0.1"
    val port = 6001 // each test need to use different port?
    val testInput = "Hello world\nLian\nHow are you doing?\n"
    val transformedInput = "HELLO WORLD\nLIAN\nHOW ARE YOU DOING?\n"

    val serverSource = Tcp().bind(host, port)

    // Flow[ByteString] is a identity flow which just pass through
    // the input without any transformation.
    // The two maps convert input to upper case and feed back to client.
    val serverSink = Sink.foreach[Tcp.IncomingConnection] { conn =>
      conn handleWith(
        Flow[ByteString]
          .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 256, allowTruncation = true))
          .map(b => b.utf8String.toUpperCase + "\n")
          .map(ByteString(_)))
    }
    val serverFuture = serverSource.to(serverSink).run()

    val clientFuture = Source(testInput.map(ByteString(_))).via(Tcp().outgoingConnection(host, port))
      .runFold(ByteString.empty){(acc, in) => acc ++ in}

    Await.result(clientFuture, 5.seconds)
    Await.result(serverFuture, 5.seconds)

    assert(serverFuture.value.get.get.localAddress.toString == s"/${host}:${port.toString}")
    assert(clientFuture.value.get.get.utf8String == transformedInput.foldLeft("")((str, c) => str + c))
  }

  "SynchronousFileSource" should "work" in {
    val file = org.apache.commons.io.FileUtils.toFile(getClass.getResource("/example.csv"))
    assert(file.exists())

    // without Framing the number of lines will be 1 instead of 4.
    val future = SynchronousFileSource(file)
      .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 256, allowTruncation = true))
      .map(b=>b.utf8String + "\n")
      .runFold(0)((lines, str) => lines + 1) // get the number of lines.

    Await.result(future, 5.seconds)
    assert(future.value.get.get == 4)
  }


  "Merge" should "work" in {
    val source = Source(List(1,2,3,4))
    val sink = Sink.fold[Int, Int](0)((sum, i) => sum + i)

    val future = FlowGraph.closed(source, sink)((srcMat, snkMat) => snkMat) {
      implicit builder =>
        import FlowGraph.Implicits._
        (src, snk) =>
          val merge = Merge[Int](2)  // 2 inputPorts
          val merge1 = builder.add(merge)

          src ~> merge1 ~> snk
          Source.single(10) ~> merge1
    }.run()

    Await.result(future, 5.seconds)
    assert(future.value.get.get == 20)
  }

  "Balance and Merge" should "work" in {
    val source = Source(List(1,2,3,4))
    val sink = Sink.fold[Int, Int](0)((sum, i) => sum + i)

    val future = FlowGraph.closed(source, sink)((srcMat, snkMat) => snkMat) {
      implicit builder =>
        import FlowGraph.Implicits._
        (src, snk) =>
          val balance = Balance[Int](2) // 2 outputPorts
          val b1 = builder.add(balance)

          val merge = Merge[Int](2)  // 2 inputPorts
          val merge1 = builder.add(merge)

          src ~> b1 ~> merge1 ~> snk
                 b1 ~> merge1
    }.run()

    Await.result(future, 5.seconds)
    assert(future.value.get.get == 10)
  }

  "Broadcast and Merge" should "work" in {
    val source = Source(1 to 4)
    val sink = Sink.fold[Int, Int](0)((sum, i) => sum + i)

    val future = FlowGraph.closed(source, sink)((srcMat, snkMat) => snkMat) {
      implicit builder =>
        import FlowGraph.Implicits._
        (src, snk) =>
          val broadcast = Broadcast[Int](2) // 2 outputPorts
          val b1 = builder.add(broadcast)

          val merge = Merge[Int](2)  // 2 inputPorts
          val merge1 = builder.add(merge)

          src ~> b1 ~> merge1 ~> snk
                 b1 ~> merge1
    }.run()

    Await.result(future, 5.seconds)
    assert(future.value.get.get == 20)
  }

  "PushStage" should "work with Source" in {
    class Map[A, B](f: A => B) extends PushStage[A, B] {
      override def onPush(elem: A, ctx: Context[B]) : SyncDirective =
        ctx.push(f(elem))
    }

    val mapStage = new Map[Int, Int]( a => a*(-1))
    val source = Source(1 to 4).transform(() => mapStage)

    //NOTE: [Int, Int] is mandatory, otherwise ~> cannot be resolved.
    val sink = Sink.fold[Int, Int](0)((sum, i) => sum + i)


    val future = FlowGraph.closed(source, sink)((srcMat, snkMat) => snkMat) {
      implicit builder =>
        import FlowGraph.Implicits._
        (src, snk) =>
          src  ~> snk
    }.run()

    Await.result(future, 5.seconds)
    assert(future.value.get.get == -10)
  }

  it should "work with Flow" in {
    class Map[A, B](f: A => B) extends PushStage[A, B] {
      override def onPush(elem: A, ctx: Context[B]) : SyncDirective =
        ctx.push(f(elem))
    }

    val mapStage = new Map[Int, Int]( a => a*(-1))
    val flow = Flow[Int].transform(()=>mapStage)

    val source = Source(1 to 4)

    //NOTE: [Int, Int] is mandatory, otherwise ~> cannot be resolved.
    val sink = Sink.fold[Int, Int](0)((sum, i) => sum + i)


    val future = FlowGraph.closed(source, sink)((srcMat, snkMat) => snkMat) {
      implicit builder =>
        import FlowGraph.Implicits._
        (src, snk) =>
          src  ~> flow ~> snk
    }.run()

    Await.result(future, 5.seconds)
    assert(future.value.get.get == -10)
  }

  "StatefulStage" should "emit multiple elements" in {
    class Duplicator[Int]() extends StatefulStage[Int, Int] {
      override def initial: StageState[Int, Int] = new StageState[Int, Int] {
        override  def onPush(elem: Int, ctx: Context[Int]) : SyncDirective =
          emit(List(elem, elem).iterator, ctx)
      }
    }

    val source = Source(1 to 4).transform(() => new Duplicator[Int]())
    val sink = Sink.fold[Int, Int](0)((sum, i) => sum + i)

    val future = FlowGraph.closed(source, sink)((srcMat, snkMat) => snkMat) {
      implicit builder =>
        import FlowGraph.Implicits._
        (src, snk) =>
          src ~> snk
    }.run()

    Await.result(future, 5.seconds)
    assert(future.value.get.get == 20)
  }

  "StatefulStage" should "become behavior" in {
    // this stage will change state if the elem > 3
    class ZeroOneStage() extends StatefulStage[Int, Int] {
      override def initial: StageState[Int, Int] = new StageState[Int, Int] {
        override def onPush(elem: Int, ctx: Context[Int]): SyncDirective = {
          if (elem < 3)
            emit(List(0).iterator, ctx)
          else {
            become(OneState)
            OneState.onPush(elem, ctx)
          }
        }
      }

      val OneState : StageState[Int, Int] = new StageState[Int, Int] {
        override def onPush(elem: Int, ctx: Context[Int]) : SyncDirective = {
          emit(List(1).iterator, ctx)
        }
      }
    }

    val source = Source(1 to 4).transform(() => new ZeroOneStage())
    val sink = Sink.fold[Int, Int](0)((sum, i) => sum + i)

    val future = FlowGraph.closed(source, sink)((srcMat, snkMat) => snkMat) {
      implicit builder =>
        import FlowGraph.Implicits._
        (src, snk) =>
          src ~> snk
    }.run()

    Await.result(future, 5.seconds)
    assert(future.value.get.get == 2)
  }

  "PushPullStage" should "work for calculating stream sum" in {
    def sumCalculator() = new PushPullStage[Int, Int] {
      var sum = 0
      override def onPush(num: Int, ctx: Context[Int]) : SyncDirective = {
        sum += num
        ctx.pull() // ctx.pull does not do anything since Source does not handle it.
      }

      override def onPull(ctx: Context[Int]) : SyncDirective = {
        if(ctx.isFinishing) ctx.pushAndFinish(sum)
        else ctx.pull()
      }

      override def onUpstreamFinish(ctx: Context[Int]) : TerminationDirective = {
        // needed otherwise stage is terminated immediately when source is exhausted.
        ctx.absorbTermination()
      }
    }

    val calsum =
      Source(1 to 4).transform(() => sumCalculator())

    val future = calsum.runFold(0)((a,b) => a + b)

    Await.result(future, 5.seconds)
    assert(future.value.get.get == 10)
  }

  "FlexiMerge" should "work" in {
    // define shape
    class PreferringMergeShape[A](_init: Init[A] = Name("PreferringMerge"))
      extends FanInShape[A](_init) {
      val preferred = newInlet[A]("preferred")
      val secondary1 = newInlet[A]("secondary1")
      val secondary2 = newInlet[A]("secondary2")
      protected override def construct(i: Init[A]) = new PreferringMergeShape[A](i)
    }

    // define logic
    class PreferringMerge extends FlexiMerge[Int, PreferringMergeShape[Int]](
      new PreferringMergeShape, Attributes.name("ImportantWithBackups")) {
      import akka.stream.scaladsl.FlexiMerge._

      override def createMergeLogic(p: PortT) = new MergeLogic[Int] {
        override def initialState =
          State[Int](ReadPreferred(p.preferred, p.secondary1, p.secondary2)) {
            (ctx, input, element) =>
              ctx.emit(element)
              SameState
          }
      }
    }

    val preferredSource = Source(1 to 10)
    val secondary1Source = Source(20 to 30)
    val secondary2Source = Source(40 to 50)

    // this sink stores all elements into a list so that we can verify the order later.
    // we expect the first 4 elements are 1 to 4.
    val sink = Sink.fold[List[Int], Int](List[Int]())((list, a) => list :+ a)

    val future = FlowGraph.closed(
      preferredSource, secondary1Source, secondary2Source, sink)((srcMat, src1Mat, src2Mat, snkMat) => snkMat) {
      implicit builder =>
        import FlowGraph.Implicits._
        (src0, src1, src2, snk) =>
          val merge = builder.add(new PreferringMerge())

          src0 ~> merge.preferred;  merge.out ~> snk
          src1 ~> merge.secondary1
          src2 ~> merge.secondary2

    }.run()

    Await.result(future, 5.seconds)

    // PreferringMerge does not guarantee preferredSource elements all reach
    // sink before other sources. The order could be:
    // 1,20,40,21,2,3,4,5,6,7,8,9,10,22,23...
    //future.value.get.get.foreach(a=>print(a.toString))
  }

  // FlexiRoute

  // DetachedStage



  /*
    "Source" should "work for ActorPublisher" in {
      import FlowGraph.Implicits._

      class TestPublisher extends ActorPublisher[Int]
      {
        override def receive = {
          case i : Int => print(i.toString)
        }
      }

      Source(new TestPublisher) ~> Sink.ignore
    }*/
}