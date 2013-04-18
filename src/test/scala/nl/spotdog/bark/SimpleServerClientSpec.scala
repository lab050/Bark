package nl.spotdog.bark

import org.specs2.mutable.Specification
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.ActorSystem
import scalaz._
import Scalaz._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.duration._

import nl.spotdog.bark.client._
import nl.spotdog.bark.server._
import BarkServerModule._
import nl.spotdog.bark.protocol._
import ETF._

class SimpleServerClientSpec extends Specification with BarkRouting {
  implicit val duration = Duration(10, SECONDS)

  val modules = module("calc") {
    call("add")((a: Int, b: Int) ⇒ Future(a + b)) ~
      call("length")((s: String) ⇒ Future(s.length))
  } ~
    module("spatial") {
      call("cubicalString")((a: Int, b: Int, c: Int) ⇒ Future((a * b * c).toString))
    } ~
    module("identity") {
      call("list")((a: List[Int]) ⇒ Future(a))
    } ~
    module("generation") {
      call("generate_5")(() ⇒ Future(5))
    }

  lazy val (client, server) = {
    val serverSystem = ActorSystem("server-system")
    val server = BarkServer(24, "CalcService")(modules)(serverSystem)
    server.run(9999)
    Thread.sleep(1000)
    val clientSystem = ActorSystem("client-system")
    val client = BarkClient("localhost", 9999, 4, "Calc client")(clientSystem)
    (client, server)
  }

  "A Client" should {
    "be able to be send and receive messages to and from a server" in {
      val reqA = ((client |?| "calc" |/| "add") <<? (1, 4))
      val resA = reqA.map(x ⇒ x.as[Int])
      val compA = resA.run.get.get == 5

      val reqB = ((client |?| "calc" |/| "length") <<? "TestString")
      val resB = reqB.map(x ⇒ x.as[Int])
      val compB = resB.copoint.get == "TestString".length

      val reqC = ((client |?| "spatial" |/| "cubicalString") <<? (2, 5, 7))
      val resC = reqC.map(x ⇒ x.as[String])
      val compC = resC.copoint.get == (2 * 5 * 7).toString

      val reqD = ((client |?| "identity" |/| "list") <<? Tuple1(List(1, 2, 4, 5, 6, 7)))
      val resD = reqD.map(x ⇒ x.as[List[Int]])
      val compD = resD.copoint.get.corresponds(List(1, 2, 4, 5, 6, 7))(_ == _)

      val reqE = (client |?| "generation" |/| "generate_5") <<? ()
      val resE = reqE.map(_.as[Int])
      val compE = resE.copoint.get == 5

      compA && compB && compC && compD && compE
    }
  }
}
