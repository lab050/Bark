package nl.spotdog.bark

import org.specs2.mutable.Specification

import nl.spotdog.bark.data_format.Bark._
import akka.actor.IO.Chunk
import java.util.Date
import play.api.libs.iteratee.Enumerator

import scalaz._
import Scalaz._

import concurrent.Await
import concurrent.duration.Duration

import akka.util.{ ByteStringBuilder, ByteString }

import nl.spotdog.bark.server.builders._

import scala.concurrent.ExecutionContext.Implicits.global

class FunctionSpec extends Specification {
  "A call" should {
    "be able to be build" in {

      val callBuilder = new BarkCallBuilder { val name = 'test }
      callBuilder((name: String) ⇒ name.length)
      true
    }

    "be able to be executed" in {

      val callBuilder = new BarkCallBuilder { val name = 'test }
      val call = callBuilder((name: String) ⇒ name.length)
      val a = Tuple1("Gideon")

   //   val ra = call.iteratee(Chunk(toBark(a)))._1.get

    //  ra == a._1.length
    }
  }

  "A cast" should {
    "be able to be build" in {

      val castBuilder = new BarkCastBuilder { val name = 'test }
      castBuilder((name: String) ⇒ println("WORKS!"))
      true
    }

    "be able to be executed" in {

      val castBuilder = new BarkCastBuilder { val name = 'test }
      val cast = castBuilder((name: String) ⇒ println("WORKS!"))
      val a = Tuple1("Gideon")

 //     val ra = cast.iteratee(Chunk(toBark(a)))._1.get
//      true
    }
  }

}