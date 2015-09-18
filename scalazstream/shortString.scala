import scala.util.control.Breaks._

object shortenString extends App {

  assert(shorten("abc") == "abc")
  assert(shorten("bdabd") == "abd")
  assert(shorten("cccadbqeeag") == "cadbqeg")
  assert(shorten("cacasc") == "acs")
  assert(shorten("abcadaba")=="abcd")

  def getInd(i: Int, s: String) : Int = s(i) - 'a'

  // O(n^2)
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
          // question: can we avoid this loop by pre-processing?
          for (j <- i + 1 to s.length - 1) {
            if ((s(i) > s(j) && counts(getInd(j,s)) >0) // if later letter is smaller and can not be deleted
              || counts(getInd(i,s))==0)                // or if this letter has already been kept before
              break                                     // then delete it
            if (s(i) < s(j) && counts(getInd(j, s)) == 1) { // if later letter is bigger and cannot be deleted
              canDelete = false                             // then do not delete it
              break
            }
          }
        }

        if(!canDelete) {
          ret = ret :+ s(i)
          counts(getInd(i,s)) = 0 // 0 marks that this letter has been kept, do not keep more
        }
        else counts(getInd(i, s)) -= 1
      }
    }

    println(ret.mkString)
    ret.mkString
  }
}