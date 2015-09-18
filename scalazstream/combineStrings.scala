import scala.util.control.Breaks._
import scala.collection.mutable.MutableList


object combineStrings extends App {

  assert(comb("", Vector[String]("RBR", "BBR", "RRR")) == 9)
  assert(comb("", Vector[String]("BR", "RRRR", "RRB", "BRBR", "RBBB", "RBBBB", "BBB")) == 25)

  //http://www.careercup.com/question?id=5131737510182912
  // O(n*n!)
  def comb(cur: String, strs: Vector[String]) : Int = {
    var len = cur.length

    for(i <- 0 to strs.length-1) {
      if(cur=="" || cur.charAt(cur.length-1) == strs(i)(0)) {
        val cur1 = cur.concat(strs(i))
        val len1 = comb(cur1, strs.take(i) ++ strs.drop(i+1))
        len = if (len > len1) len else len1
      }
    }

    len
  }


  assert(removeSpaces("   ") == " " )
  assert(removeSpaces("     a      ") == " a " )
  assert(removeSpaces("   a  b c") == " a b c")
  assert(removeSpaces("abc") == "abc")
  assert(removeSpaces("a  b c    ad") == "a b c ad")
  assert(removeSpaces("a  b c    ad  ") == "a b c ad ")

  // remove spaces
  // https://jiangok.wordpress.com/2014/08/04/2675/
  def removeSpaces(s: String) : String = {
    val arr = s.toArray

    var a = -1
    var spaceReserved = false
    var removed = 0

    for(i<- 0 to arr.size-1) {
      arr(i) match {
        case ' ' =>
          if(spaceReserved) {
            if(a == -1) a = i
            removed += 1
          }
          else {
            if(a != -1) a += 1
            spaceReserved = true
          }
        case v@_ =>
          if(a != -1) {
            arr(a) = v
            a += 1
            arr(i)=' ' // must reset after copy
            spaceReserved = false }
      }
    }

    val ret = arr.take(s.length - removed).mkString
   // println(s"!$ret, $removed!")
    ret
  }


  //http://www.careercup.com/question?id=5926214520799232

  assert(isOneEditAway("abc", "adc") == true)
  assert(isOneEditAway("abcd", "abc") == true)
  assert(isOneEditAway("add", "aba") == false)
  assert(isOneEditAway("addd", "adddd") == true)
  assert(isOneEditAway("abc", "dabc") == true)
  assert(isOneEditAway("abc", "ac") == true)

  def isOneEditAway(s1: String, s2: String) : Boolean = {
    if(s1.length-s2.length > 1 || s1.length-s2.length < -1) return false
    if(s1.length == s2.length) {
      var dif = 0
      for(i <- 0 to s1.length-1) {
        if(s1(i)!=s2(i)) {
          if (dif == 1) return false
          dif += 1
        }
      }
      true
    }
    else {
      var p1 : Int = 0
      var p2 : Int = 0
      var dif = 0
      while(p1 < s1.length && p2 < s2.length) {
        if(s1(p1) != s2(p2)) {
          if(dif == 1) return false
          dif += 1
          if(s1.length > s2.length) p1 += 1
          else p2 += 1
        }
        else { p1 += 1; p2 += 1 }
      }
      true
    }
  }


  // Connect all the nodes at same level of a binary tree recursively
  case class Node(v: Int) {
    var left : Node = null
    var right : Node = null
  }

  def connect(a : Node): Node =
  {
    def recursive(a: Node) : Array[(Node, Node)] = {
      var arr = Array((a, a))

      if(a.left == null && a.right == null) return arr
      if(a.left == null) {
        val right = recursive(a.right)
        for(i <- 0 to right.length) {
          right(i)._2.right = null
        }
        return arr ++ recursive(a.right)
      }

      if(a.right == null) {
        val left = recursive(a.left)
        for(i <- 0 to left.length-1) {
          left(i)._2.right = null
        }
        return arr ++ recursive(a.left)
      }

      val left = recursive(a.left)
      val right = recursive(a.right)
      for (i <- 0 to left.length-1) {
        if(i < right.length) {
          arr = arr :+ (left(i)._1, right(i)._2)
          left(i)._2.right = right(i)._1
          right(i)._2.right = null
        }
        else {
          arr = arr :+ (left(i)._1, left(i)._2)
          left(i)._2.right = null
        }
      }

      for(i <- left.length to right.length-1) {
        right(i)._2.right = null
        arr = arr :+ (right(i)._1, right(i)._2)
      }

      a.left.right = a.right
      arr
    }

    if(a==null) return null
    recursive(a)
    a
  }


