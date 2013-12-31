package class_instance_extractor

import scala.collection.mutable.Set
import scala.io.Source

import org.scalaquery.ql.basic.BasicDriver.Implicit.queryToQueryInvoker
import org.scalaquery.ql.basic.BasicDriver.Implicit.tableToQuery
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.session.Database

object AnalyzeRefinementClassInstanceList {
  def main(args: Array[String]) {
    val roleClassSet = Set[String]()
    var source = Source.fromFile("refined_class_list_from_role.txt", "utf-8")
    for (line <- source.getLines()) {
      roleClassSet.add(line)
    }
    val typeClassSet = Set[String]()
    source = Source.fromFile("refined_class_list_from_type.txt", "utf-8")
    for (line <- source.getLines()) {
      typeClassSet.add(line)
    }
    println(roleClassSet.size)
    println(typeClassSet.size)

    val refinedDB = Database.forURL(url = "jdbc:sqlite:refined_class_instance_list.db", driver = "org.sqlite.JDBC")
    var roleTripleCnt = 0
    var typeTripleCnt = 0
    refinedDB withSession {
      val q = for { result <- ClassInstanceList }
        yield result.jwoClass ~ result.jwoInstance
      for ((jwoClass, jwoInstance) <- q.list) {
        if (roleClassSet.contains(jwoClass)) {
          roleTripleCnt += 1
        }
        if (typeClassSet.contains(jwoClass)) {
          typeTripleCnt += 1
        }
      }
    }
    println(roleTripleCnt)
    println(typeTripleCnt)
  }
}