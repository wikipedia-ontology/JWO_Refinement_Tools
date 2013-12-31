
package class_instance_refinement_tool

import org.scalaquery.session.Database
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.ql.basic.BasicTable
import org.scalaquery.ql.basic.BasicDriver.Implicit._
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Set
import scala.collection.mutable.Map
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.FileOutputStream
import scala.io.Source
import class_instance_extractor.ClassInstanceList

/**
 * 2. Refining class-instance relationships and identifying alignment target classes
 * 2-5. class_instance_refinement_tool.DBDuplicationCheck.scala
 * - Input
 * -- inputs_and_outputs/refined_class_instance_list.db
 * - Output
 * -- inputs_and_outputs/refined_class_instance_list2.db
 */
object DBDuplicationCheck {
  def main(args: Array[String]) {
    val classInstanceSet = Set[String]()
    val typeDB = Database.forURL(url = "jdbc:sqlite:inputs_and_outputs/refined_class_instance_list.db", driver = "org.sqlite.JDBC")
    var cnt = 0
    typeDB withSession {
      val q = for { result <- ClassInstanceList }
        yield result.jwoClass ~ result.jwoInstance
      for ((jwoClass, jwoInstance) <- q.list) {
        classInstanceSet.add(jwoClass + "\t" + jwoInstance)
        cnt += 1
        if (cnt % 1000 == 0) {
          println(cnt)
        }
      }
    }
    println(classInstanceSet.size)
    cnt = 0
    val refinedDB = Database.forURL(url = "jdbc:sqlite:inputs_and_outputs/refined_class_instance_list2.db", driver = "org.sqlite.JDBC")
    refinedDB withSession {
      (ClassInstanceList.ddl).create
      for (ci <- classInstanceSet) {
        val Array(cls, instance) = ci.split("\t")
        ClassInstanceList.insert(cls, instance)
        cnt += 1
        if (cnt % 1000 == 0) {
          println(cnt)
        }
      }
    }
  }
}