  /*
  In a string detect the smallest window length with highest number of distinct characters. For eg.
A = “aabcbcdbca”, then ans would be 4 as of “dbca”
   */

  assert(maxWin("aabcbcdbca") == 4)
  assert(maxWin("aabca") == 3)


  def maxWin(s: String) : Int = {
    var (x, y, maxWinLen) = (0, 0, 0)
    val table = Array.fill(26)(-1)

    def getAppears(a: Int) = table(s(a) - 'a')
    def setAppears(a: Int) = { table(s(a) - 'a') = a }
    def resetAppears(a: Int) = { table(s(a) - 'a') = -1 }

    while (y < s.length) {
      if (getAppears(y) == -1) {
        setAppears(y)
        if (y - x + 1 > maxWinLen) {
          maxWinLen = y - x + 1
        }
        else
        {
          maxWinLen
        }
      } else {
        for(i <- x to getAppears(y) - 1)
          resetAppears(i)
        x = getAppears(y) + 1
        setAppears(y)
      }
      y += 1
    }

    maxWinLen
  }


  /*
  Given a linked list and a positive integer n, reverse the order of nodes in between n and last but n nodes.
example: 1->2->3->4->5, n=2. Output is 1->4->3->2->5
   */

  // assume n < list.length/2
  def reverse(node: Node, n: Int) : Node = {

    // simply revert a link list, return (head, tail)
    def simpleRev(p: Node): (Node, Node) = {
      if (p == null) return (null, null)

      var p1: Node = null
      var p2 = p

      while (p2 != null) {
        val p3 = p2.right
        p2.right = p1
        p1 = p2
        p2 = p3
      }

      return (p1, p)
    }

    def findNode(p: Node, n: Int): (Node, Node) = {
      if (p == null) return (null, null)

      var p1: Node = null
      var p2 = p
      var steps = 1
      while (steps < n && p2 != null) {
        p1 = p2
        p2 = p2.right
        steps += 1
      }

      if (steps < n || p2 == null) return (null, null)
      else return (p1, p2)
    }

    val tup1 = findNode(node, n)

    var p1 = node
    var p2 = tup1._1
    var p3 = tup1._2
    while (p3.right != null) {
      p3 = p3.right
      p2 = p2.right
      p1 = p1.right
    }

    val p4 = p3.right // p4 could be null

    val tup3 = simpleRev(tup1._2)
    tup1._1.right = tup3._1
    if (p4 != null) tup3._2.right = p4

    node
  }



  assert(isPathCross(Array(1,2,3,4)) == false)
  assert(isPathCross(Array(2,1,1,2)) == true)

