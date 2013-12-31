package class_property_extractor

import scala.collection.JavaConversions._
import scala.collection.mutable.Map
import scala.collection.mutable.Set

import org.scalaquery.ql.basic.BasicDriver.Implicit.queryToQueryInvoker
import org.scalaquery.ql.basic.BasicDriver.Implicit.tableToQuery
import org.scalaquery.ql.basic.BasicTable
import org.scalaquery.ql.basic.BasicDriver.Implicit._
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.session.Database

import com.hp.hpl.jena.rdf.model.ResourceFactory
import com.hp.hpl.jena.util.FileManager

import class_instance_extractor.ClassInstanceList

object ClassPropertyList extends BasicTable[(String, String, Int)]("ClassPropertyListTable") {
  def jwoClass = column[String]("jwoClass")
  def jwoProperty = column[String]("jwoProperty")
  def jwoPropertyCount = column[Int]("jwoPropertyCount")

  def * = jwoClass ~ jwoProperty ~ jwoPropertyCount
}

/**
 * 6. Defining the domains of properties based on a consideration of property inheritance
 * 6-1. class_property_extractor.ClassPropertyExtractor.scala
 * - Inputs
 * -- inputs_and_outputs/refined_class_instance_list_removing_redundant_type.db
 * -- ontologies/wikipediaontology_instance_20101114ja.rdf
 * - Output
 * -- inputs_and_outputs/class_property_list_with_count.db
 */
object ClassPropertyExtractor {
  def main(args: Array[String]) {
    val classInstanceSetMap = Map[String, Set[String]]()
    val typeDB = Database.forURL(url = "jdbc:sqlite:inputs_and_outputs/refined_class_instance_list_removing_redundant_type.db", driver = "org.sqlite.JDBC")
    typeDB withSession {
      val q = for { result <- ClassInstanceList }
        yield result.jwoClass ~ result.jwoInstance
      for ((jwoClass, jwoInstance) <- q.list) {
        classInstanceSetMap.get(jwoClass) match {
          case Some(instanceSet) => instanceSet.add(jwoInstance)
          case None =>
            val instanceSet = Set(jwoInstance)
            classInstanceSetMap.put(jwoClass, instanceSet)
        }
      }
    }

    println("model loading");
    val ns = "http://www.yamaguti.comp.ae.keio.ac.jp/wikipedia_ontology/instance/"
    val inputOntology = "ontologies/wikipediaontology_instance_20101114ja.rdf"
    val model = FileManager.get().loadModel(inputOntology)
    println("model loaded")

    val classPropertyListDB = Database.forURL(url = "jdbc:sqlite:inputs_and_outputs/class_property_list_with_count.db", driver = "org.sqlite.JDBC")
    classPropertyListDB withSession {
      (ClassPropertyList.ddl).create
    }

    for ((cls, instanceSet) <- classInstanceSetMap) {
      println(cls + "->" + instanceSet.size)
      val propertyCountMap = Map[String, Int]()
      for (instance <- instanceSet) {
        for (stmt <- model.listStatements(ResourceFactory.createResource(ns + instance), null, null).toList()) {
          var property = stmt.getPredicate().getURI()
          if (property.split("property/").size == 2) {
            property = property.split("property/")(1) // JWOのプロパティの場合には，プロパティ名のみに変換．それ以外は名前空間も保持．
          }
          //    println(instance + "-->" + property)
          if (property != "http://www.w3.org/1999/02/22-rdf-syntax-ns#type") {
            propertyCountMap.get(property) match {
              case Some(count) => propertyCountMap.put(property, count + 1)
              case None =>
                propertyCountMap.put(property, 1)
            }
          }
        }
      }
      classPropertyListDB withSession {
        for ((property, count) <- propertyCountMap) {
          // println(cls + "\t" + property + "\t" + count)
          ClassPropertyList.insert(cls, property, count)
        }
      }
    }

  }
}

