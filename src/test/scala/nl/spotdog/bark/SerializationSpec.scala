package nl.spotdog.bark

import org.specs2.mutable.Specification
import nl.spotdog.bark.data_format.Bark._
import akka.actor.IO.Chunk
import akka.actor.IO._
import java.util.Date
import scalaz._
import Scalaz._
import concurrent.Await
import concurrent.duration.Duration
import play.api.libs.iteratee._
import nl.spotdog.bark.data_format.Bark
import nl.spotdog.bark.messages.Request
import nl.spotdog.bark.messages.BarkRequestConverters

case class TestClass(a: Int, b: String, d: (Int, List[Int]))

class SerializationSpec extends Specification {
  // "A Small Int" should {
  //   "be able to be (de)serialized" in {
  //     val a = toBark(4.toByte)
  //     val b = toBark(-15.toByte)

  //     val r1 = BarkReader[Byte]
  //     val ra = r1(Chunk(a))._1.get

  //     val r2 = BarkReader[Byte]
  //     val rb = r2(Chunk(b))._1.get

  //     ra == 4.toByte && rb == -15.toByte
  //   }
  // }

  "A Int" should {
    "be able to be (de)serialized" in {
      val a = toBark(8887)
      val b = toBark(43)
      val c = toBark(-47163)

      val ra = fromBark[Int](a).get
      val rb = fromBark[Int](b).get
      val rc = fromBark[Int](c).get

      ra == 8887 && rb == 43 && rc == -47163
    }
  }

  "A Double" should {
    "be able to be (de)serialized" in {
      val a = toBark(43.0544322)
      val b = toBark(-888.32123222)
      val c = toBark(0.349954)

      val ra = fromBark[Double](a).get
      val rb = fromBark[Double](b).get
      val rc = fromBark[Double](c).get

      ra == 43.0544322 && rb == -888.32123222 && rc == 0.349954
    }
  }

  "A BigInt" should {
    "be able to be (de)serialized" in {
      val smallBigInt = BigInt(432234) << 16
      val largeBigInt = BigInt(54334858) << 3200

      val ra = fromBark[BigInt](toBark(smallBigInt)).get
      val rb = fromBark[BigInt](toBark(largeBigInt)).get

      ra == smallBigInt && rb == largeBigInt
    }
  }

  "A String" should {
    "be able to be (de)serialized" in {
      val a = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque id ipsum a erat faucibus fringilla. Maecenas vehicula scelerisque enim sit amet consequat. Ut vitae lacinia tortor. Donec tincidunt diam vitae diam dictum eu condimentum urna luctus. Nulla nibh metus, lacinia sed tempor eu, viverra non nisl. Quisque quam lorem, aliquet fermentum interdum ut, aliquam id nisi. Nunc ipsum magna, bibendum at tincidunt eget, cursus et ipsum. Cras ut lectus enim, ut ultrices libero. In eget commodo orci. Nam commodo lacus lacus. Vivamus vitae purus tellus. Maecenas bibendum accumsan blandit. Aenean nec lacus nisl, eget aliquam metus."

      val ra = fromBark[String](toBark(a)).get

      a == ra
    }
  }

  "A Symbol" should {
    "Should be able to be (de)serialized" in {
      val a = 'testSymbol

      val ra = fromBark[Symbol](toBark(a)).get

      a == ra
    }
  }

  "A List" should {
    "be able to be (de)serialized" in {
      val a = List(1, 2, 3, 4)

      val ra = fromBark[List[Int]](toBark(a)).get

      a == ra
    }
  }

  "A Tuple" should {
    "be able to be (de)serialized" in {
      val a = ("aaa", 2, 3)

      val ra = fromBark[Tuple3[String, Int, Int]](toBark(a)).get
      a == ra
    }
  }

  //   // // // "A Big tuple" should {
  //   // // //   "be able to be (de)serialized" in {
  //   // // //     val a = LargeTuple(Seq.fill(500)((1, 4)))

  //   // // //     val ra = Await.result(Enumerator(toBark(a)) |>>> readLargeTuple, Duration.Inf)

  //   // // //     a == ra
  //   // // //   }
  //   // // // }

  //   // // // "A case class" should {
  //   // // //   "be able to be (de)serialized" in {
  //   // // //     // Silly test, easier case class serialization will be added soon...
  //   // // //     val a = TestClass(54, "Lorem Ipsum", (5 -> List(5, 33, 21)))
  //   // // //     val ra = Await.result(Enumerator(toBark(TestClass.unapply(a).get)) |>>> readSmallTuple, Duration.Inf)
  //   // // //     a == TestClass.tupled(ra.asInstanceOf[(Int, String, (Int, List[Int]))])
  //   // // //   }
  //   // // // }

  "A Boolean" should {
    "be able to be (de)serialized" in {
      val a = false
      val b = true

      val ra = fromBark[Boolean](toBark(a)).get
      val rb = fromBark[Boolean](toBark(b)).get

      a == ra && b == rb
    }
  }

  "A Map" should {
    "be able to be (de)serialized" in {
      val a = Map(1 -> 4, 3 -> 3)
      val b = Map("A" -> List(4, 4, 5, 6), "B" -> List(3, 4))

      val ra = fromBark[Map[Int, Int]](toBark(a)).get
      val rb = fromBark[Map[String, List[Int]]](toBark(b)).get

      a == ra && b == rb
    }
  }

  "A Date" should {
    "be able to be (de)serialized" in {
      val a = new Date()

      val ra = fromBark[Date](toBark(a)).get

      a == ra
    }
  }

  "A Call" should { 
    "be able to be (de)serialized" in {
    	val serializer = new BarkRequestConverters {}
    	import serializer._
    	
    	val a = Request.Call('test, 'fun, ("A", 4, 5, "D"))
    	val ra = fromBark[Request.Call[(String, Int, Int, String)]](toBark(a)).get
    	
    	a == ra
    }
  }
  
  "A Cast" should { 
    "be able to be (de)serialized" in {
    	val serializer = new BarkRequestConverters {}
    	import serializer._
    	
    	val a = Request.Cast('test, 'fun, ("A", 4, 5, "D"))
    	val ra = fromBark[Request.Cast[(String, Int, Int, String)]](toBark(a)).get
    	
    	a == ra
    }
  }
  
  //   // "Complex types" should {
  //   //   "be able to be (de)serialized" in {
  //   //     val a = (false, "2", List(1, 2, 3, 4), Map("a" -> 3, "b" -> 5), (1, 2))

  //   //     val r1 = BarkReader[(Boolean, String, List[Int], Map[String, Int], (Int, Int))]
  //   //     val ra = r1(Chunk(toBark(a)))._1.get

  //   //     a == ra
  //   //   }
  //   // }
}