package it.unibo.dcs.commons.dataaccess

import java.lang.Boolean

import io.vertx.lang.scala.json.JsonObject

import scala.collection.JavaConverters._
import scala.collection.mutable

final class InsertParams private(val columnNames: String, val values: String)

object InsertParams {

  def apply(jsonObject: JsonObject): InsertParams = {
    def join(it: Traversable[String]): String = it.mkString(", ")

    val columnNames = mutable.Buffer[String]()
    val values = mutable.Buffer[String]()
    for ((columnName, value) <- jsonObject.getMap.asScala) {
      columnNames += escape(columnName)
      values += toValue(value)
    }
    new InsertParams(join(columnNames), join(values))
  }

  private[dataaccess] def escape(columnName: String): String = s"`$columnName`"

  private[dataaccess] def toValue(value: Any): String = value match {
    case s: String => s"'$s'"
    case n: Number => n.toString
    case b: Boolean => if (b) "'1'" else "'0'"
    case _ => throw new IllegalArgumentException("Expected value to ba a String, a Number or a Boolean.")
  }

}