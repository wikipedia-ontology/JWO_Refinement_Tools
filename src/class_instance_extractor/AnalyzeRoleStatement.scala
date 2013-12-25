
package class_instance_extractor

import java.io.BufferedWriter
import java.io.FileOutputStream
import java.io.OutputStreamWriter

import scala.collection.mutable.Set
import scala.io.Source

import org.scalaquery.ql.basic.BasicDriver.Implicit.queryToQueryInvoker
import org.scalaquery.ql.basic.BasicDriver.Implicit.tableToQuery
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.session.Database

object AnalyzeRoleStatement {
  def main(args: Array[String]) {
    val duplicatedClassInstanceSet = Set[String]()
    val inputText = "inputs_and_outputs/duplicated_class-instance_from_role.txt"
    val source = Source.fromFile(inputText, "utf-8")
    for (line <- source.getLines()) {
      duplicatedClassInstanceSet.add(line)
    }
    println(duplicatedClassInstanceSet.size)

    val classInstanceSet = Set[String]()
    val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("inputs_and_outputs/tests/added_class-instance_from_role.txt"), "UTF-8"))
    val roleDB = Database.forURL(url = "jdbc:sqlite:inputs_and_outputs/class_instance_list_from_role.db", driver = "org.sqlite.JDBC")
    var cnt = 0
    roleDB withSession {
      val q = for { result <- ClassInstanceList }
        yield result.jwoClass ~ result.jwoInstance
      for ((jwoClass, jwoInstance) <- q.list) {
        classInstanceSet.add(jwoClass + "\t" + jwoInstance)
      }
    }

    println(classInstanceSet.size)
    val addedSet = classInstanceSet -- duplicatedClassInstanceSet
    for (ci <- addedSet) {
      writer.write(ci)
      writer.newLine
      println(ci)
      cnt += 1
    }
    writer.close
    println(cnt)
  }
}