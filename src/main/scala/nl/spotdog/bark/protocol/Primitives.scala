package nl.spotdog.bark.protocol

import akka.util.{ ByteStringBuilder, ByteString, ByteIterator }
import ETFTypes._
import java.util.Date
import scala.util.Try
import shapeless._
import HList._
import Tuples._
import java.util.Locale

case class Atom(v: String)

object HeaderFunctions {
  def checkMagic(b: Byte) = {
    b match {
      case MAGIC ⇒ ()
      case _     ⇒ throw new Exception("Bad Magic")
    }
  }

  def checkSignature(expected: Byte, b: Byte) =
    if (b != expected) {
      throw new Exception("Unexpected signature: '" + (b & 0xFF) + "', expected: " + (expected & 0xFF))
    }
}

trait ETFConverters {
  import HeaderFunctions._

  implicit val byteOrder: java.nio.ByteOrder = java.nio.ByteOrder.BIG_ENDIAN

  implicit object IntConverter extends ETFConverter[Int] {
    def write(o: Int) = {
      val builder = new ByteStringBuilder

      builder.putByte(ETFTypes.INT)
      builder.putInt(o).result
    }

    def readFromIterator(iter: ByteIterator): Int = {

      checkSignature(ETFTypes.INT, iter.getByte)
      iter.getInt(byteOrder)
    }
  }

  implicit object DoubleConverter extends ETFConverter[Double] {
    def write(o: Double) = {
      val bytes = String.format(Locale.US, "%15.15e", new java.lang.Double(o)).getBytes
      val padded = bytes ++ Stream.continually(0.toByte).take(ETFTypes.FLOAT_LENGTH - bytes.length)

      val builder = new ByteStringBuilder

      builder.putByte(ETFTypes.FLOAT)
      builder.putBytes(padded)

      builder.result
    }

    def readFromIterator(iter: ByteIterator): Double = {

      checkSignature(ETFTypes.FLOAT, iter.getByte)
      new java.lang.Double(new String(iter.take(ETFTypes.FLOAT_LENGTH).toArray)).doubleValue
    }
  }

  implicit object ByteConverter extends ETFConverter[Byte] {
    def write(o: Byte) = {
      val builder = new ByteStringBuilder

      builder.putByte(ETFTypes.SMALL_INT)
      builder.putByte(o)
      builder.result
    }

    def readFromIterator(iter: ByteIterator): Byte = {

      checkSignature(ETFTypes.SMALL_INT, iter.getByte)
      iter.getByte
    }
  }

  implicit object BigIntConverter extends ETFConverter[BigInt] {
    def write(o: BigInt) = {
      val bytes = o.toByteArray
      val sign = if (o.signum < 0) 1 else 0
      val length = bytes.length

      val builder = new ByteStringBuilder

      if (length < 255) {
        builder.putByte(ETFTypes.SMALL_BIGNUM)
        builder.putByte(length.toByte)
      } else {
        builder.putByte(ETFTypes.LARGE_BIGNUM)
        builder.putInt(length)
      }

      builder.putByte(sign.toByte)
      builder.putBytes(bytes)

      builder.result
    }

    def readFromIterator(iter: ByteIterator): BigInt = {

      iter.getByte match {
        case SMALL_BIGNUM ⇒
          val size = iter.getByte.toInt
          val sign = iter.getByte.toInt match {
            case 0 ⇒ 1
            case _ ⇒ -1
          }
          val ba = iter.take(size)
          BigInt(sign, ba.toArray)

        case LARGE_BIGNUM ⇒
          val size = iter.getInt(byteOrder)
          val sign = iter.getByte.toInt match {
            case 0 ⇒ 1
            case _ ⇒ -1
          }
          val ba = iter.take(size)
          BigInt(sign, ba.toArray)
      }
    }
  }

  implicit object StringConverter extends ETFConverter[String] {
    def write(o: String) = {
      val builder = new ByteStringBuilder

      builder.putByte(ETFTypes.STRING)
      builder.putShort(o.getBytes("UTF-8").length)
      builder.putBytes(o.getBytes("UTF-8"))
      builder.result
    }

    def readFromIterator(iter: ByteIterator): String = {
      checkSignature(ETFTypes.STRING, iter.getByte)
      val size = iter.getShort(byteOrder)

      val arr = new Array[Byte](size)
      for (i ← 0 to size - 1) arr(i) = iter.next
      new String(arr, "UTF-8")
    }
  }

  implicit object ByteArrayConverter extends ETFConverter[Array[Byte]] {
    def write(o: Array[Byte]) = {
      val builder = new ByteStringBuilder

      builder.putByte(ETFTypes.BIN)
      builder.putInt(o.length)
      builder.putBytes(o)
      builder.result
    }

    def readFromIterator(iter: ByteIterator): Array[Byte] = {

      checkSignature(ETFTypes.BIN, iter.getByte)
      val size = iter.getInt(byteOrder)

      val arr = new Array[Byte](size)
      for (i ← 0 to size - 1) arr(i) = iter.next
      arr
    }
  }

