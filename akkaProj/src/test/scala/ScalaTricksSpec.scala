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

class ScalaTricksSpec(_system: ActorSystem)
  extends TestKit(_system)
  with ImplicitSender
  with Matchers
  with FlatSpecLike
  with BeforeAndAfterAll {
  def this() = this(ActorSystem("ScalaTricksSpec"))

  def print(log: String) = _system.log.debug(log)

  implicit val materializer = ActorMaterializer()

  override def afterAll: Unit = {
    system.shutdown()
    system.awaitTermination(10.seconds)
  }

  "trait and abstract class" should "be instantiated with curly bracket" in {
    trait t { def a = 10}
    new t{}  // note the curly bracket

    abstract class c { def a = println("")}
    new c{}
  }

  "self type" should "work for cake pattern DI" in {
    //http://stackoverflow.com/questions/1990948/what-is-the-difference-between-self-types-and-trait-subclasses
    trait User { def name: String }
    trait Tweeter {
      // "this" can be replaced by any string
      this: User =>

      def tweet(msg : String) = print(s"$name: $msg")
    }

    // user is injected
    new Tweeter with User { override def name = ""}
  }

  it should "work for cyclical dependencies" in {
    trait A { self: B => }
    trait B { self: A => }
  }

  "duck typing" should "work" in {
    def duck(d : {def quack() : String}) = println(d.quack)

    val _duck = new AnyRef { def quack() = "quack" }

    duck(_duck)
  }


  "this.type" should "work" in {
    // http://scalada.blogspot.com/2008/02/thistype-for-chaining-method-calls.html

    // if you change this.type to A and B in class A and B respectively,
    // b.method1.method2 will not compile
    class A { def method1: this.type = this }
    class B extends A { def method2: this.type = this }
    val b = new B
    b.method1.method2
  }

  "implicit" should "work for return value" in {
    implicit def conv(i : Int) : Boolean = i < 0
    def f() : Boolean = 10 // int 10 is converted to boolean
    assert(f() == false)
  }

  "implicit" should "work for function without explicit parameters" in {
    implicit def conv(implicit i: Int) : Boolean = i < 0
    implicit val i = 10
    def f() : Boolean = implicitly // implicit i is used
    assert(f() == false)
  }

  "implicit" should "work for function without explicit parameters -2" in {

    def f1() : Int = 10

    implicit  def f2(a: Int): Boolean = a < 0

    def f3(b : Boolean) : Boolean = b

    assert(f3(f1()) == false)
  }

  "implicit apply" should "convert target to object" in {
    // Note the implicit apply()
    object obj { implicit def apply(i : Int) : obj = new obj() }
    class obj() { val i = 0 }

    // NO Need to import obj._
    def f() : obj = 10 // this will be converted to obj.
    assert(f().i == 0)
  }

  "partial function" should "work for fraction" in {
    // why do we need partial function?
    // Because the client function can use isDefinedAt to elegantly handle the two cases.
    // NOTE: match CAN be skipped for anonymous function
    val fraction : PartialFunction[Int, Int] = { case d if d != 0 => 10/d }

    // match CANNOT be skipped for a non-anonymous function
    def client(d: Int) : Option[Int] =
      d match {
        case c if fraction.isDefinedAt(c) => Some(fraction(2))
        case _ => None
      }

    assert(client(2) == Some(5))
    assert(client(0) == None)
  }

  "curry" should "work" in {
    def sort(f: (Int, Int) => Boolean, t: (Int, Int)) = {
      if(f(t._1, t._2) == true) (t._2, t._1)
      else t
    }

    def sortCurried = (sort _).curried

    def client1 = sortCurried((i, j) => i > j)
    assert(client1(1, 2) == (1, 2))

    def client2 = sortCurried((i, j) => i < j)
    assert(client2(1, 2) == (2, 1))
  }

  "implicit" should "work for tuple parameter" in {
    trait Trait1[T] {
      def dummy = 10
    }

    implicit def convInt(i : Int) : Boolean = i < 0
    implicit def convString(s : String) : Boolean = s.length < 5

    implicit def func[A, B](implicit convI: A => Boolean, convS: B => Boolean): Trait1[(A, B)] = {
       new Trait1[(A, B)] {}
    }

    //http://www.draconianoverlord.com/2012/12/14/scala-implicit-conversion-with-tuples.html
    //http://spray.io/blog/2012-12-13-the-magnet-pattern/
    // We define a single magnet branch for all tuples at once by
    // making use of shapeless’ support for automatically converting tuples to HLists.
    //def f() : Trait1[(Int, String)] = 10 -> "a" //func[Int, String]
    //assert(f().dummy == 10)
  }

  "shapeless" should "work" in {
    //type C = Int :+: String :+: CNil

    val c = 10 :: "hello" :: HNil


  }
}