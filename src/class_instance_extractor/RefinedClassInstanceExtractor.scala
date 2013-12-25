package class_instance_extractor

import scala.collection.mutable.Map
import scala.collection.mutable.Set
import scala.io.Source

import org.scalaquery.ql.basic.BasicDriver.Implicit._

import org.scalaquery.session.Database
import org.scalaquery.session.Database.threadLocalSession

/**
 * 2. Refining class-instance relationships and identifying alignment target classes
 *  2-4. class_instance_extractor.RefinedClassInstanceExtractor.scala
 * - Inputs
 * -- inputs_and_outputs/class-list_from_role.txt
 * -- inputs_and_outputs/class-list_from_type.txt
 * -- inputs_and_outputs/class-instance-refinement-results-20120302.txt
 * -- inputs_and_outputs/merged_class_instance_list.db
 * - Output
 * -- inputs_and_outputs/refined_class_instance_list.db
 */
object RefinedClassInstanceExtractor {
  def main(args: Array[String]) {

    val clsRefinedMap = Map[String, String]()
    val classInstanceRefinementResults = "inputs_and_outputs/class-instance-refinement-results-20120302.txt"
    var source = Source.fromFile(classInstanceRefinementResults, "utf-8")
    for (line <- source.getLines()) {
      val Array(isCorrect, org, refined, supCls) = line.split("\t")
      if (isCorrect == "true") {
        clsRefinedMap.put(org, refined)
      }
    }
    val refinedClassSet = clsRefinedMap.keySet

    val roleClassSet = Set[String]()
    val orgRoleClassSet = Set[String]()
    val classListFromRole = "inputs_and_outputs/class-list_from_role.txt"
    source = Source.fromFile(classListFromRole, "utf-8")
    for (cls <- source.getLines()) {
      orgRoleClassSet.add(cls)
      clsRefinedMap.get(cls) match {
        case Some(refCls) =>
          roleClassSet.add(refCls)
        case None =>
      }
    }
    val typeClassSet = Set[String]()
    val orgTypeClassSet = Set[String]()
    val classListFromType = "inputs_and_outputs/class-list_from_type.txt"
    source = Source.fromFile(classListFromType, "utf-8")
    for (cls <- source.getLines()) {
      orgTypeClassSet.add(cls)
      clsRefinedMap.get(cls) match {
        case Some(refCls) =>
          typeClassSet.add(refCls)
        case None =>
      }
    }
    val allRefinedClassSet = roleClassSet ++ typeClassSet

    val mergedDB = Database.forURL(url = "jdbc:sqlite:inputs_and_outputs/merged_class_instance_list.db", driver = "org.sqlite.JDBC")
    val refinedDB = Database.forURL(url = "jdbc:sqlite:inputs_and_outputs/refined_class_instance_list.db", driver = "org.sqlite.JDBC")
    refinedDB withSession {
      (ClassInstanceList.ddl).create
    }

    var cnt = 0
    mergedDB withSession {
      val q = for { result <- ClassInstanceList }
        yield result.jwoClass ~ result.jwoInstance
      for ((jwoClass, jwoInstance) <- q.list) {
        if (refinedClassSet.contains(jwoClass)) {
          clsRefinedMap.get(jwoClass) match {
            case Some(refinedClass) =>
              refinedDB withSession {
                ClassInstanceList.insert(refinedClass, jwoInstance)
              }
              cnt += 1
              if (cnt % 1000 == 0) {
                println(cnt)
              }
            case None =>
          }
        }
      }
    }

  }
}