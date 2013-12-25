package class_instance_extractor

import org.scalaquery.session.Database
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.ql.basic.BasicTable
import org.scalaquery.ql.basic.BasicDriver.Implicit._
import java.sql.Timestamp
import java.util.Date
import java.io.File
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.collection.mutable.Map

/**
 * 1. Extracting class-instance relationships
 * 1-3. class_instance_extractor.ConvertClassInstanceListToSQLiteDB.scala
 * - Inputs
 * -- inputs_and_outputs/class-instance.txt
 * -- inputs_and_outputs/class-instance_from_role.txt
 * - Oputputs
 * -- inputs_and_outputs/class_instance_list_from_type.db
 * -- inputs_and_outputs/class_instance_list_from_role.db
 *
 */
object ClassInstanceList extends BasicTable[(String, String)]("ClassInstanceListTable") {
  def jwoClass = column[String]("jwoClass")
  def jwoInstance = column[String]("jwoInstance")

  def * = jwoClass ~ jwoInstance
}

object ConvertClassInstanceListToSQLiteDB {
  val inputClassInstanceListFromRole = "inputs_and_outputs/class-instance_from_role.txt"
  val inputClassInstanceList = "inputs_and_outputs/class-instance.txt"
  val outputDBFromType = Database.forURL(url = "jdbc:sqlite:inputs_and_outputs/class_instance_list_from_type.db", driver = "org.sqlite.JDBC")
  val outputDBFromRole = Database.forURL(url = "jdbc:sqlite:inputs_and_outputs/class_instance_list_from_role.db", driver = "org.sqlite.JDBC")

  def convertClassInstanceListFromTypeToSQLiteDB() {
    outputDBFromType withSession {
      (ClassInstanceList.ddl).create
      val clsInstanceListMap = Map[String, ListBuffer[String]]()
      var cnt = 0
      for (line <- Source.fromFile(inputClassInstanceList).getLines) {
        if (line.split("\t").size == 2) {
          val Array(instance, cls) = line.split("\t")
          ClassInstanceList.insert(cls, instance)
          cnt += 1
          if (cnt % 10000 == 0) {
            println(cnt)
          }
        }
      }
    }
  }

  def convertClassInstanceListFromRoleToSQLiteDB() {
    outputDBFromRole withSession {
      (ClassInstanceList.ddl).create
      val clsInstanceListMap = Map[String, ListBuffer[String]]()
      var cnt = 0
      for (line <- Source.fromFile(inputClassInstanceListFromRole).getLines) {
        if (line.split("\t").size == 4) {
          val Array(instance, cls, p, t) = line.split("\t")
          var clsR = cls.replaceAll("，", "")
          if (0 < clsR.size) {
            clsInstanceListMap.get(clsR) match {
              case Some(list) => list += instance
              case None =>
                val list = ListBuffer[String](instance)
                clsInstanceListMap.put(clsR, list)
            }
          }
        }
      }
      for ((key, values) <- clsInstanceListMap) {
        if (10 < values.size) {
          for (instance <- values) {
            ClassInstanceList.insert(key, instance)
            cnt += 1
            if (cnt % 10000 == 0) {
              println(cnt)
            }
          }
        }
      }
    }
  }

  def main(args: Array[String]) {
    convertClassInstanceListFromTypeToSQLiteDB()
    convertClassInstanceListFromRoleToSQLiteDB()
    outputDBFromType withSession {
      val referedClass = "人物"
      val q = for { result <- ClassInstanceList if result.jwoClass === referedClass }
        yield result.jwoClass ~ result.jwoInstance
      for ((jwoClass, jwoInstance) <- q.list.take(100)) {
        println(jwoClass + "," + jwoInstance)
      }
    }
  }
}