package nl.spotdog.bark.server.builders

import akka.util.ByteString

import nl.spotdog.bark.protocol._
import nl.spotdog.bark.protocol.ETF._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import nl.spotdog.bark.server._

import scalaz._
import Scalaz._

import scala.util.Try

trait BarkCallBuilder {
  def name: Symbol

  def apply[R](f: Function0[R])(implicit writer: ETFWriter[R]) =
    BarkServerFunction.call(name)((bs: ByteString) ⇒ Try(writer.write(f())))

  def apply[T1, R](f: Function1[T1, R])(implicit reader: ETFReader[Tuple1[T1]], writer: ETFWriter[R]) =
    BarkServerFunction.call(name)((bs: ByteString) ⇒ Try(fromETF[Tuple1[T1]](bs).getOrElse(throw new Exception("Couldn't process arguments"))) flatMap { x ⇒
      Try(writer.write(f(x._1)))
    })

  def apply[T1, T2, R](f: Function2[T1, T2, R])(implicit reader: ETFReader[Tuple2[T1, T2]], writer: ETFWriter[R]) =
    BarkServerFunction.call(name)((bs: ByteString) ⇒ Try(fromETF[Tuple2[T1, T2]](bs).getOrElse(throw new Exception("Couldn't process arguments"))) flatMap { x ⇒
      Try(writer.write(f(x._1, x._2)))
    })

  def apply[T1, T2, T3, R](f: Function3[T1, T2, T3, R])(implicit reader: ETFReader[Tuple3[T1, T2, T3]], writer: ETFWriter[R]) =
    BarkServerFunction.call(name)((bs: ByteString) ⇒ Try(fromETF[Tuple3[T1, T2, T3]](bs).getOrElse(throw new Exception("Couldn't process arguments"))) flatMap { x ⇒
      Try(writer.write(f(x._1, x._2, x._3)))
    })

  def apply[T1, T2, T3, T4, R](f: Function4[T1, T2, T3, T4, R])(implicit reader: ETFReader[Tuple4[T1, T2, T3, T4]], writer: ETFWriter[R]) =
    BarkServerFunction.call(name)((bs: ByteString) ⇒ Try(fromETF[Tuple4[T1, T2, T3, T4]](bs).getOrElse(throw new Exception("Couldn't process arguments"))) flatMap { x ⇒
      Try(writer.write(f(x._1, x._2, x._3, x._4)))
    })

  def apply[T1, T2, T3, T4, T5, R](f: Function5[T1, T2, T3, T4, T5, R])(implicit reader: ETFReader[Tuple5[T1, T2, T3, T4, T5]], writer: ETFWriter[R]) =
    BarkServerFunction.call(name)((bs: ByteString) ⇒ Try(fromETF[Tuple5[T1, T2, T3, T4, T5]](bs).getOrElse(throw new Exception("Couldn't process arguments"))) flatMap { x ⇒
      Try(writer.write(f(x._1, x._2, x._3, x._4, x._5)))
    })

  def apply[T1, T2, T3, T4, T5, T6, R](f: Function6[T1, T2, T3, T4, T5, T6, R])(implicit reader: ETFReader[Tuple6[T1, T2, T3, T4, T5, T6]], writer: ETFWriter[R]) =
    BarkServerFunction.call(name)((bs: ByteString) ⇒ Try(fromETF[Tuple6[T1, T2, T3, T4, T5, T6]](bs).getOrElse(throw new Exception("Couldn't process arguments"))) flatMap { x ⇒
      Try(writer.write(f(x._1, x._2, x._3, x._4, x._5, x._6)))
    })

  def apply[T1, T2, T3, T4, T5, T6, T7, R](f: Function7[T1, T2, T3, T4, T5, T6, T7, R])(implicit reader: ETFReader[Tuple7[T1, T2, T3, T4, T5, T6, T7]], writer: ETFWriter[R]) =
    BarkServerFunction.call(name)((bs: ByteString) ⇒ Try(fromETF[Tuple7[T1, T2, T3, T4, T5, T6, T7]](bs).getOrElse(throw new Exception("Couldn't process arguments"))) flatMap { x ⇒
      Try(writer.write(f(x._1, x._2, x._3, x._4, x._5, x._6, x._7)))
    })

  def apply[T1, T2, T3, T4, T5, T6, T7, T8, R](f: Function8[T1, T2, T3, T4, T5, T6, T7, T8, R])(implicit reader: ETFReader[Tuple8[T1, T2, T3, T4, T5, T6, T7, T8]], writer: ETFWriter[R]) =
    BarkServerFunction.call(name)((bs: ByteString) ⇒ Try(fromETF[Tuple8[T1, T2, T3, T4, T5, T6, T7, T8]](bs).getOrElse(throw new Exception("Couldn't process arguments"))) flatMap { x ⇒
      Try(writer.write(f(x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8)))
    })

