//package nl.spotdog.bark
//
//import nl.spotdog.bark.client.BarkRequest
//import org.specs2.mutable.Specification
//
//import nl.spotdog.bark.serialization.Bark._
//import akka.actor.IO.Chunk
//import akka.actor.IO._
//import akka.actor._
//
//import java.util.Date
//import play.api.libs.iteratee.Enumerator
//
//import scalaz._
//import Scalaz._
//import effect._
//
//import concurrent.Await
//import concurrent.duration.Duration
//
//import concurrent.{ Promise, Future }
//
//import akka.util.{ ByteStringBuilder, ByteString }
//
//import nl.spotdog.bark.server._
//import nl.spotdog.bark.server.builders._
//import nl.spotdog.bark.monads.IterateeZMonad._
//import scala.concurrent.ExecutionContext.Implicits.global
//
//import nl.spotdog.bark.actors._
//import nl.spotdog.bark.serialization._
//import nl.spotdog.bark.serialization.Bark._
//
//import akka.actor.{ Props, ActorSystem }
//
//import concurrent._
//import concurrent.duration._
//
//class ServerClientSpec extends Specification with BarkRouting {
//  import BarkServerModule._
//
//  implicit val system = ActorSystem()
//  val server = startServer(serverPort = 8080) {
//    module("hello_world") {
//      call("hello") {
//        (name: String) ⇒
//          "hello"+name
//      }
//    } ~
//      module("list_mutation") {
//        call("add_to_list") {
//          (list: List[Int], add: Int) ⇒
//            list.map(_ + add)
//        }
//      }
//  }
//
//  Thread.sleep(500)
//
//  val client = system.actorOf(Props(new BarkClientActor("localhost", 8080)), name = "client-actor")
//
//  private[this] def createIOAction(commandBytes: ByteString) = {
//    val action = {
//      val promise = Promise[ByteString]
//      client ! BarkRequest(commandBytes, promise)
//      promise.future
//    }.point[IO]
//
//    action
//  }
//
//  "Perfrmance" should {
//    "be sufficient" in {
//      //val a = (1, 2, 4, "A")
//      //val c = "123213213123213213213123213123123213123123"
//      // val n = 90000
//      // val t2 = System.currentTimeMillis
//      // val enums = Vector.fill(n)(toBark(c))
//      // //val enums = Vector.fill(n)(Enumerator(toBark(a))).foldLeft(Enumerator(toBark(a)))((b, a) ⇒ b >>> a)
//      // val d2 = System.currentTimeMillis - t2
//
//      // println(":\n*** number enum  ops/s: "+n / (d2 / 1000.0)+"\n")
//
//      // //val iterees = Future.sequence(enums.map(x ⇒ Enumerator(x) |>>> BarkReader[String]))
//
//      // val t3 = System.currentTimeMillis
//
//      // val iter = enums.foldLeft(akka.actor.IO.takeList(n)(BarkReader[String]))((a, b) ⇒ a(Chunk(b))._1).get
//
//      // // enums.map { x ⇒
//      // //   val r1 = BarkReader[Int]
//      // //   val ra = r1(Chunk(x))._1.get
//      // // }
//      // //val enums = Vector.fill(n)(Enumerator(toBark(a))).foldLeft(Enumerator(toBark(a)))((b, a) ⇒ b >>> a)
//      // val d3 = System.currentTimeMillis - t3
//
//      // println(":\n*** number enum  ops/s: "+n / (d3 / 1000.0)+"\n")
//
//      //      val iter = Vector.fill(900)(BarkReader[Tuple4[Int, Int, Int, String]]).foldLeft(BarkReader[Tuple4[Int, Int, Int, String]])((b, a) ⇒ b >> a)
//
//      // val iter = Iteratee.foreach[ByteString](x ⇒ x.length)
//      // val n = 500000
//      // val t = System.currentTimeMillis
//      // for (i <- 1 to n) toJson(a).toString
//      // val d = System.currentTimeMillis - t
//
//      // val r = toJson(a)
//      // println(":\n*** number of writes ops/s: "+n / (d / 1000.0)+"\n")
//
//      // val t1 = System.currentTimeMillis
//      // for (i <- 1 to n) fromJson[Blaat](r)
//      // val d1 = System.currentTimeMillis - t1
//
//      // println(":\n*** number of reads ops/s: "+n / (d1 / 1000.0)+"\n")
//
//      val a = (1, 4, "BB", 5)
//      val n = 800000
//      val t = System.currentTimeMillis
//      for (i ← 1 to n) {
//        toBark(a)
//        // val r1 = BarkReader[Tuple3[Int, String, String]]
//        // val ra = r1(Chunk(toBark(a)))._1.get
//      }
//      val d = System.currentTimeMillis - t
//
//      println(":\n*** number of writes ops/s: "+n / (d / 1000.0)+"\n")
//
//      // val r = toJson(a)
//      // println(":\n*** number of writes ops/s: "+n / (d / 1000.0)+"\n")
//
//      val bb = toBark(a)
//      val t1 = System.currentTimeMillis
//      for (i ← 1 to n) {
//        val r1 = fromBark[Tuple4[Int, Int, String, Int]](bb)
//        // val ra = r1(Chunk(bb))._1.get
//      }
//      val d1 = System.currentTimeMillis - t1
//
//      println(":\n*** number of reads ops/s: "+n / (d1 / 1000.0)+"\n")
//
//      true
//    }
//  }
//  "A client" should {
//    "be able to send request to a server" in {
//      for (i ← 0 to 250) {
//        Thread.sleep(5)
//        runBench
//      }
//
//      true
//    }
//  }
//
//  def runBench = {
//    val n = 30000
//    val comm = ('call, 'hello_world, 'hello, Tuple1("Gideon"))
//    println("Sending commands")
//
//    val futuresA = for (i ← 1 to n) yield createIOAction(toBark(comm))
//    val ioActionsA = futuresA.toList.sequence
//
//    val seqIOActs = ioActionsA.map(x ⇒ Future.sequence(x))
//
//    val t = System.currentTimeMillis
//    val result = Await.result(seqIOActs.unsafePerformIO, Duration.Inf)
//    val d = System.currentTimeMillis - t
//
//    println(":\n*** number of ops/s: "+n / (d / 1000.0)+"\n")
//  }
//}
//
//object Bench {
//
//}