  /*
  You are given a list of n float numbers x_1, x_2, x_3, ... x_n, where x_i > 0.
A traveler starts at point (0, 0) and moves x_1 metres to the north, then x_2 metres to the west, x_3 to the south, x_4 to the east and so on (after each move his direction changes counter-clockwise)
Write an single-pass algorithm that uses O(1) memory to determine, if the travelers path crosses itself, or not (i.e. if it's self-intersecting)
e.g.
2 1 1 2 -> crosses
1 2 3 4 -> doesn't cross

http://www.careercup.com/question?id=5143327210995712
   */
  def isPathCross(d: Array[Int]) : Boolean = {

    var buf = Array.empty[(Int,Int,Int)]

    def sort(x: Int, y: Int) : (Int, Int) = {
      return if(x > y) (y, x) else (x, y)
    }

    def cross() : Boolean = {
      if(buf.size < 4) return false

      val e5 = buf(buf.size-1)
      val e3 = buf(buf.size - 4)

      val (e5_small, e5_big) = sort(e5._2, e5._3)
      val (e3_small, e3_big) = sort(e3._2, e3._3)

      if(e5._1 >= e3_small && e5._1 <= e3_big && e3._1 >= e5_small && e3._1 <= e5_big) return true

      if(buf.size == 6) {
        val e0 = buf(0)
        val (e0_small, e0_big) = sort(e0._2, e0._3)
        if(e5._1 >= e0_small && e5._1 <= e0_big && e0._1 >= e5_small && e0._1 <= e5_big) return true
      }

      return false
    }

    for(j<-0 to d.length-1) {
      val direction = j%4
      var newElem : (Int, Int, Int) = null
      if(direction == 0 || direction == 1) {
        if(j==0) newElem = (0, 0, d(j))
        else newElem = (buf(j-1)._3, buf(j-1)._1, buf(j-1)._1 + d(j))
      } else if(direction == 2 || direction == 3) {
        newElem = (buf(j-1)._3, buf(j-1)._1, buf(j-1)._1 - d(j))
      }

      if(buf.length == 6)
        buf = buf.drop(1)

      buf = buf :+ newElem

      if(cross()) return true
    }

    return false
  }


  /*
Two strings s1 and s2 are given. You have make a new string s3 such that it has
both s1 and s2 as one of its subsequences and length of s3 is minimum.

apple pear -> applear
acd   acc  -> accd / acdc
cad  acd -> cacd
   */

  assert(makeString("apple", "pear") == "applear")
  assert(makeString("pear", "apple") == "applear")
  assert(makeString("bfc", "bac") == "bfac" || makeString("bfc", "bac") == "bafc")
  assert(makeString("acd", "cad") == "cacd")


  def makeString(s1: String, s2: String) : String = {

    def recurse(x: Int, y: Int, buffer: Array[Char]): String = {
      var buf = buffer

      if(x == s1.length || y == s2.length) {
        for (i <- y to s2.length - 1) {
          buf = buf :+ s2(i)
        }

        for (i <- x to s1.length - 1) {
          buf = buf :+ s1(i)
        }

        return buf.mkString
      }

      if (s1(x) == s2(y)) {
        val a1 = recurse(x+1, y+1, buf :+ s1(x))
        val a2 = recurse(x+1, y, buf :+ s1(x))
        val a3 = recurse(x, y+1, buf :+ s2(y))
        val smaller = if (a1.length > a2.length) a2 else a1
        return if(smaller.length < a3.length) smaller else a3
      } else {
        val a1 = recurse(x+1, y, buf :+ s1(x))
        val a2 = recurse(x, y+1, buf :+ s2(y))
        return if(a1.length < a2.length) a1 else a2
      }
    }

    if (s1 == null || s1.length == 0) return s2
    if (s2 == null || s2.length == 0) return s1
    val ret = recurse(0, 0, Array.empty[Char])
    //println(ret)
    ret
  }


  /*
  Given N matrix production (A1*A2*A3...*AN), different matrix can have different dimensions but they
  satisfy the production requirement. For example, A1 is 2x3, then A2 can be 3x4 but not 5x2.
  When a 3x2 matrix products a 2x4 matrix, the total number of number production required is 24.
  How to reduce the total number of number production when calculating (A1*A2*A3...*AN)?
   */

  assert(maxtrixProd(Vector((3,2),(2,5),(5,4),(4,2)))._1 == 68)

  // the return is (minProdNum, mergeSteps)
  // O(n * n!)
  def maxtrixProd(a: Vector[(Int, Int)]) : (Int, Vector[Int]) = {
    def recurse(a: Vector[(Int, Int)], nProd: Int, steps: Vector[Int]) : (Int, Vector[Int]) = {
      if(a.length == 1) return (nProd, steps)
      var min = 999999
      var minSteps : Vector[Int] = null

      for(i<-0 to a.length -2) {
        var a2 : Vector[(Int, Int)] = a
        val prods = a2(i)._1 * a2(i)._2 * a2(i+1)._2
        a2 = a2.updated(i+1, (a2(i)._1, a2(i+1)._2))
        a2 = a2.take(i) ++ a2.takeRight(a2.length - i - 1)

        val t = recurse(a2, prods + nProd, steps :+ i)
        if(min > t._1) {
          min = t._1
          minSteps = t._2
        }
      }

      (min, minSteps)
    }

    val min = recurse(a, 0, Vector.empty[Int])
  //  println(min._1)
  //  println(min._2.mkString(","))
    min
  }