  def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, R](f: Function9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R])(implicit reader: ETFReader[Tuple9[T1, T2, T3, T4, T5, T6, T7, T8, T9]], writer: ETFWriter[R]) =
    BarkServerFunction.call(name)((bs: ByteString) ⇒ Try(fromETF[Tuple9[T1, T2, T3, T4, T5, T6, T7, T8, T9]](bs).getOrElse(throw new Exception("Couldn't process arguments"))) flatMap { x ⇒
      Try(writer.write(f(x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, x._9)))
    })

  def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R](f: Function10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R])(implicit reader: ETFReader[Tuple10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10]], writer: ETFWriter[R]) =
    BarkServerFunction.call(name)((bs: ByteString) ⇒ Try(fromETF[Tuple10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10]](bs).getOrElse(throw new Exception("Couldn't process arguments"))) flatMap { x ⇒
      Try(writer.write(f(x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, x._9, x._10)))
    })

  def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R](f: Function11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R])(implicit reader: ETFReader[Tuple11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11]], writer: ETFWriter[R]) =
    BarkServerFunction.call(name)((bs: ByteString) ⇒ Try(fromETF[Tuple11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11]](bs).getOrElse(throw new Exception("Couldn't process arguments"))) flatMap { x ⇒
      Try(writer.write(f(x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, x._9, x._10, x._11)))
    })

  def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R](f: Function12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R])(implicit reader: ETFReader[Tuple12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12]], writer: ETFWriter[R]) =
    BarkServerFunction.call(name)((bs: ByteString) ⇒ Try(fromETF[Tuple12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12]](bs).getOrElse(throw new Exception("Couldn't process arguments"))) flatMap { x ⇒
      Try(writer.write(f(x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, x._9, x._10, x._11, x._12)))
    })

  def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R](f: Function13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R])(implicit reader: ETFReader[Tuple13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13]], writer: ETFWriter[R]) =
    BarkServerFunction.call(name)((bs: ByteString) ⇒ Try(fromETF[Tuple13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13]](bs).getOrElse(throw new Exception("Couldn't process arguments"))) flatMap { x ⇒
      Try(writer.write(f(x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, x._9, x._10, x._11, x._12, x._13)))
    })

  def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R](f: Function14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R])(implicit reader: ETFReader[Tuple14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14]], writer: ETFWriter[R]) =
    BarkServerFunction.call(name)((bs: ByteString) ⇒ Try(fromETF[Tuple14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14]](bs).getOrElse(throw new Exception("Couldn't process arguments"))) flatMap { x ⇒
      Try(writer.write(f(x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, x._9, x._10, x._11, x._12, x._13, x._14)))
    })

  def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R](f: Function15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R])(implicit reader: ETFReader[Tuple15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15]], writer: ETFWriter[R]) =
    BarkServerFunction.call(name)((bs: ByteString) ⇒ Try(fromETF[Tuple15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15]](bs).getOrElse(throw new Exception("Couldn't process arguments"))) flatMap { x ⇒
      Try(writer.write(f(x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, x._9, x._10, x._11, x._12, x._13, x._14, x._15)))
    })

  def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R](f: Function16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R])(implicit reader: ETFReader[Tuple16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16]], writer: ETFWriter[R]) =
    BarkServerFunction.call(name)((bs: ByteString) ⇒ Try(fromETF[Tuple16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16]](bs).getOrElse(throw new Exception("Couldn't process arguments"))) flatMap { x ⇒
      Try(writer.write(f(x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, x._9, x._10, x._11, x._12, x._13, x._14, x._15, x._16)))
    })

  def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R](f: Function17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R])(implicit reader: ETFReader[Tuple17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17]], writer: ETFWriter[R]) =
    BarkServerFunction.call(name)((bs: ByteString) ⇒ Try(fromETF[Tuple17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17]](bs).getOrElse(throw new Exception("Couldn't process arguments"))) flatMap { x ⇒
      Try(writer.write(f(x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, x._9, x._10, x._11, x._12, x._13, x._14, x._15, x._16, x._17)))
    })

  def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R](f: Function18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R])(implicit reader: ETFReader[Tuple18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18]], writer: ETFWriter[R]) =
    BarkServerFunction.call(name)((bs: ByteString) ⇒ Try(fromETF[Tuple18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18]](bs).getOrElse(throw new Exception("Couldn't process arguments"))) flatMap { x ⇒
      Try(writer.write(f(x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, x._9, x._10, x._11, x._12, x._13, x._14, x._15, x._16, x._17, x._18)))
    })

  def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R](f: Function19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R])(implicit reader: ETFReader[Tuple19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19]], writer: ETFWriter[R]) =
    BarkServerFunction.call(name)((bs: ByteString) ⇒ Try(fromETF[Tuple19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19]](bs).getOrElse(throw new Exception("Couldn't process arguments"))) flatMap { x ⇒
      Try(writer.write(f(x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, x._9, x._10, x._11, x._12, x._13, x._14, x._15, x._16, x._17, x._18, x._19)))
    })

  def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R](f: Function20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R])(implicit reader: ETFReader[Tuple20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20]], writer: ETFWriter[R]) =
    BarkServerFunction.call(name)((bs: ByteString) ⇒ Try(fromETF[Tuple20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20]](bs).getOrElse(throw new Exception("Couldn't process arguments"))) flatMap { x ⇒
      Try(writer.write(f(x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, x._9, x._10, x._11, x._12, x._13, x._14, x._15, x._16, x._17, x._18, x._19, x._20)))
    })

  def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R](f: Function21[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R])(implicit reader: ETFReader[Tuple21[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21]], writer: ETFWriter[R]) =
    BarkServerFunction.call(name)((bs: ByteString) ⇒ Try(fromETF[Tuple21[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21]](bs).getOrElse(throw new Exception("Couldn't process arguments"))) flatMap { x ⇒
      Try(writer.write(f(x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, x._9, x._10, x._11, x._12, x._13, x._14, x._15, x._16, x._17, x._18, x._19, x._20, x._21)))
    })

  def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R](f: Function22[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R])(implicit reader: ETFReader[Tuple22[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22]], writer: ETFWriter[R]) =
    BarkServerFunction.call(name)((bs: ByteString) ⇒ Try(fromETF[Tuple22[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22]](bs).getOrElse(throw new Exception("Couldn't process arguments"))) flatMap { x ⇒
      Try(writer.write(f(x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, x._9, x._10, x._11, x._12, x._13, x._14, x._15, x._16, x._17, x._18, x._19, x._20, x._21, x._22)))
    })

}