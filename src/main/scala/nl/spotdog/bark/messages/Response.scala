package nl.spotdog.bark.messages

import nl.spotdog.bark.data_format._
import BarkTypes._

import akka.util.{ByteIterator, ByteString, ByteStringBuilder}


trait Response

trait FailedResponse extends Response

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
   * AsyncReply: Send back to the client with the resulting value through a callback
   * 
   * (`asyncreply, (32), "sum-callback-23")
   * 
   * */
 // case class AsyncReply[T](value: T, callback: String) extends Response
  
  /* 
   * Error: Send back to the client in case of an error
   * 
   * Error types: protocol, server, user, and proxy (BERT-RPC style)
   * ('error, `server, 2, "UnknownFunction", "function 'collect' not found on module 'logs'", [""])
   * 
   * */
  case class Error(errorType: Symbol, errorCode: Int, errorClass: String, errorDetail: String, backtrace: List[String]) extends FailedResponse
  
  /* 
   * Error: Send back to the client in case of an error through a callback
   * 
   * ('error, `server, 2, "UnknownFunction", "function 'collect' not found on module 'logs'", [""], "collect-callback-2")
   * 
   * */
 // case class AsyncError(errorType: Symbol, errorCode: Int, errorClass: String, errorDetail: String, backtrace: List[String], callback: String) extends FailedResponse
}

object BarkResponseConverters extends ETFConverters with TupleConverters {
  import HeaderFunctions._

  implicit def replyConverter = new ETFConverter[Response.Reply] {
    def write(o: Response.Reply) = {
      val builder = new ByteStringBuilder
      builder.putByte(MAGIC)
      builder.putByte(SMALL_TUPLE)
      builder.putByte(2.toByte)

      builder ++= SymbolConverter.write('reply)
      builder ++= o.value
      builder.result
    }

    def readFromIterator(iter: ByteIterator): Response.Reply = {
      checkMagic(iter.getByte)
      checkSignature(SMALL_TUPLE, iter.getByte)
      val size = iter.getByte
      val v1 = SymbolConverter.readFromIterator(iter)
      Response.Reply(iter.toByteString)
    }
  }
  
  implicit def noReplyConverter = new ETFConverter[Response.NoReply] {
    def write(o: Response.NoReply) = {
      tuple1Converter[Symbol].write(Tuple1('noreply))
    }

    def readFromIterator(iter: ByteIterator): Response.NoReply = {
      val tpl = tuple1Converter[Symbol].readFromIterator(iter)
      Response.NoReply()
    }
  }

  implicit def errorConverter = new ETFConverter[Response.Error] {
    def write(o: Response.Error) = {
      tuple6Converter[Symbol, Symbol, Int, String, String, List[String]].write(('error, o.errorType, o.errorCode, o.errorClass, o.errorDetail, o.backtrace))
    }

    def readFromIterator(iter: ByteIterator): Response.Error = {
      val tpl = tuple6Converter[Symbol, Symbol, Int, String, String, List[String]].readFromIterator(iter)
      Response.Error(tpl._2, tpl._3, tpl._4, tpl._5, tpl._6)
    }
  }
}
