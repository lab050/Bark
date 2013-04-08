package nl.spotdog.bark.data_format

import akka.util.{ ByteStringBuilder, ByteString, ByteIterator }
import BarkTypes._

import java.util.Date

import scala.util.Try

import shapeless._
import HList._
import Tuples._

object HeaderFunctions {
  def checkMagic(b: Byte) = b match {
    case MAGIC ⇒ ()
    case _     ⇒ throw new Exception("Bad Magic")
  }

  def checkSignature(expected: Byte, b: Byte) = if (b != expected) throw new Exception("Unexpected signature")
}

trait ETFConverters {
  import HeaderFunctions._

  implicit val byteOrder: java.nio.ByteOrder = java.nio.ByteOrder.BIG_ENDIAN

  implicit object IntConverter extends ETFConverter[Int] {
    def write(o: Int) = {
      val builder = new ByteStringBuilder
      builder.putByte(BarkTypes.MAGIC)
      builder.putByte(BarkTypes.INT)
      builder.putInt(o).result
    }

    def readFromIterator(iter: ByteIterator): Int = {
      checkMagic(iter.getByte)
      checkSignature(BarkTypes.INT, iter.getByte)
      iter.getInt(byteOrder)
    }
  }

  implicit object DoubleConverter extends ETFConverter[Double] {
    def write(o: Double) = {
      val bytes = String.format("%15.15e", new java.lang.Double(o)).getBytes
      val padded = bytes ++ Stream.continually(0.toByte).take(BarkTypes.FLOAT_LENGTH - bytes.length)

      val builder = new ByteStringBuilder
      builder.putByte(BarkTypes.MAGIC)
      builder.putByte(BarkTypes.FLOAT)
      builder.putBytes(padded)

      builder.result
    }

    def readFromIterator(iter: ByteIterator): Double = {
      checkMagic(iter.getByte)
      checkSignature(BarkTypes.FLOAT, iter.getByte)
      new java.lang.Double(new String(iter.take(BarkTypes.FLOAT_LENGTH).toArray)).doubleValue
    }
  }

  implicit object ByteConverter extends ETFConverter[Byte] {
    def write(o: Byte) = {
      val builder = new ByteStringBuilder
      builder.putByte(BarkTypes.MAGIC)
      builder.putByte(BarkTypes.SMALL_INT)
      builder.putByte(o)
      builder.result
    }

    def readFromIterator(iter: ByteIterator): Byte = {
      checkMagic(iter.getByte)
      checkSignature(BarkTypes.SMALL_INT, iter.getByte)
      iter.getByte
    }
  }

  implicit object BigIntConverter extends ETFConverter[BigInt] {
    def write(o: BigInt) = {
      val bytes = o.toByteArray
      val sign = if (o.signum < 0) 1 else 0
      val length = bytes.length

      val builder = new ByteStringBuilder
      builder.putByte(BarkTypes.MAGIC)

      if (length < 255) {
        builder.putByte(BarkTypes.SMALL_BIGNUM)
        builder.putByte(length.toByte)
      } else {
        builder.putByte(BarkTypes.LARGE_BIGNUM)
        builder.putInt(length)
      }

      builder.putByte(sign.toByte)
      builder.putBytes(bytes)

      builder.result
    }

    def readFromIterator(iter: ByteIterator): BigInt = {
      checkMagic(iter.getByte)
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
      builder.putByte(BarkTypes.MAGIC)
      builder.putByte(BarkTypes.STRING)
      builder.putShort(o.length)
      builder.putBytes(o.getBytes)
      builder.result
    }

    def readFromIterator(iter: ByteIterator): String = {
      checkMagic(iter.getByte)
      checkSignature(BarkTypes.STRING, iter.getByte)
      val size = iter.getShort(byteOrder)

      val arr = new Array[Byte](size)
      for (i ← 0 to size - 1) arr(i) = iter.next
      Symbol(new String(arr))
      new String(arr)
    }
  }

  implicit object ByteArrayConverter extends ETFConverter[Array[Byte]] {
    def write(o: Array[Byte]) = {
      val builder = new ByteStringBuilder
      builder.putByte(BarkTypes.MAGIC)
      builder.putByte(BarkTypes.BIN)
      builder.putInt(o.length)
      builder.putBytes(o)
      builder.result
    }

    def readFromIterator(iter: ByteIterator): Array[Byte] = {
      checkMagic(iter.getByte)
      checkSignature(BarkTypes.BIN, iter.getByte)
      val size = iter.getInt(byteOrder)

      val arr = new Array[Byte](size)
      for (i ← 0 to size - 1) arr(i) = iter.next
      arr
    }
  }

  implicit object SymbolConverter extends ETFConverter[Symbol] {
    def write(o: Symbol) = {
      val builder = new ByteStringBuilder
      builder.putByte(BarkTypes.MAGIC)
      builder.putByte(BarkTypes.ATOM)
      builder.putShort(o.name.length)
      builder.putBytes(o.name.getBytes)
      builder.result
    }

    def readFromIterator(iter: ByteIterator): Symbol = {
      checkMagic(iter.getByte)
      checkSignature(BarkTypes.ATOM, iter.getByte)
      val size = iter.getShort(byteOrder)

      val arr = new Array[Byte](size)
      for (i ← 0 to size - 1) arr(i) = iter.next
      Symbol(new String(arr))
    }
  }

  implicit def ListConverter[T](implicit aConv: ETFConverter[T]) = new ETFConverter[List[T]] {
    def write(o: List[T]) = {
      val builder = new ByteStringBuilder
      builder.putByte(BarkTypes.MAGIC)
      builder.putByte(BarkTypes.LIST)
      builder.putInt(o.length)
      o.foreach {
        x ⇒
          builder ++= aConv.write(x)
      }
      builder.putByte(BarkTypes.ZERO)
      builder.result
    }

    def readFromIterator(iter: ByteIterator): List[T] = {
      checkMagic(iter.getByte)
      checkSignature(BarkTypes.LIST, iter.getByte)
      val size = iter.getInt(byteOrder)
      val l = for (i ← 1 to size) yield aConv.readFromIterator(iter)
      iter.getByte
      l.toList
    }
  }
}

trait BarkConverters extends ETFConverters with TupleConverters {

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

  def wrapReply(bs: ByteString) = {
    val builder = new ByteStringBuilder
    builder.putByte(MAGIC)
    builder.putByte(SMALL_TUPLE)
    builder.putByte(2)
    builder ++= SymbolConverter.write('reply)
    builder ++= bs
    builder.result
  }
}

object Bark extends BarkConverters {
  def toBark[T](o: T)(implicit writer: ETFWriter[T]): ByteString = writer.write(o)

  def fromBark[T](o: ByteString)(implicit reader: ETFReader[T]): Option[T] = Try(reader.read(o)).toOption
}