package nl.spotdog.bark.server

import shapeless._
import TypeOperators._
import LUBConstraint._
import HList._

trait BarkServerModule {
  def name: Symbol

  def funcs: BarkServerFunctionHolder
}

case class BarkServerModules(modules: Map[Symbol, BarkServerModule]) {
  def ~(module: BarkServerModule) = BarkServerModules(modules + (module.name -> module))
}

case class BarkServerFunctionHolder(calls: Map[Symbol, BarkServerCall], casts: Map[Symbol, BarkServerCast])

object BarkServerModule {
  implicit def barkModuleToBarkModules(m: BarkServerModule): BarkServerModules = BarkServerModules(Map(m.name -> m))

  def module[T <: HList: <<:[BarkServerFunction]#λ, A <: HList, B <: HList](n: Symbol)(fs: BarkServerFunctions[T])(implicit callFilter: FilterAux[T, BarkServerCall, A],
                                                                                                                   castFilter: FilterAux[T, BarkServerCast, B],
                                                                                                                   tl: ToList[A, BarkServerCall],
                                                                                                                   tl2: ToList[B, BarkServerCast]) =
    new BarkServerModule {
      val name = n
      val funcs = {
        val calls = fs.functions.filter[BarkServerCall]
        val casts = fs.functions.filter[BarkServerCast]
        BarkServerFunctionHolder(calls.toList.map(x ⇒ x.name -> x).toMap, casts.toList.map(x ⇒ x.name -> x).toMap)
      }
    }
}

object Test extends BarkRouting {
  import BarkServerModule._
  import nl.spotdog.bark.data_format._
  import Bark._

  val a = module('persons) {
    call('test)((a: Int, b: Int) ⇒ a + b) ~
      cast('blaat)((s: String) ⇒ s.length)
  } ~ module('other) {
    cast('yes)(() ⇒ "NO")
  }
}