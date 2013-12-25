package test

import org.scalaquery.session.Database
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.ql.basic.BasicDriver.Implicit._
import scala.collection.mutable.Set
import scala.io.Source
import class_instance_extractor.ClassInstanceList

object CICounter {
  def main(args: Array[String]) {

    val roleClassSet = Set[String]()
    var source = Source.fromFile("refined_class_list_from_role.txt", "utf-8")
    for (cls <- source.getLines()) {
      roleClassSet.add(cls)
    }
    println(roleClassSet.size)
    val typeClassSet = Set[String]()
    source = Source.fromFile("refined_class_list_from_type.txt", "utf-8")
    for (cls <- source.getLines()) {
      typeClassSet.add(cls)
    }
    println(typeClassSet.size)

    var roleCICnt = 0
    var typeCICnt = 0
    val refinedDB = Database.forURL(url = "jdbc:sqlite:refined_class_instance_list2.db", driver = "org.sqlite.JDBC")
    refinedDB withSession {

      val q = for { result <- ClassInstanceList }
        yield result.jwoClass ~ result.jwoInstance
      for ((jwoClass, jwoInstance) <- q.list) {
        if (roleClassSet.contains(jwoClass)) {
          roleCICnt += 1
        }
        if (typeClassSet.contains(jwoClass)) {
          typeCICnt += 1
        }
      }
    }
    println(roleCICnt)
    println(typeCICnt)
  }
}