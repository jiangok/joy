import scala.util.control.Breaks._

object shortenString extends App {

  assert(shorten("abc") == "abc")
  assert(shorten("bdabd") == "abd")

  def getInd(i: Int, s: String) : Int = s(i) - 'a'

  def shorten(s: String) : String = {
    // get counts
    val counts = Array.fill[Int](26)(0)
    for(i <- 0 to s.length-1) {
      counts(getInd(i, s)) += 1
    }

    var ret = Vector.empty[Char]

    // abc -> abc
    // bdabd -> abd
    // shorten
    for(i <- 0 to s.length-1) {
      if(counts(getInd(i, s)) == 1) ret  = ret :+ s(i)
      else {
        var canDelete = true
        breakable {
          for (j <- i + 1 to s.length - 1) {
            if (s(i) > s(j)) break
            if (counts(getInd(i, s)) == 1 && s(i) < s(j)) {
              canDelete = false
              break
            }
          }
        }

        if(!canDelete) {
          ret = ret :+ s(i)
          counts(getInd(i, s)) -= 1
        }
      }
    }

    ret.mkString
  }
}