  /*
  There are 4 types of coins (25 cent, 10 cent, 5 cent and 1 cent). Given a number,
  return the minimum number of coins whose sum equals to this number.
  What about the 4 types of coins changes to (25, 10, 6, 1)? Write code.
   */

  assert(cent5(59) == 7)

  def cent5(n: Int) : Int = {
    val coins = Array(0,0,0,0)

    var remain = n
    while(remain > 0) {
      if(remain >= 25) {
        coins(0) += remain/25
        remain = remain % 25
      } else if(remain >= 10) {
        coins(1) += remain/10
        remain = remain % 10
      } else if(remain >= 5) {
        coins(2) += remain / 5
        remain = remain % 5
      } else {
        coins(3) += remain
        remain = 0
      }
    }

    coins.foldLeft(0)((a, b) => a + b)
  }


  assert(cent6(13) == 3)

  // O(n*n!)
  def cent6(n: Int) : Int = {
    val values = Vector(25, 10, 6, 1)

    // return (num of coins, coin distribution vector)
    def recurse(m: Int, coins: Vector[Int]) : (Int, Vector[Int]) = {
      if(m < 0) return (Int.MaxValue, Vector.empty[Int])
      if(m == 0) return (coins.foldLeft(0)((a,b) => a + b), coins)

      var min = Int.MaxValue
      var coins2 : Vector[Int] = null

      for(i <- 0 to 3) {
        val a1 = recurse(m-values(i), coins.updated(i, coins(i) + 1))
        if(min > a1._1) {
          min = a1._1
          coins2 = a1._2
        }
      }

      return (min, coins2)
    }

    val a = recurse(n, Vector(0,0,0,0))._1
    //println(a)
    a
  }

  /*
  Given unsigned integer 'x', write an algorithm thet returns unsigned integer 'y' such that
  it has the same number of bits set as 'x' and is not equal to 'x' and the distance |x-y| is minimized.

Example:
x: 01
y: 10

Note that one bit is set and 'x' is not equal 'y'. You may assume that x is positive integer between zero and 2^32-2;
   */

  assert(getY(Vector(0,0,1,0,1)) == Vector(0,0,1,1,0))

  // java and scala does not have unsigned int.
  // below
  def getY(x: Vector[Int]) : Vector[Int] = {
    assume(x != 0)

    for(i <- x.length -1 to 0 by -1) {
      if(x(i) == 0) {
        if(i != x.length - 1) {
          if(x(i+1) == 1) {
            val ret = x.updated(i, 1).updated(i+1, 0)
            //println(ret.mkString(","))
            return ret
          }
        }
        if(i != 0) {
          if(x(i-1) == 1) {
            val ret = x.updated(i-1, 0).updated(i, 1)
            //println(ret.mkString(","))
            return ret
          }
        }
      }
    }

    Vector.empty[Int]
  }

  /*
  post order traversal for an N-ary tree iterative way
  http://www.careercup.com/question?id=5682612859305984
  You can modify the structure without using extra data structure
   */
  case class NaryNode(v: Int, var children: Vector[NaryNode])

  def Traverse(root: NaryNode) = {
    def recurse(prev: NaryNode, current: NaryNode): Unit = {
      if (current == null) return
      if (prev == null) current.children = current.children :+ null

      if (current.children.filter(_ == prev).length != 0 ||
        current.children.length == 1) {
        // if the prev node is one of the child of current, then
        // it means all children have been visited. Now visit current node.

        // if current node has no children, then visit current node

        // ==1 because we add one pointer to the children Vector
        // for pointing to the current node's sibling.
        println(current.v)

        // visit the current node's sibling
        recurse(current, current.children(current.children.length - 1))
      } else {
        // connect the children into a singular link list.
        for (i <- 0 to current.children.length - 2) {
          current.children(i).children = current.children(i).children :+ current.children(i + 1)
        }

        // connect the last child back to the current node.
        current.children(current.children.length - 1).children =
          current.children(current.children.length - 1).children :+ current

        // now visit the current node's first child.
        recurse(current, current.children(0))
      }
    }

    recurse(null, root)
  }

