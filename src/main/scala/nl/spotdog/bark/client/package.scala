package nl.spotdog.bark

import nl.gideondk.sentinel.Task
import nl.gideondk.sentinel.Task._
import nl.spotdog.bark.protocol.ETFReader
import scala.util.Failure
import scala.util.Success
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

package object client {

  implicit class ConvertableTask(val task: Task[BarkClientResult]) {
    def as[T](implicit reader: ETFReader[T]) =
      Task(task.get.map(_.map(_.flatMap(x ⇒ x.as[T] match {
        case None    ⇒ Failure(new Exception("Failed to deserialize"))
        case Some(x) ⇒ Success(x)
      }))))
  }
}