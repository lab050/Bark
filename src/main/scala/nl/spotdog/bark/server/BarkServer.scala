package nl.spotdog.bark.server

import akka.actor.{ Props, ActorSystem }
import akka.actor.ActorRef

import nl.gideondk.sentinel.server._

import nl.spotdog.bark.protocol.BarkMessaging._
import akka.io.LengthFieldFrame

class BarkServer(description: String)(router: BarkRouter)(implicit system: ActorSystem) {
  var serverRef: Option[ActorRef] = None

  def ctx = new HasByteOrder {
    def byteOrder = java.nio.ByteOrder.BIG_ENDIAN
  }

  val stages = new LengthFieldFrame(1024 * 1024 * 1024) // Max 1Gb messages 

  def stop {
    if (serverRef.isDefined) {
      system stop serverRef.get
      serverRef = None
    }
  }

  def run(port: Int) = {
    if (serverRef.isEmpty) serverRef = Some(SentinelServer(port, router.handle, description)(ctx, stages))
  }
}

object BarkServer {
  def apply(description: String)(modules: BarkServerModules)(implicit system: ActorSystem) = {
    new BarkServer(description)(new BarkRouter(modules))
  }
}