  /*
  Give you an array which has n integers,it has both positive and negative integers.
  Now you need sort this array in a special way.After that,the negative integers should in the front,
  and the positive integers should in the back.Also the relative position should not be changed.
eg. -1 1 3 -2 2 ans: -1 -2 1 3 2.
o(n)time complexity and o(1) space complexity is perfect.

http://www.careercup.com/question?id=5201559730257920
   */



  /*
  Given an int array which might contain duplicates,
  find the largest subset of it which form a sequence.
Eg. {1,6,10,4,7,9,5}
then ans is 4,5,6,7

Sorting is an obvious solution. Can this be done in O(n) time
   */

  assert(largestSubset(Vector(1,6,10,4,7,9,5)) == Vector(4,5,6,7))

  def largestSubset(a: Vector[Int]) : Vector[Int] = {
    val hash = new scala.collection.mutable.HashMap[Int,Int]()
    var ret = Vector.empty[Int]
    var longest = -1
    var longestPtr = -1

    def getLen(i: Int) : Int = {
      if(hash(i) >= i) return hash(i) -i +1
      else return i - hash(i) + 1
    }

    def handleLongest(i: Int) = {
      val newLen = getLen(i)
      if(longest < newLen) {
        longest = newLen
        longestPtr = i
      }
    }

    // 1  (1,1) (6,6) (10,10) (4,4)
    // 2  (1,1) (6,7) (10,10) (4,4) (7,6)
    // 2   (1,1) (6,7) (10,9)  (4,4) (7,6) (9,10)
    // 4   (1,1) (6,7) (10,9)  (4,7) (7, 4) (9,10)

    for(i <- 0 to a.length-1) {
      if(hash.contains(a(i)) == false) {
        // fill the gap
        if(hash.contains(a(i)-1) && hash.contains(a(i)+1)) {
          val low = hash(a(i)-1)
          val high = hash(a(i)+1)
          hash(low) = high
          hash(high) = low
          handleLongest(low)
        } else if(hash.contains(a(i)-1)) {
          // add high end
            val front = hash(a(i)-1)
            hash(front) = a(i)
            hash(a(i)) = front
            handleLongest(front)
        } else if(hash.contains(a(i)+1)) {
          // add low end
          val end = hash(a(i)+1)
          hash(end) = a(i)
          hash(a(i)) = end
          handleLongest(end)
        }
        else {
          hash(a(i)) = a(i)
          handleLongest(a(i))
        }
      }
    }

    var low = 0
    var high = 0
    if(hash(longestPtr) >= longestPtr) {
      low = longestPtr
      high = hash(longestPtr)
    } else {
      low = hash(longestPtr)
      high = longestPtr
    }

    for(i<- low to high) {
      ret = ret :+ i
    }

    ret
  }


  /*
  Given a N*N Matrix.
All rows are sorted, and all columns are sorted.
Find the Kth Largest element of the matrix.
   */
  class MinHeap() {
    def insert(a: (Int, Int)) = {}
    def getMin() : (Int, Int) = { (0,0) }
    def removeTop() = {}
  }

  def findKthElem(m: Vector[Vector[Int]], k: Int) : Int = {
    // use min heap
    var p = 0
    val N = m.length
    var cx = Vector.fill[Int](m.length)(0)
    var cy = cx

    var ret = 0
    val heap = new MinHeap

    heap.insert((0, 0))
    while (p < k) {
      val (x, y) = heap.getMin()
      ret = m(x)(y)

      cx = cx.updated(x, {
        if (y < N - 2) y + 1 else y
      })

      cy = cy.updated(y, {
        if (x < N - 2) x + 1 else x
      })

      // check (x+1,y)
      if (x < N - 1 && y <= cx(x + 1)) {
        heap.insert((x + 1, y))
      }

      // check (x, y+1)
      if (y < N - 1 && x <= cy(y + 1)) {
        heap.insert((x, y + 1))
      }

      // remove the top
      heap.removeTop()
      p += 1
    }

    ret
  }



