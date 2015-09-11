import scalaz.stream.Process

object scalazStream_process extends App {

  println("testApp started")

  val p = Process(5,4,3,2,1)
  val results = p collect {
    case 1 => "one"
    case 2 => "two"
    case 3 => "three"
  }

  val filtered = results.filter { _.length > 3 }
  filtered map { println(_) }
}
