package nl.spotdog.bark.server

import akka.actor.{ Props, ActorSystem }
import nl.spotdog.bark.actors._
import akka.actor.ActorRef
import nl.gideondk.sentinel.server._

import nl.spotdog.bark.protocol.BarkMessaging._
import akka.io.LengthFieldFrame

class BarkServer(numberOfWorkers: Int, description: String)(router: BarkRouter)(implicit system: ActorSystem) {
  var serverRef: Option[ActorRef] = None

  def ctx = new HasByteOrder {
    def byteOrder = java.nio.ByteOrder.BIG_ENDIAN
  }

  val stages = new LengthFieldFrame(1024 * 1024 * 50) // Max 50MB messages 

  def stop {
    if (serverRef.isDefined) system stop serverRef.get
  }

  def run(port: Int) = {
    val actor = SentinelServer.randomRouting(port, numberOfWorkers, router.handle, description)(ctx, stages, true)
  }
}

object BarkServer {
  def apply(numberOfWorkers: Int, description: String)(router: BarkRouter)(implicit system: ActorSystem) = {
    new BarkServer(numberOfWorkers, description)(router)
  }
}