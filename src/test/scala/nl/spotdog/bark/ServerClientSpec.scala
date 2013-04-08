package nl.spotdog.bark

import nl.spotdog.bark.client._
import nl.spotdog.bark.server._
import org.specs2.mutable.Specification
import BarkServerModule._
import nl.spotdog.bark.protocol._
import ETF._
import akka.actor.ActorSystem

import scalaz._
import Scalaz._

class ServerClientSpec extends Specification with BarkRouting {
  val modules = module('calc) {
    call('add)((a: Int, b: Int) ⇒ a + b) ~
      call('length)((s: String) ⇒ s.length)
  } ~
    module('spatial) {
      call('cubicalString)((a: Int, b: Int, c: Int) ⇒ (a * b * c).toString)
    } ~
    module('identity) {
      call('list)((a: List[Int]) ⇒ a)
    }

  implicit val system = ActorSystem("test-system")
  val (client, server) = {
    val server = BarkServer(2, "CalcService")(new BarkRouter(modules))
    server.run(9999)
    Thread.sleep(1000)
    val client = BarkClient.apply("localhost", 9999, 2, "Calc client")
    (client, server)
  }

  "A Client" should {
    "be able to be send and receive messages to and from a server" in {
      val reqA = ((client |?| 'calc |/| 'add) <<? (1, 4))
      val resA = reqA.map(x ⇒ x.as[Int])
      val compA = resA.unsafeFulFill.toOption.get.get == 5

      val reqB = ((client |?| 'calc |/| 'length) <<? Tuple1("TestString"))
      val resB = reqB.map(x ⇒ x.as[Int])
      val compB = resB.unsafeFulFill.toOption.get.get == "TestString".length

      val reqC = ((client |?| 'spatial |/| 'cubicalString) <<? (2, 5, 7))
      val resC = reqC.map(x ⇒ x.as[String])
      val compC = resC.unsafeFulFill.toOption.get.get == (2 * 5 * 7).toString

      val reqD = ((client |?| 'identity |/| 'list) <<? Tuple1(List(1, 2, 4, 5, 6, 7)))
      val resD = reqD.map(x ⇒ x.as[List[Int]])
      val compD = resD.unsafeFulFill.toOption.get.get.corresponds(List(1, 2, 4, 5, 6, 7))(_ == _)

      compA && compB && compC && compD
    }
  }
}
