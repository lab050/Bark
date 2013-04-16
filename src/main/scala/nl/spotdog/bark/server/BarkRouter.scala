package nl.spotdog.bark.server

import akka.actor.{ Props, ActorSystem }

import nl.spotdog.bark.protocol._
import BarkMessaging._

import akka.util._

import akka.actor.ActorRef

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import scalaz._
import Scalaz._

import nl.spotdog.bark.protocol._
import ETF._

import scala.util.Try

import shapeless._
import Functions._
import Tuples._

class BarkRouter(modules: BarkServerModules) {
  import ETF._

  type BarkServerValidation[T] = Validation[Response.Error, T]

  def checkHeader(iter: ByteIterator): BarkServerValidation[Unit] = {
    try {
      HeaderFunctions.checkMagic(iter.getByte)
      HeaderFunctions.checkSignature(ETFTypes.SMALL_TUPLE, iter.getByte)
      Success(())
    } catch {
      case e: Throwable ⇒ Failure(Response.Error(Atom("protocol"), 1, "ParseError", e.getMessage, List[String]()))
    }
  }

  def getSizeTypeModuleAndFunction(iter: ByteIterator): BarkServerValidation[(Int, Atom, Atom, Atom)] = {
    val size = iter.getByte
    if (size < 3 || size > 4) Failure(Response.Error(Atom("protocol"), 1, "ParseError", "Incorrect request message", List[String]()))
    else {
      val tpl = for {
        callType ← Try(AtomConverter.readFromIterator(iter))
        module ← Try(AtomConverter.readFromIterator(iter))
        function ← Try(AtomConverter.readFromIterator(iter))
      } yield (size.toInt, callType, module, function)

      tpl match {
        case scala.util.Success(s) ⇒ Success(s)
        case scala.util.Failure(f) ⇒ Failure(Response.Error(Atom("protocol"), 1, "ParseError", "Incorrect request message", List[String](f.getMessage)))
      }
    }
  }

  def handleCall(module: Atom, functionName: Atom, arguments: ByteString) = for {
    module ← modules.modules.get(module).toSuccess(Response.Error(Atom("server"), 1, "ModuleError", "Unknown module", List[String]()))
    function ← module.funcs.calls.get(functionName).toSuccess(Response.Error(Atom("server"), 2, "FunctionError", "Unknown function", List[String]()))
    res ← Try(function.function(arguments).map(x ⇒ replyConverter.write(Response.Reply(x)))) match {
      case scala.util.Success(s) ⇒ Success(s)
      case scala.util.Failure(f) ⇒ Failure(Response.Error(Atom("server"), 0, "RuntimeError", f.getMessage, List[String]()))
    }
  } yield res

  def handleCast(module: Atom, functionName: Atom, arguments: ByteString) = for {
    module ← modules.modules.get(module).toSuccess(Response.Error(Atom("server"), 1, "ModuleError", "Unknown module", List[String]()))
    function ← module.funcs.casts.get(functionName).toSuccess(Response.Error(Atom("server"), 2, "FunctionError", "Unknown function", List[String]()))
    res ← {
      function.function(arguments)
      Success[Response.Error, Future[ByteString]](Future(noReplyConverter.write(Response.NoReply())))
    }
  } yield res

  def handle(bs: ByteString): Future[ByteString] = {
    val iter = bs.iterator

    val value = for {
      _ ← checkHeader(iter)
      tpl ← getSizeTypeModuleAndFunction(iter)
      (size, callType, module, function) = tpl
      res ← callType match {
        case Atom("call") ⇒ handleCall(module, function, iter.toByteString)
        case Atom("cast") ⇒ handleCast(module, function, iter.toByteString)
      }
    } yield res

    value match {
      case Success(s) ⇒ s.recover {
        case e: Exception ⇒ errorConverter.write(Response.Error(Atom("server"), 0, "RuntimeError", e.getMessage, List[String]()))
      }
      case Failure(e) ⇒ Future(errorConverter.write(e))
    }
  }
}

import TypeOperators._

trait BarkCallBuilder {
  def name: Atom

  def apply[R](f: Function0[Future[R]])(implicit writer: ETFWriter[R]) =
    BarkServerFunction.call(name)((bs: ByteString) ⇒ f().map(writer.write(_)))

  def apply[R, F, FO, A <: HList, P <: Product](f: F)(implicit h: FnHListerAux[F, FO], ev: FO <:< (A ⇒ Future[R]), tplr: TuplerAux[A, P],
                                                      hl: HListerAux[P, A], reader: ETFReader[P], writer: ETFWriter[R]) =
    BarkServerFunction.call(name)((args: ByteString) ⇒ f.hlisted(reader.read(args).hlisted).map(writer.write(_)))

}

trait BarkCastBuilder {
  def name: Atom

  def apply[R](f: Function0[Future[R]])(implicit writer: ETFWriter[R]) =
    BarkServerFunction.cast(name)((bs: ByteString) ⇒ f().map(writer.write(_)))

  def apply[R, F, FO, A <: HList, P <: Product](f: F)(implicit h: FnHListerAux[F, FO], ev: FO <:< (A ⇒ Unit), tplr: TuplerAux[A, P],
                                                      hl: HListerAux[P, A], reader: ETFReader[P]) =
    BarkServerFunction.cast(name)((args: ByteString) ⇒ Future(f.hlisted(reader.read(args).hlisted)))
}

trait BarkRouting {
  def call(n: String) = new BarkCallBuilder {
    val name = Atom(n)
  }

  def cast(n: String) = new BarkCastBuilder {
    val name = Atom(n)
  }
}
