package it.unibo.dcs.dataacces.commons

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
      columnNames += toColumn(columnName)
      values += toValue(value)
    }
    new InsertParams(join(columnNames), join(values))
  }

  private def toColumn(columnName: String): String = s"`$columnName`"

  private def toValue(value: Any): String = value match {
    case s: String => s"'$s'"
    case n: Number => n.toString
    case b: Boolean => b.toString.toUpperCase
  }

}

object Test extends App {

  val params = InsertParams(new JsonObject()
    .put("username", "vandao9")
    .put("first_name", "Mattia")
    .put("last_name", "Vandi")
    .put("visible", true)
    .put("count", 5)
  )

  println(params.columnNames)
  println(params.values)

}
