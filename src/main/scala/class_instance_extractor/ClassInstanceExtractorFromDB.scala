package class_instance_extractor

import java.io.BufferedWriter
import java.io.FileOutputStream
import java.io.OutputStreamWriter

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map

import org.scalaquery.ql.basic.BasicDriver.Implicit.columnBaseToInsertInvoker
import org.scalaquery.ql.basic.BasicDriver.Implicit.queryToQueryInvoker
import org.scalaquery.ql.basic.BasicDriver.Implicit.scalaQueryDriver
import org.scalaquery.ql.basic.BasicDriver.Implicit.tableToQuery
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.session.Database

/**
 * 1. Extracting class-instance relationships
 * 1-4. class_instance_extractor.ClassInstanceExtractorFromDB.scala
 * - Inputs
 * -- inputs_and_outputs/class_instance_list_from_role.db
 * -- inputs_and_outputs/class_instance_list_from_type.db
 * - Outputs
 * -- inputs_and_outputs/merged_class_instance_list.db
 * -- inputs_and_outputs/duplicated_class-instance_from_role.txt
 */
object ClassInstanceExtractorFromDB {

  def putClassInstance(clsInstanceListMap: Map[String, ListBuffer[String]], jwoClass: String, jwoInstance: String) = {
    clsInstanceListMap.get(jwoClass) match {
      case Some(list) => list += jwoInstance
      case None =>
        val list = ListBuffer[String](jwoInstance)
        clsInstanceListMap.put(jwoClass, list)
    }
  }

  def main(args: Array[String]) {

    val mergedDB = Database.forURL(url = "jdbc:sqlite:inputs_and_outputs/merged_class_instance_list.db", driver = "org.sqlite.JDBC")
    mergedDB withSession {
      (ClassInstanceList.ddl).create
    }
    val roleDB = Database.forURL(url = "jdbc:sqlite:inputs_and_outputs/class_instance_list_from_role.db", driver = "org.sqlite.JDBC")
    val typeDB = Database.forURL(url = "jdbc:sqlite:inputs_and_outputs/class_instance_list_from_type.db", driver = "org.sqlite.JDBC")
    val outputText = "inputs_and_outputs/duplicated_class-instance_from_role.txt"
    val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputText), "UTF-8"))
    var cnt = 0

    val clsInstanceListMap = Map[String, ListBuffer[String]]()
    roleDB withSession {
      val q = for { result <- ClassInstanceList }
        yield result.jwoClass ~ result.jwoInstance
      for ((jwoClass, jwoInstance) <- q.list) {
        putClassInstance(clsInstanceListMap, jwoClass, jwoInstance)
      }
    }
    println("created map")
    typeDB withSession {
      val q = for { result <- ClassInstanceList }
        yield result.jwoClass ~ result.jwoInstance
      for ((jwoClass, jwoInstance) <- q.list) {
        if (clsInstanceListMap.contains(jwoClass)) {
          putClassInstance(clsInstanceListMap, jwoClass, jwoInstance)
        } else {
          mergedDB withSession {
            ClassInstanceList.insert(jwoClass, jwoInstance)
          }
          cnt += 1
        }
        if (cnt % 1000 == 0) {
          println(cnt)
        }
      }
    }

    for (cls <- clsInstanceListMap.keySet) {
      clsInstanceListMap.get(cls) match {
        case Some(list) =>
          for (i <- list.toSet[String]) {
            mergedDB withSession {
              ClassInstanceList.insert(cls, i)
            }
            cnt += 1
            if (cnt % 1000 == 0) {
              println(cnt)
            }
          }
          val duplicatedList = list -- list.toSet[String]
          for (al <- duplicatedList) {
            writer.write(cls + "\t" + al)
            writer.newLine
          }
        case None =>
      }
    }
    writer.close
  }
}