  implicit object AtomConverter extends ETFConverter[Atom] {
    def write(o: Atom) = {
      val builder = new ByteStringBuilder

      builder.putByte(ETFTypes.ATOM)
      builder.putShort(o.v.length)
      builder.putBytes(o.v.getBytes)
      builder.result
    }

    def readFromIterator(iter: ByteIterator): Atom = {

      checkSignature(ETFTypes.ATOM, iter.getByte)
      val size = iter.getShort(byteOrder)

      val arr = new Array[Byte](size)
      for (i ← 0 to size - 1) arr(i) = iter.next
      Atom(new String(arr))
    }
  }

  implicit object SymbolConverter extends ETFConverter[Symbol] {
    def write(o: Symbol) = {
      val builder = new ByteStringBuilder

      builder.putByte(ETFTypes.ATOM)
      builder.putShort(o.name.length)
      builder.putBytes(o.name.getBytes)
      builder.result
    }

    def readFromIterator(iter: ByteIterator): Symbol = {

      checkSignature(ETFTypes.ATOM, iter.getByte)
      val size = iter.getShort(byteOrder)

      val arr = new Array[Byte](size)
      for (i ← 0 to size - 1) arr(i) = iter.next
      Symbol(new String(arr))
    }
  }

  implicit def ListConverter[T](implicit aConv: ETFConverter[T]) = new ETFConverter[List[T]] {
    def write(o: List[T]) = {
      val builder = new ByteStringBuilder

      builder.putByte(ETFTypes.LIST)
      builder.putInt(o.length)
      o.foreach {
        x ⇒
          builder ++= aConv.write(x)
      }
      builder.putByte(ETFTypes.ZERO)
      builder.result
    }

    def readFromIterator(iter: ByteIterator): List[T] = {

      checkSignature(ETFTypes.LIST, iter.getByte)
      val size = iter.getInt(byteOrder)
      val l = for (i ← 1 to size) yield aConv.readFromIterator(iter)
      iter.getByte
      l.toList
    }
  }

  implicit def OptionTConverter[T](implicit aConv: ETFConverter[T]) = new ETFConverter[Option[T]] {
    def write(o: Option[T]) = {
      o match {
        case None ⇒
          val builder = new ByteStringBuilder
          builder.putByte(ETFTypes.NIL)
          builder.result
        case Some(x) ⇒
          aConv.write(x)
      }
    }

    def readFromIterator(iter: ByteIterator): Option[T] = {
      if (iter.head == ETFTypes.NIL) {
        iter.next
        None: Option[T]
      } else {
        Some(aConv.readFromIterator(iter))
      }
    }
  }
}

trait ExtendedETFConverters extends ETFConverters with TupleConverters {

  implicit object BooleanConverter extends BarkConverter[Boolean] {
    def write(o: Boolean) = {
      if (o == true)
        tuple2Converter[Symbol, Symbol].write(('bark, 'true))
      else
        tuple2Converter[Symbol, Symbol].write(('bark, 'false))
    }

    def readFromIterator(bi: ByteIterator) = tuple2Converter[Symbol, Symbol].readFromIterator(bi) match {
      case ('bark, 'true)  ⇒ true
      case ('bark, 'false) ⇒ false
      case _               ⇒ throw new Exception("Incorrect boolean")
    }
  }

  implicit def MapConverter[A, B](implicit aConv: ETFConverter[A], bConv: ETFConverter[B]) = new BarkConverter[Map[A, B]] {
    def write(o: Map[A, B]) = {
      tuple3Converter[Symbol, Symbol, List[Tuple2[A, B]]].write(('bark, 'dict, o.toList))
    }

    def readFromIterator(bi: ByteIterator) = {
      val tl = tuple3Converter[Symbol, Symbol, List[Tuple2[A, B]]].readFromIterator(bi)
      tl._3.toMap
    }
  }

  implicit def SetConverter[A](implicit aConv: ETFConverter[A]) = new BarkConverter[Set[A]] {
    def write(o: Set[A]) = {
      tuple3Converter[Symbol, Symbol, List[A]].write(('bark, 'set, o.toList))
    }

    def readFromIterator(bi: ByteIterator) = {
      val tl = tuple3Converter[Symbol, Symbol, List[A]].readFromIterator(bi)
      tl._3.toSet
    }
  }

  implicit object DateConverter extends BarkConverter[Date] {
    def write(o: Date) = {
      val time = o.getTime
      tuple5Converter[Symbol, Symbol, Int, Int, Int].write(('bark, 'time, (time / 1e9).floor.toInt, ((time % 1e9) / 1e3).floor.toInt, (time % 1e3).floor.toInt))
    }

    def readFromIterator(bi: ByteIterator) = {
      val tl = tuple5Converter[Symbol, Symbol, Int, Int, Int].readFromIterator(bi)
      val stamp = (tl._3.toLong * 1e9) + (tl._4.toLong * 1e3) + tl._5.toLong
      new java.util.Date(stamp.toLong)
    }
  }
}

object ETF extends ExtendedETFConverters {
  def toETF[T](o: T)(implicit writer: ETFWriter[T]): ByteString = {
    val builder = new ByteStringBuilder
    builder.putByte(MAGIC)
    builder ++= writer.write(o)
    builder.result
  }

  def fromETF[T](o: ByteString)(implicit reader: ETFReader[T]): Option[T] = Try {
    val bi = o.iterator
    HeaderFunctions.checkMagic(bi.getByte)
    reader.readFromIterator(bi)
  }.toOption
}