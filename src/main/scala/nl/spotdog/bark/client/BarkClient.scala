package nl.spotdog.bark.client

import akka.actor.{ Props, ActorSystem }

import akka.actor.ActorRef
import akka.util.ByteString
import concurrent.Promise
import scalaz._
import Scalaz._
import effect._

import scala.util.Try

import akka.io._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import nl.spotdog.bark.protocol._
import nl.spotdog.bark.protocol.BarkMessaging._

import nl.gideondk.sentinel._
import client._

import shapeless._
import TypeOperators._

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
  def as[T](implicit reader: ETFReader[T]) = ETF.fromETF[T](rawResult)
}

class BarkClientFunction(client: BarkClient, module: String, functionName: String) {
  private def handleResponse(bs: ByteString) = {
    Try(replyConverter.read(bs)) match {
      case scala.util.Success(s) ⇒ s.point[Task].map(x ⇒ BarkClientResult(x.value))
      case scala.util.Failure(e) ⇒ {
        val error = errorConverter.read(bs)
        Task(Future(throw new Exception(error.errorDetail))) // TODO: handle specific errors into specific throwables
      }
    }
  }

  def call(): Task[BarkClientResult] = {
    val req = Request.ArgumentLessCall(Atom(module), Atom(functionName))
    val cmd = argumentLessCallConverter.write(req)
    (client sendCommand cmd) flatMap handleResponse
  }

  def call[T](args: T)(implicit nst: T <:!< Product, tW: ETFConverter[T]): Task[BarkClientResult] =
    call(Tuple1(args))

  def call[T <: Product](args: T)(implicit tW: ETFConverter[T]): Task[BarkClientResult] = {
    val req = Request.Call(Atom(module), Atom(functionName), args)
    val cmd = callConverter(tW).write(req)
    (client sendCommand cmd) flatMap handleResponse
  }

  def cast[T](args: T)(implicit nst: T <:!< Product, tW: ETFConverter[T]): Task[Unit] =
    cast(Tuple1(args))

  def cast[T <: Product](args: T)(implicit tW: ETFConverter[T]): Task[Unit] = {
    val req = Request.Cast(Atom(module), Atom(functionName), args)
    val cmd = castConverter(tW).write(req)
    (client sendCommand cmd).flatMap { x ⇒
      Try(noReplyConverter.read(x)) match {
        case scala.util.Success(s) ⇒ s.point[Task].map(_ ⇒ ())
        case scala.util.Failure(e) ⇒ {
          val error = errorConverter.read(x)
          Task(Future(throw new Exception(error.errorDetail))) // TODO: handle specific errors into specific throwables
        }
      }
    }
  }

  def <<?(): Task[BarkClientResult] = call()

  def <<?[T <: Product](args: T)(implicit tW: ETFConverter[T]): Task[BarkClientResult] = call(args)

  def <<?[T](args: T)(implicit nst: T <:!< Product, tW: ETFConverter[T]): Task[BarkClientResult] = call(args)

  def <<![T <: Product](args: T)(implicit tW: ETFConverter[T]): Task[Unit] = cast(args)

  def <<![T](args: T)(implicit nst: T <:!< Product, tW: ETFConverter[T]): Task[Unit] = cast(args)
}

class BarkClientModule(client: BarkClient, name: String) {
  def |/|(functionName: String) = new BarkClientFunction(client, name, functionName)
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

  def module(moduleName: String) = new BarkClientModule(this, moduleName)

  def |?|(moduleName: String) = module(moduleName)
}

object BarkClient {
  def apply(host: String, port: Int, numberOfWorkers: Int, description: String)(implicit system: ActorSystem) =
    new BarkClient(host, port, numberOfWorkers, description)
}
