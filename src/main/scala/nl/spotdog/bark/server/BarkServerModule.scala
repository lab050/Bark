package nl.spotdog.bark.server

import nl.spotdog.bark.protocol._
import shapeless._
import TypeOperators._
import LUBConstraint._
import HList._

trait BarkServerModule {
  def name: Atom

  def funcs: BarkServerFunctionHolder
}

case class BarkServerModules(modules: Map[Atom, BarkServerModule]) {
  def ~(module: BarkServerModule) = BarkServerModules(modules + (module.name -> module))
}

case class BarkServerFunctionHolder(calls: Map[Atom, BarkServerCall], casts: Map[Atom, BarkServerCast])

object BarkServerModule {
  implicit def barkModuletoETFModules(m: BarkServerModule): BarkServerModules = BarkServerModules(Map(m.name -> m))

  def module[T <: HList: <<:[BarkServerFunction]#λ, A <: HList, B <: HList](n: String)(fs: BarkServerFunctions[T])(implicit callFilter: FilterAux[T, BarkServerCall, A],
                                                                                                                   castFilter: FilterAux[T, BarkServerCast, B],
                                                                                                                   tl: ToList[A, BarkServerCall],
                                                                                                                   tl2: ToList[B, BarkServerCast]) =
    new BarkServerModule {
      val name = Atom(n)
      val funcs = {
        val calls = fs.functions.filter[BarkServerCall]
        val casts = fs.functions.filter[BarkServerCast]
        BarkServerFunctionHolder(calls.toList.map(x ⇒ x.name -> x).toMap, casts.toList.map(x ⇒ x.name -> x).toMap)
      }
    }
}