package nl.spotdog.bark.protocol

import akka.util.{ ByteStringBuilder, ByteString, ByteIterator }

import akka.actor.IO._
import ETFTypes._

trait ETFWriter[T] {
  def write(o: T): ByteString
}

trait ETFReader[T] {
  def readFromIterator(o: ByteIterator): T
  def read(o: ByteString): T = readFromIterator(o.iterator)
}

trait ETFConverter[T] extends ETFReader[T] with ETFWriter[T]

trait BarkConverter[T] extends ETFConverter[T]
