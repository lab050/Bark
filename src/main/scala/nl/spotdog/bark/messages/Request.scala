package nl.spotdog.bark.messages

import shapeless._
import nl.spotdog.bark.data_format.HeaderFunctions
import nl.spotdog.bark.data_format.ETFConverter
import akka.util.ByteStringBuilder
import nl.spotdog.bark.data_format.BarkTypes
import akka.util.ByteIterator

import nl.spotdog.bark.data_format._

trait Request {
  def module: Symbol
  def functionName: Symbol
  def arguments: Product
}

object Request {
  
  /* 
   * Call: Send a request to a server, waiting for a immediate (blocking) response, best used in CPU bound services. 
   * 
   * (`call, `persons, `fetch, ("gideondk")))
   * 
   */
  case class Call[T <: Product](module: Symbol, functionName: Symbol, arguments: T) extends Request
  
  /* 
   * Cast: Send a request to a server, waiting for a immediate reply but not a response (fire and forget)
   * 
   * (`cast, `persons, `remove, ("gideondk")))
   *   
   */
  case class Cast[T <: Product](module: Symbol, functionName: Symbol, arguments: T) extends Request
  
  /* 
   * AsyncCall: Send a request to a server, waiting for a immediate reply and sending the response later through a callback
   * 
   * (`asyncreply, `persons, `collect, ("group_ids", [1,2,3]), "collect-callback-2")
   *    
   */
 // case class AsyncCall[T <: Product](module: Symbol, functionName: Symbol, arguments: T, callback: String) extends Request
  
//  /* 
//   * React: Send a request to a server, creating a remote iteratee like process. 
//   */
//  case class React[Args <: HList](module: Symbol, functionName: Symbol, arguments: Args) extends Request[Args]
}

object BarkRequestConverters extends ETFConverters with TupleConverters {
  implicit def callConverter[T <: Product](implicit c1: ETFConverter[T]) = new ETFConverter[Request.Call[T]] {
    def write(o: Request.Call[T]) = {
      val callTpl = Request.Call.unapply(o).get
      tuple4Converter[Symbol, Symbol, Symbol, T].write('call, callTpl._1, callTpl._2, callTpl._3)
    }

    def readFromIterator(iter: ByteIterator): Request.Call[T] = {
      val tpl = tuple4Converter[Symbol, Symbol, Symbol, T].readFromIterator(iter)
      Request.Call(tpl._2, tpl._3, tpl._4)
    }
  }
  
  implicit def castConverter[T <: Product](implicit c1: ETFConverter[T]) = new ETFConverter[Request.Cast[T]] {
    def write(o: Request.Cast[T]) = {
      val callTpl = Request.Cast.unapply(o).get
      tuple4Converter[Symbol, Symbol, Symbol, T].write('cast, callTpl._1, callTpl._2, callTpl._3)
    }

    def readFromIterator(iter: ByteIterator): Request.Cast[T] = {
      val tpl = tuple4Converter[Symbol, Symbol, Symbol, T].readFromIterator(iter)
      Request.Cast(tpl._2, tpl._3, tpl._4)
    }
  }
}