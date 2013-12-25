package class_extractor

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

/**
 * 2. Refining class-instance relationships and identifying alignment target classes
 * 2-3. class_extractor.RefinedClassExtractor.scala
 * - Inputs
 * -- inputs_and_outputs/class-list_from_role.txt
 * -- inputs_and_outputs/class-list_from_type.txt
 * -- inputs_and_outputs/class-instance-refinement-results-20120302.txt
 * - Outputs
 * -- inputs_and_outputs/refined_class_list_from_role.txt
 * -- inputs_and_outputs/refined_class_list_from_type.txt
 * -- inputs_and_outputs/refined_class_list.txt
 */
object RefinedClassExtractor {
  def main(args: Array[String]) {
    val classListFromRole = "inputs_and_outputs/class-list_from_role.txt"
    val classListFromType = "inputs_and_outputs/class-list_from_type.txt"

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
    println(orgRoleClassSet.size)
    println(orgTypeClassSet.size)
    println((orgRoleClassSet ++ orgTypeClassSet).size)
    println(roleClassSet.size)
    println(typeClassSet.size)
    println(allRefinedClassSet.size)

    val refinedClassListFromRole = "inputs_and_outputs/refined_class_list_from_role.txt"
    val refinedClassListFromType = "inputs_and_outputs/refined_class_list_from_type.txt"
    val refinedClassList = "inputs_and_outputs/refined_class_list.txt"
    val writer1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(refinedClassListFromRole), "UTF-8"))
    val writer2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(refinedClassListFromType), "UTF-8"))
    val writer3 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(refinedClassList), "UTF-8"))

    for (c <- roleClassSet) {
      writer1.write(c)
      writer1.newLine
    }
    for (c <- typeClassSet) {
      writer2.write(c)
      writer2.newLine
    }
    for (c <- allRefinedClassSet) {
      writer3.write(c)
      writer3.newLine
    }
    writer1.close
    writer2.close
    writer3.close
  }
}