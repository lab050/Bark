package nl.spotdog.bark

import org.specs2.mutable.Specification
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.ActorSystem
import scalaz._
import Scalaz._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

import nl.spotdog.bark.client._
import nl.spotdog.bark.server._
import BarkServerModule._
import nl.spotdog.bark.protocol._
import ETF._

class SimpleServerClientSpec extends Specification with BarkRouting {
  val modules = module("calc") {
    call("add")((a: Int, b: Int) ⇒ a + b) ~
      call("length")((s: String) ⇒ s.length)
  } ~
    module("spatial") {
      call("cubicalString")((a: Int, b: Int, c: Int) ⇒ (a * b * c).toString)
    } ~
    module("identity") {
      call("list")((a: List[Int]) ⇒ a)
    }

  val (client, server) = {
    val serverSystem = ActorSystem("server-system")
    val server = BarkServer(24, "CalcService")(new BarkRouter(modules))(serverSystem)
    server.run(9999)
    Thread.sleep(1000)
    val clientSystem = ActorSystem("server-system")
    val client = BarkClient.apply("localhost", 9999, 4, "Calc client")(clientSystem)
    (client, server)
  }

  "A Client" should {
    "be able to be send and receive messages to and from a server" in {
      val reqA = ((client |?| "calc" |/| "add") <<? (1, 4))
      val resA = reqA.map(x ⇒ x.as[Int])
      val compA = resA.unsafeFulFill.toOption.get.get == 5

      val reqB = ((client |?| "calc" |/| "length") <<? "TestString")
      val resB = reqB.map(x ⇒ x.as[Int])
      val compB = resB.unsafeFulFill.toOption.get.get == "TestString".length

      val reqC = ((client |?| "spatial" |/| "cubicalString") <<? (2, 5, 7))
      val resC = reqC.map(x ⇒ x.as[String])
      val compC = resC.unsafeFulFill.toOption.get.get == (2 * 5 * 7).toString

      val reqD = ((client |?| "identity" |/| "list") <<? Tuple1(List(1, 2, 4, 5, 6, 7)))
      val resD = reqD.map(x ⇒ x.as[List[Int]])
      val compD = resD.unsafeFulFill.toOption.get.get.corresponds(List(1, 2, 4, 5, 6, 7))(_ == _)

      compA && compB && compC && compD
    }

    "be able to use to the server in timely fashion" in {
      val num = 50000
      val mulActs = for (i ← 1 to num) yield ((client |?| "calc" |/| "add") <<? (1, 4))
      val ioActs = mulActs.toList.map(_.run).sequence
      val futs = ioActs.map(x ⇒ Future.sequence(x.map(_.run)))

      val fut = futs.unsafePerformIO
      BenchmarkHelpers.timed("Handling " + num + " requests", num) {
        Await.result(fut, Duration.apply(10, scala.concurrent.duration.SECONDS))
        true
      }
      val res = Await.result(fut, Duration.apply(10, scala.concurrent.duration.SECONDS))
      res.map(_.toOption.get).filterNot(x ⇒ x.as[Int].get == 5).length == 0
    }
  }
}
