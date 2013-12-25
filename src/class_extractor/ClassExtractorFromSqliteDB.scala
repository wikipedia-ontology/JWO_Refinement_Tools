package class_extractor

import org.scalaquery.session.Database
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.ql.basic.BasicTable
import org.scalaquery.ql.basic.BasicDriver.Implicit._
import scala.collection.mutable.Set
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.FileOutputStream
import class_instance_extractor.ClassInstanceList

/**
 * 2. Refining class-instance relationships and identifying alignment target classes
 * 2-1. class_extractor.ClassExtractorFromSqliteDB.scala
 * - Inputs
 * -- inputs_and_outputs/merged_class_instance_list.db
 * -- inputs_and_outputs/class_instance_list_from_type.db
 * -- inputs_and_outputs/class_instance_list_from_role.db
 * - Outputs
 * -- inputs_and_outputs/merged-class-list.txt
 * -- inputs_and_outputs/class-list_from_type.txt
 * -- inputs_and_outputs/class-list_from_role.txt
 *
 */
object ClassExtractorFromSqliteDB {

  def extractClassList(inputDB: String, outputText: String) {
    val classSet = Set[String]()
    val database = Database.forURL(url = inputDB, driver = "org.sqlite.JDBC")
    var cnt = 0
    database withSession {
      val q = for { result <- ClassInstanceList }
        yield result.jwoClass ~ result.jwoInstance
      for ((jwoClass, jwoInstance) <- q.list) {
        // println(jwoClass + "," + jwoInstance)
        classSet.add(jwoClass)
        cnt += 1
      }
    }
    println(cnt)
    val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputText), "UTF-8"))
    println(classSet.size)
    for (cls: String <- classSet) {
      writer.write(cls)
      writer.newLine()
    }
    writer.close
  }

  def main(args: Array[String]) {
    val inputDB = "jdbc:sqlite:inputs_and_outputs/merged_class_instance_list.db"
    val inputDBFromType = "jdbc:sqlite:inputs_and_outputs/class_instance_list_from_type.db"
    val inputDBFromRole = "jdbc:sqlite:inputs_and_outputs/class_instance_list_from_role.db"
    val outputText = "inputs_and_outputs/merged-class-list.txt"
    val outputTextFromType = "inputs_and_outputs/class-list_from_type.txt"
    val outputTextFromRole = "inputs_and_outputs/class-list_from_role.txt"
    extractClassList(inputDB, outputText)
    extractClassList(inputDBFromType, outputTextFromType)
    extractClassList(inputDBFromRole, outputTextFromRole)
  }
}