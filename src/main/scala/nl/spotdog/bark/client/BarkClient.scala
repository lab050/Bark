package nl.spotdog.bark.client

import akka.actor.{ Props, ActorSystem }

import nl.spotdog.bark.actors._
import akka.actor.ActorRef
import akka.util.ByteString
import concurrent.Promise
import scalaz._
import Scalaz._
import effect._

import akka.io._
import nl.spotdog.bark.data_format._
import nl.spotdog.bark.messages._

import nl.gideondk.sentinel._
import client._

trait BarkClientConfig {
  def host: String
  def port: Int
  def workers: Int
}

object BarkClientConfig {
  def apply(serverHost: String, serverPort: Int, workerCount: Int) = new BarkClientConfig {
    val host = serverHost
    val port = serverPort
    val workers = workerCount
  }
}

case class BarkClientResult(rawResult: ByteString) {
  def as[T](implicit reader: ETFReader[T]) = Bark.fromBark[T](rawResult)
}

class BarkClientFunction(client: BarkClient, module: Symbol, functionName: Symbol) {
  def call[T <: Product](args: T)(implicit tW: ETFConverter[T]) = {
    val req = Request.Call(module, functionName, args)
    val cmd = BarkRequestConverters.callConverter.write(req)
    (client sendCommand cmd).map(BarkClientResult(_))
  }

  def cast[T <: Product](args: T)(implicit tW: ETFConverter[T]) = {
    val req = Request.Cast(module, functionName, args)
    val cmd = BarkRequestConverters.castConverter.write(req)
    (client sendCommand cmd).map(BarkClientResult(_))
  }

  def <<?[T <: Product](args: T)(implicit tW: ETFConverter[T]) = call(args)
  def <<![T <: Product](args: T)(implicit tW: ETFConverter[T]) = cast(args)
}

class BarkClientModule(client: BarkClient, name: Symbol) {
  def |/|(functionName: Symbol) = new BarkClientFunction(client, name, functionName)
}

class BarkClient(host: String, port: Int, numberOfWorkers: Int, description: String)(implicit system: ActorSystem) {
  def ctx = new HasByteOrder {
    def byteOrder = java.nio.ByteOrder.BIG_ENDIAN
  }

  val stages = new LengthFieldFrame(1024 * 1024 * 50) // Max 50MB messages 

  val actor = SentinelClient.randomRouting(host, port, numberOfWorkers, description)(ctx, stages, true)

  def close = {
    system stop actor
  }

  def sendCommand(cmd: ByteString) = actor <~< cmd

  def module(moduleName: Symbol) = new BarkClientModule(this, moduleName)

  def |?|(moduleName: Symbol) = module(moduleName)
}
