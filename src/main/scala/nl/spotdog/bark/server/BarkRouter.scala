package nl.spotdog.bark.server

import akka.actor.{ Props, ActorSystem }

import nl.spotdog.bark.protocol._
import BarkMessaging._

import akka.util._

import akka.actor.ActorRef
import builders._
import scala.util.Try
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import scalaz._
import Scalaz._

import nl.spotdog.bark.protocol._
import ETF._

class BarkRouter(modules: BarkServerModules) {
  import ETF._
  type BarkServerValidation[T] = Validation[Response.Error, T]

  def checkHeader(iter: ByteIterator): BarkServerValidation[Unit] = {
    try {
      HeaderFunctions.checkMagic(iter.getByte)
      HeaderFunctions.checkSignature(ETFTypes.SMALL_TUPLE, iter.getByte)
      Success(())
    } catch {
      case e: Throwable ⇒ Failure(Response.Error('protocol, 1, "ParseError", e.getMessage, List[String]()))
    }
  }

  def getSizeTypeModuleAndFunction(iter: ByteIterator): BarkServerValidation[(Int, Symbol, Symbol, Symbol)] = {
    val size = iter.getByte
    if (size != 4) Failure(Response.Error('protocol, 1, "ParseError", "Incorrect request message", List[String]()))
    else {
      val tpl = for {
        callType ← Try(SymbolConverter.readFromIterator(iter))
        module ← Try(SymbolConverter.readFromIterator(iter))
        function ← Try(SymbolConverter.readFromIterator(iter))
      } yield (size.toInt, callType, module, function)

      tpl match {
        case scala.util.Success(s) ⇒ Success(s)
        case scala.util.Failure(f) ⇒ Failure(Response.Error('protocol, 1, "ParseError", "Incorrect request message", List[String](f.getMessage)))
      }
    }
  }

  def handleCall(module: Symbol, functionName: Symbol, arguments: ByteString) = for {
    module ← modules.modules.get(module).toSuccess(Response.Error('server, 1, "ModuleError", "Unknown module", List[String]()))
    function ← module.funcs.calls.get(functionName).toSuccess(Response.Error('server, 2, "FunctionError", "Unknown function", List[String]()))
    res ← function.function(arguments) match {
      case scala.util.Success(s) ⇒ Success(Future(replyConverter.write(Response.Reply(s))))
      case scala.util.Failure(e) ⇒ Failure(Response.Error('server, 0, "RuntimeError", e.getMessage, List[String]())) // Should extend the stacktrace into the error
    }
  } yield res

  def handleCast(module: Symbol, functionName: Symbol, arguments: ByteString) = for {
    module ← modules.modules.get(module).toSuccess(Response.Error('server, 1, "ModuleError", "Unknown module", List[String]()))
    function ← module.funcs.casts.get(functionName).toSuccess(Response.Error('server, 2, "FunctionError", "Unknown function", List[String]()))
    res ← function.function(arguments) match {
      case scala.util.Success(s) ⇒ Success(Future(noReplyConverter.write(Response.NoReply())))
      case scala.util.Failure(e) ⇒ Failure(Response.Error('server, 0, "RuntimeError", e.getMessage, List[String]())) // Should extend the stacktrace into the error
    }
  } yield res

  def handle(bs: ByteString): Future[ByteString] = {
    val iter = bs.iterator

    val value = for {
      _ ← checkHeader(iter)
      tpl ← getSizeTypeModuleAndFunction(iter)
      (size, callType, module, function) = tpl
      payload = iter.toByteString
      res ← callType match {
        case 'call ⇒ handleCall(module, function, payload)
        case 'cast ⇒ handleCast(module, function, payload)
      }
    } yield res

    value match {
      case Success(s) ⇒ s
      case Failure(e) ⇒ Future(errorConverter.write(e))
    }
  }
}

trait BarkRouting {
  def call(n: Symbol) = new BarkCallBuilder {
    val name = n
  }

  def cast(n: Symbol) = new BarkCastBuilder {
    val name = n
  }
}