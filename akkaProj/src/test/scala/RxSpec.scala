import akka.stream.FanInShape.{Init, Name}
import akka.stream.io.{SynchronousFileSource, Framing}
import akka.stream.{Attributes, FanInShape, OverflowStrategy, ActorMaterializer}
import akka.stream.scaladsl._
import akka.stream.stage._
import akka.util.ByteString
import org.scalatest.{ BeforeAndAfterAll, FlatSpecLike, Matchers }
import akka.actor.{ Actor, Props, ActorSystem, FSM }
import akka.testkit.{ ImplicitSender, TestKit, TestActorRef, TestFSMRef }
import scala.concurrent.{Future, Await, ExecutionContext}
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import ExecutionContext.Implicits.global
import shapeless._
import com.mfglabs.stream._
import extensions.shapeless._

class RxSpec(_system: ActorSystem)
  extends TestKit(_system)
  with ImplicitSender
  with Matchers
  with FlatSpecLike
  with BeforeAndAfterAll {
  def this() = this(ActorSystem("StreamSpec"))

  def print(log: String) = _system.log.debug(log)

  implicit val materializer = ActorMaterializer()

  override def afterAll: Unit = {
    system.shutdown()
    system.awaitTermination(10.seconds)
  }

  "polymorphic function" should "work" in {
    import poly._

    object choose extends (Set ~> Option) {
      def apply[T](s: Set[T]) = s.headOption
    }

    assert(choose(Set(1,2,3)) == Option(1))
    assert(choose(Set("a","b","c")) == Option("a"))

    def pairApply(f: Set ~> Option) = (f(Set(1,2,3)), f(Set("a", "b", "c")))
    assert(pairApply(choose) == (Option(1), Option("a")))
  }


  "apply function" should "work in class" in {
    // Note that apply function is in class.
    class myAdd(x: Int) { def apply(y: Int) = y + y }
    val x = new myAdd(2)
    assert(x(4) == 8) // the class object x is used like a function.
  }

  "magnet pattern" should "work" in {
    trait FWMagnet {
      type Result
      def apply(): Result
    }

    implicit def clientImpl(a: Int) = new FWMagnet {
      override type Result = Int
      override def apply(): Result = a * 2
    }

    def genericFunc(mag: FWMagnet): mag.Result = mag()
    assert(genericFunc(10) == 20)
  }


  "size" should "be generic for preliminary types" in {
    object size extends Poly1 {
      //return an object of type shapeless.poly.Case[Poly1.this.type, shapeless.::[Int, shapeless.HNil]]
      implicit def caseInt = at[Int](x => 1)

      //return an object of type shapeless.poly.Case[Poly1.this.type, shapeless.::[String, shapeless.HNil]]
      implicit def caseString = at[String](_.length)

      //at[]() returns an object of type shapeless.poly.Case[Poly1.this.type, shapeless.::[(T, U), shapeless.HNil]]
      //Aux[T,Int] is  an object of type shapeless.poly.Case[Poly1.this.type, shapeless.::[(T, U), shapeless.HNil]] { override type Result = Result0}
      implicit def caseTuple[T, U](implicit st: Case.Aux[T, Int], su: Case.Aux[U, Int]) =
        at[(T,U)](t => size(t._1) + size(t._2))
    }

    // call PolyDefns, abstract class,
    // def apply[T](t : T)(implicit ev : scala.Predef.=:=[shapeless.::[T, shapeless.HNil], L]) : Case.this.Result
    // as long as type T::HNil =:= L where T is Int.
    assert(size(23) == 1)

    assert(size("abc") == 3)
    assert(size((23, "foo")) == 4)
  }
}