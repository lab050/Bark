package nl.spotdog.bark

import org.specs2.mutable.Specification
import nl.spotdog.bark.protocol._
import nl.spotdog.bark.protocol.ETF._

import akka.actor.IO.Chunk
import akka.actor.IO._
import java.util.Date
import scalaz._
import Scalaz._
import concurrent.Await
import concurrent.duration.Duration
import play.api.libs.iteratee._

case class TestClass(a: Int, b: String, d: (Int, List[Int]))

class SerializationSpec extends Specification {
  "A Int" should {
    "be able to be (de)serialized" in {
      val a = toETF(8887)
      val b = toETF(43)
      val c = toETF(-47163)

      val ra = fromETF[Int](a).get
      val rb = fromETF[Int](b).get
      val rc = fromETF[Int](c).get

      ra == 8887 && rb == 43 && rc == -47163
    }
  }

  "A Double" should {
    "be able to be (de)serialized" in {
      val a = toETF(43.0544322)
      val b = toETF(-888.32123222)
      val c = toETF(0.349954)

      val ra = fromETF[Double](a).get
      val rb = fromETF[Double](b).get
      val rc = fromETF[Double](c).get

      ra == 43.0544322 && rb == -888.32123222 && rc == 0.349954
    }
  }

  "A BigInt" should {
    "be able to be (de)serialized" in {
      val smallBigInt = BigInt(432234) << 16
      val largeBigInt = BigInt(54334858) << 3200

      val ra = fromETF[BigInt](toETF(smallBigInt)).get
      val rb = fromETF[BigInt](toETF(largeBigInt)).get

      ra == smallBigInt && rb == largeBigInt
    }
  }

  "A String" should {
    "be able to be (de)serialized" in {
      val a = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque id ipsum a erat faucibus fringilla. Maecenas vehicula scelerisque enim sit amet consequat. Ut vitae lacinia tortor. Donec tincidunt diam vitae diam dictum eu condimentum urna luctus. Nulla nibh metus, lacinia sed tempor eu, viverra non nisl. Quisque quam lorem, aliquet fermentum interdum ut, aliquam id nisi. Nunc ipsum magna, bibendum at tincidunt eget, cursus et ipsum. Cras ut lectus enim, ut ultrices libero. In eget commodo orci. Nam commodo lacus lacus. Vivamus vitae purus tellus. Maecenas bibendum accumsan blandit. Aenean nec lacus nisl, eget aliquam metus."

      val ra = fromETF[String](toETF(a)).get

      a == ra
    }
  }

  "A Symbol" should {
    "Should be able to be (de)serialized" in {
      val a = 'testSymbol

      val ra = fromETF[Symbol](toETF(a)).get

      a == ra
    }
  }

  "A List" should {
    "be able to be (de)serialized" in {
      val a = List(1, 2, 3, 4)

      val ra = fromETF[List[Int]](toETF(a)).get

      a == ra
    }
  }

  "A Tuple" should {
    "be able to be (de)serialized" in {
      val a = ("aaa", 2, 3)

      val ra = fromETF[Tuple3[String, Int, Int]](toETF(a)).get
      a == ra
    }
  }

  "A Boolean" should {
    "be able to be (de)serialized" in {
      val a = false
      val b = true

      val ra = fromETF[Boolean](toETF(a)).get
      val rb = fromETF[Boolean](toETF(b)).get

      a == ra && b == rb
    }
  }

  "A Map" should {
    "be able to be (de)serialized" in {
      val a = Map(1 -> 4, 3 -> 3)
      val b = Map("A" -> List(4, 4, 5, 6), "B" -> List(3, 4))

      val ra = fromETF[Map[Int, Int]](toETF(a)).get
      val rb = fromETF[Map[String, List[Int]]](toETF(b)).get

      a == ra && b == rb
    }
  }

  "A Date" should {
    "be able to be (de)serialized" in {
      val a = new Date()

      val ra = fromETF[Date](toETF(a)).get

      a == ra
    }
  }

  //   // "Complex types" should {
  //   //   "be able to be (de)serialized" in {
  //   //     val a = (false, "2", List(1, 2, 3, 4), Map("a" -> 3, "b" -> 5), (1, 2))

  //   //     val r1 = BarkReader[(Boolean, String, List[Int], Map[String, Int], (Int, Int))]
  //   //     val ra = r1(Chunk(toETF(a)))._1.get

  //   //     a == ra
  //   //   }
  //   // }
}