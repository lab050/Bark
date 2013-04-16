package nl.spotdog.bark.server

import akka.actor.{ Props, ActorSystem }
import akka.actor.ActorRef

import nl.gideondk.sentinel.server._

import nl.spotdog.bark.protocol.BarkMessaging._
import akka.io.LengthFieldFrame

class BarkServer(numberOfWorkers: Int, description: String)(router: BarkRouter)(implicit system: ActorSystem) {
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
    if (serverRef.isEmpty) serverRef = Some(SentinelServer.randomRouting(port, numberOfWorkers, router.handle, description)(ctx, stages, true))
  }
}

object BarkServer {
  def apply(numberOfWorkers: Int, description: String)(modules: BarkServerModules)(implicit system: ActorSystem) = {
    new BarkServer(numberOfWorkers, description)(new BarkRouter(modules))
  }
}