  /*
  input a text and a pattern. The pattern can have *, say a*b, a**b, *ab...
  find whether the pattern matches the whole input text.
   */

  assert(pm("abc", "*") == true)
  assert(pm("abcb", "*a*b") == true)
  assert(pm("abcdadab", "a**b") == true)


  def pm(s: String, p: String) : Boolean = {
    if(s.isEmpty && p.isEmpty) return true
    if(s.isEmpty || p.isEmpty) return false
    if(p(0) == s(0))
      return pm(s.substring(1), p.substring(1))
    if(p(0) == '*') {
      return pm(s, p.substring(1)) ||
        pm(s.substring(1), p) ||
        pm(s.substring(1), p.substring(1))
    }
    else return false
  }

  /*
  write "bool isSplitable(int[] arr)" to return true if the arr can be
  divided into two parts having the equal sums; return false otherwise.
   */


  /* connect siblings into a list using O(1) storage */
  case class LNode(left: LNode, right: LNode, var next: LNode)

  def linkSibling(root: LNode) : LNode = {

    def getFirstNodeHavingChild(sib: LNode) : LNode = {
      if (sib == null) return null

      var s = sib
      while (s != null || s.left != null || s.right != null) s = s.next
      if (s == null) return null
      else if (s.left != null) return s.left
      s.right
    }

    if(root == null) return null

    var p = root
    var first = root
    var first2 : LNode = null

    while(p != null) {
      val sibling = p.next

      if(p.left!=null) {
        p.left.next = if(p.right != null) p.right else getFirstNodeHavingChild(sibling)
        if(first2 == null) first2 = p.left
      }

      if(p.right!=null) {
        p.right.next = getFirstNodeHavingChild(sibling)
        if (first2 == null) first2 = p.right
      }

      if(p.next != null) p = p.next
      else {
        first = first2
        first2 = null
        p = first
      }
    }

    root
  }

  /*
  Given API:
int Read4096(char* buf);
It reads data from a file and records the position so that the next time when it is called it
 read the next 4k chars (or the rest of the file, whichever is smaller) from the file.
The return is the number of chars read.

Todo: Use above API to Implement API
"int Read(char* buf, int n)" which reads any number of chars from the file.
assume buf is big enough
   */
/*
  def Read4096(buf: Array[Char]) : Int  = { 0 }

  def Read(buf: Array[Char], n: Int) : Int = {
    var remain = n
    var fileEnd = false
    val ibuf = Array.empty[Char]
    while(remain > 0 && fileEnd == false) {
      val t : Int = Read4096(ibuf)

      ibuf.map(buf = buf + _)

      if(t<=remain) remain -= t
      else remain = 0

      if(t < 4096) fileEnd = true
    }

    n - remain
  }*/


  /*
  Write a program to process the matrix. If an element is 0 at ith row and jth column,
  then make the whole ith row and jth column to 0.

Constraints:
Space complexity should be O(1)
Time complexity - Only single pass is allowed. Note that single pass is not O(n).
This is single pass : An element will read and written only ones.
   */
  def set0(matrix: Array[Array[Int]]) : Array[Array[Int]] = {
    for(i <- 0 to matrix.length-1) {
      for(j <- 0 to matrix(i).length - 1) {
        if(matrix(i)(j) == 0) {
          for(x <- 0 to i-1) {
            for(y <- 0 to j-1) {
              if(matrix(x)(y) != 0) matrix(x)(y) == 0
            }
            for(y <- j+1 to matrix(i).length-1) {
              if(matrix(x)(y) != 0) matrix(x)(y) == 0
            }
          }

          for(x <- i+1 to matrix.length-1) {
            for(y<- 0 to j-1) {
              if(matrix(x)(y) != 0) matrix(x)(y) == 0
            }
            for(y<-j+1 to matrix(i).length-1) {
              if(matrix(x)(y) != 0) matrix(x)(y) == 0
            }
          }
        }
      }
    }
    return matrix
  }
}