package nl.spotdog.bark.protocol

import shapeless._

import akka.util.{ ByteIterator, ByteStringBuilder, ByteString }

import nl.spotdog.bark.protocol._
import nl.spotdog.bark.protocol.ETFTypes._

import akka.io.PipelineContext

trait ArgumentLessRequest {
  def module: Atom
  def functionName: Atom
}

trait Request extends ArgumentLessRequest {
  def module: Atom
  def functionName: Atom
}

trait Response

trait FailedResponse extends Response

object Request {
  /* 
   * Argument less call: Send a request to a server, waiting for a immediate (blocking) response, best used in CPU bound services. 
   * 
   * (`call, `generation, `generateId))
   * 
   */
  case class ArgumentLessCall(module: Atom, functionName: Atom) extends ArgumentLessRequest

  /* 
   * Call: Send a request to a server, waiting for a immediate (blocking) response, best used in CPU bound services. 
   * 
   * (`call, `persons, `fetch, ("gideondk")))
   * 
   */
  case class Call[T <: Product](module: Atom, functionName: Atom, arguments: T) extends Request

  /* 
   * Cast: Send a request to a server, waiting for a immediate reply but not a response (fire and forget)
   * 
   * (`cast, `persons, `remove, ("gideondk")))
   *   
   */
  case class Cast[T <: Product](module: Atom, functionName: Atom, arguments: T) extends Request
}

object Response {
  /* 
   * Reply: Send back to the client with the resulting value
   * 
   * (`reply, ("Gideon", "de Kok"))
   * 
   * */
  case class Reply(value: ByteString) extends Response

  /* 
   * NoReply: Send back to the client in case of a "cast" request
   * 
   * (`noreply)
   * 
   * */
  case class NoReply() extends Response

  /* 
   * Error: Send back to the client in case of an error
   * 
   * Error types: protocol, server, user, and proxy (BERT-RPC style)
   * ('error, (`server, 2, "UnknownFunction", "function 'collect' not found on module 'logs'", [""]))
   * 
   * */
  case class Error(errorType: Atom, errorCode: Int, errorClass: String, errorDetail: String, backtrace: List[String]) extends FailedResponse
}

object BarkMessaging extends ETFConverters with TupleConverters {
  import HeaderFunctions._

  trait HasByteOrder extends PipelineContext {
    def byteOrder: java.nio.ByteOrder
  }

  /* Request */
  implicit def callConverter[T <: Product](implicit c1: ETFConverter[T]) = new ETFConverter[Request.Call[T]] {
    def write(o: Request.Call[T]) = {
      val callTpl = Request.Call.unapply(o).get
      tuple4Converter[Atom, Atom, Atom, T].write(Atom("call"), callTpl._1, callTpl._2, callTpl._3)
    }

    def readFromIterator(iter: ByteIterator): Request.Call[T] = {
      val tpl = tuple4Converter[Atom, Atom, Atom, T].readFromIterator(iter)
      Request.Call(tpl._2, tpl._3, tpl._4)
    }
  }

  implicit def argumentLessCallConverter = new ETFConverter[Request.ArgumentLessCall] {
    def write(o: Request.ArgumentLessCall) = {
      val callTpl = Request.ArgumentLessCall.unapply(o).get
      tuple3Converter[Atom, Atom, Atom].write(Atom("call"), callTpl._1, callTpl._2)
    }

    def readFromIterator(iter: ByteIterator): Request.ArgumentLessCall = {
      val tpl = tuple3Converter[Atom, Atom, Atom].readFromIterator(iter)
      Request.ArgumentLessCall(tpl._2, tpl._3)
    }
  }

  implicit def castConverter[T <: Product](implicit c1: ETFConverter[T]) = new ETFConverter[Request.Cast[T]] {
    def write(o: Request.Cast[T]) = {
      val callTpl = Request.Cast.unapply(o).get
      tuple4Converter[Atom, Atom, Atom, T].write(Atom("cast"), callTpl._1, callTpl._2, callTpl._3)
    }

    def readFromIterator(iter: ByteIterator): Request.Cast[T] = {
      val tpl = tuple4Converter[Atom, Atom, Atom, T].readFromIterator(iter)
      Request.Cast(tpl._2, tpl._3, tpl._4)
    }
  }

  /* Response */

  implicit def replyConverter = new ETFConverter[Response.Reply] {
    def write(o: Response.Reply) = {
      val builder = new ByteStringBuilder
      builder.putByte(SMALL_TUPLE)
      builder.putByte(2.toByte)

      builder ++= AtomConverter.write(Atom("reply"))
      builder ++= o.value
      builder.result
    }

    def readFromIterator(iter: ByteIterator): Response.Reply = {
      checkSignature(SMALL_TUPLE, iter.getByte)
      val size = iter.getByte
      val v1 = AtomConverter.readFromIterator(iter)
      if (v1 != Atom("reply")) throw new Exception("Response is not of the 'reply' type")
      Response.Reply(iter.toByteString)
    }
  }

  implicit def noReplyConverter = new ETFConverter[Response.NoReply] {
    def write(o: Response.NoReply) = {
      tuple1Converter[Atom].write(Tuple1(Atom("noreply")))
    }

    def readFromIterator(iter: ByteIterator): Response.NoReply = {
      val tpl = tuple1Converter[Atom].readFromIterator(iter)
      if (tpl._1 != Atom("noreply")) throw new Exception("Response is not of the 'noreply' type")
      Response.NoReply()
    }
  }

  implicit def errorConverter = new ETFConverter[Response.Error] {
    def write(o: Response.Error) = {
      tuple2Converter[Atom, Tuple5[Atom, Int, String, String, List[String]]].write((Atom("error"), (o.errorType, o.errorCode, o.errorClass, o.errorDetail, o.backtrace)))
    }

    def readFromIterator(iter: ByteIterator): Response.Error = {
      val tpl = tuple2Converter[Atom, (Atom, Int, String, String, List[String])].readFromIterator(iter)
      Response.Error(tpl._2._1, tpl._2._2, tpl._2._3, tpl._2._4, tpl._2._5)
    }
  }
}