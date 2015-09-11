import scala.collection.immutable.{HashMap, HashSet}

object fib extends App {

  assert(fib(5).recFib == 8)
  assert(fib(5).tailRecFib == 8)
  assert(fib(5).loopFib == 8)
  assert(fib(5).dpFib == 8)

  val v = Vector(1, 2, 3)
  assert(Permutation(v).permute() == 6)
  val v2 = Vector(1, 2, 2)
  assert(Permutation(v2).permute() == 3)

  val s = HashSet(1,2,3)
 // assert(Combination(s) == 7)

  // http://www.careercup.com/question?id=4886983698546688
  def findCommon(l1: List[Int], l2: List[Int]) : List[Int] = {
    if(l1.length == 0 || l2.length == 0) return Nil
    var i = 0
    var j = 0
    var ret = List.empty[Int]
    while(i<l1.length && j<l2.length) {
      if(l1(i)==l2(j)) {
        ret = ret :+ l1(i)
        i += 1
        j += 1
      }
      if(l1(i)<l2(j)) {
        i += 1
      }
      else {
        j += 1
      }
    }
    ret
  }


}

case class fib(n: Int)
{
  //
  // recursive impl, O(2^n)
  //
  def recFib() = {
    assume(n>0)

    def recFib2(n: Int) : Int = {
      assume(n > 0)

      if (n == 1) 1
      else if (n == 2) 2
      else recFib2(n - 1) + recFib2(n - 2) // this is NOT tail recurse!
    }

    recFib2(n)
  }

  //
  // tail recursive impl, O(n)
  //
  def tailRecFib() = {
    assume(n>0)

    def rec(n: Int, a: Int, b: Int) : Int = {
      if(n==0) b
      else rec(n-1, b, a+b)
    }

    if(n==1) 1
    else if(n==2) 2
    else rec(n, 0, 1)
  }

  //
  // loop, O(n)
  //
  def loopFib() = {
    assume(n>0)

    if(n==1) 1
    else if(n==2) 2
    else {
      var t1 = 1
      var t2 = 2
      for (i <- 3 to n) {
        val tmp = t1 + t2
        t1 = t2
        t2 = tmp
      }

      t2
    }
  }

  //
  // DP, O(n)
  //
  def dpFib() = {
    assume(n > 0)

    def sub(a: Int) : Int = {
      if (n == 1) 1
      else if (n == 2) 2
      else {
        var map = HashMap.empty[Int, Int] + (1 -> 1, 2 -> 2)

        if (map.contains(a)) {
          map.get(a).get
        }
        else {
          val ret = sub(a - 1) + sub(a - 2)
          map += (a -> ret)
          ret
        }
      }
    }

    sub(n)
  }
}

case class Permutation(var vector: Vector[Int]) {
  // return the number of permutations, O(n*n!)
  def permute() : Int = {

    def swap(v: Vector[Int], i: Int, j: Int) : Vector[Int] = {
      val tmp = v(i)
      var v2 = v.updated(i, v(j))
      v2 = v2.updated(j, tmp)
      v2
    }

    def rec(current: Int): Int = {
      if (current == vector.length - 1) {
        //println(vector)
        1
      }
      else {
        var set = HashSet.empty[Int]
        var count = 0
        for (i <- current to vector.length - 1) {
          if(!set.contains(vector(i))) {
            set += (vector(i))

            vector = swap(vector, i, current)

            count += rec(current + 1)

            vector = swap(vector, i, current)
          }
        }

        count
      }
    }

    rec(0)
  }
}


case class Combination(var v: HashSet[Int]) {
  def rec(n: Int, v: HashSet[Int]) : Int = {
    if(n==v.size) return 1

    var count = 0
    for(i<-0 to v.size-1) {
      count += rec(n-1, v.drop(1))
    }

    count
  }

  def comb() : Int = {
    var count = 0
    for(i<-1 to v.size) {
      count += rec(i, v)
    }

    count
  }
}



