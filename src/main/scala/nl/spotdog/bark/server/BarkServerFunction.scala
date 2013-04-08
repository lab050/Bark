package nl.spotdog.bark.server

import akka.util.ByteString
import scala.util.Try


import shapeless._
import TypeOperators._
import LUBConstraint._
import HList._

case class BarkServerFunctions[T <: HList](functions: T) {
  def ~(function: BarkServerCall)(implicit prepend : Prepend[T, BarkServerCall :: HNil]) = 
    BarkServerFunctions(functions :+ function)
    
   def ~(function: BarkServerCast)(implicit prepend : Prepend[T, BarkServerCast :: HNil]) = 
    BarkServerFunctions(functions :+ function)
}

private object BarkServerFunction {
  implicit def barkCallFunctionToBarkFunctions(m: BarkServerCall): BarkServerFunctions[BarkServerCall :: HNil] = BarkServerFunctions(m :: HNil)
  implicit def barkCastFunctionToBarkFunctions(m: BarkServerCast): BarkServerFunctions[BarkServerCast :: HNil] = BarkServerFunctions(m :: HNil)

  def call(n: Symbol)(f: ByteString ⇒ Try[ByteString]) = BarkServerCall(n, f)

  def cast(n: Symbol)(f: ByteString ⇒ Try[Unit]) = BarkServerCast(n, f)
}

trait BarkServerFunction {
  def name: Symbol

  def function: ByteString ⇒ Try[_]
}

case class BarkServerCall(name: Symbol, function: ByteString ⇒ Try[ByteString]) extends BarkServerFunction 

case class BarkServerCast(name: Symbol, function: ByteString ⇒ Try[Unit]) extends BarkServerFunction

