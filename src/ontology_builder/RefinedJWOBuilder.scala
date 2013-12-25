package ontology_builder

import java.io._
import com.hp.hpl.jena.util.FileManager
import org.scalaquery.session.Database
import org.scalaquery.ql.basic.BasicDriver.Implicit.columnBaseToInsertInvoker
import org.scalaquery.ql.basic.BasicDriver.Implicit.queryToQueryInvoker
import org.scalaquery.ql.basic.BasicDriver.Implicit.scalaQueryDriver
import org.scalaquery.ql.basic.BasicDriver.Implicit.tableToQuery
import org.scalaquery.ql.basic.BasicTable
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.session.Database
import com.hp.hpl.jena.vocabulary._
import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.rdf.model.ResourceFactory
import scala.collection.mutable.Set
import scala.collection.mutable.Map
import scala.collection.JavaConversions._

import property_elevator.ClassPropertyWithLabelList
import class_instance_extractor.ClassInstanceList

case class JWOProperty(property: String, label: String)

/**
 * 7. Buiding Refined JWO
 * 7-1. ontology_builder.RefinedJWOBuilder.scala
 * - Inputs
 * -- inputs_and_outputs/refined_class_instance_list_removing_redundant_type.db
 * -- inputs_and_outputs/class_elevated_property_list_with_label_and_depth_removing_inherited_properties.db
 * -- ontologies/merged_ontology_revised_by_hand_20130912.owl
 * - Outputs
 * -- ontologies/refined_jwo_class_instance_20131225.owl
 * -- ontologies/refined_jwo_20131225.owl
 */
object RefinedJWOBuilder {

  def makeClassInstanceSetMap: Map[String, Set[String]] = {
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
    return classInstanceSetMap
  }

  def makeClassPropertySetMap: Map[Resource, Set[JWOProperty]] = {
    val classPropertySetMap = Map[Resource, Set[JWOProperty]]()
    val classPropertyListDB = Database.forURL(url = "jdbc:sqlite:inputs_and_outputs/class_elevated_property_list_with_label_and_depth_removing_inherited_properties.db", driver = "org.sqlite.JDBC")
    classPropertyListDB withSession {
      val q = for { result <- ClassPropertyWithLabelList }
        yield result.jwoClass ~ result.jwoProperty ~ result.label ~ result.depthOfClass
      for ((jwoClass, jwoProperty, label, depthOfClass) <- q.list) {
        val property = JWOProperty(jwoProperty, label)
        val jwoClassRes = ResourceFactory.createResource(jwoClass)
        classPropertySetMap.get(jwoClassRes) match {
          case Some(propertySet) => propertySet.add(property)
          case None =>
            val propertySet = Set(property)
            classPropertySetMap.put(jwoClassRes, propertySet)
        }
      }
    }
    return classPropertySetMap
  }

  def main(args: Array[String]) {
    val inputOntology = "ontologies/merged_ontology_revised_by_hand_20130912.owl"
    val inputModel = FileManager.get().loadModel(inputOntology)
    val jwoNS = "http://www.yamaguti.comp.ae.keio.ac.jp/wikipedia_ontology/"
    val jwoInstanceNS = jwoNS + "instance/"
    val jwoClassNS = jwoNS + "class/"
    val jwoPropertyNS = jwoNS + "property/"
    val rdfTypeProperty = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")

    val refinedJWOClassInstance = "ontologies/refined_jwo_class_instance_20131225.owl"
    val refinedJWOClassInstanceModel = ModelFactory.createDefaultModel()
    val refinedJWO = "ontologies/refined_jwo_20131225.owl"
    val refinedJWOModel = ModelFactory.createDefaultModel()
    refinedJWOModel.add(inputModel)

    val classInstanceSetMap = makeClassInstanceSetMap
    for ((jwoClass, jwoInstanceSet) <- classInstanceSetMap) {
      for (jwoInstance <- jwoInstanceSet) {
        val clsRes = ResourceFactory.createResource(jwoClass)
        val instanceRes = ResourceFactory.createResource(jwoInstanceNS + jwoInstance)
        refinedJWOClassInstanceModel.add(instanceRes, rdfTypeProperty, clsRes)
      }
    }

    refinedJWOClassInstanceModel.write(new OutputStreamWriter(new FileOutputStream(refinedJWOClassInstance), "UTF-8"))
    refinedJWOClassInstanceModel.close()

    val classPropertySetMap = makeClassPropertySetMap
    for ((jwoClass, jwoPropertySet) <- classPropertySetMap) {
      for (jwoProperty <- jwoPropertySet) {
        val propertyRes = ResourceFactory.createProperty(jwoPropertyNS + jwoProperty.property)
        refinedJWOModel.add(propertyRes, rdfTypeProperty, RDF.Property)
        refinedJWOModel.add(propertyRes, RDFS.domain, jwoClass)
      }
    }

    refinedJWOModel.write(new OutputStreamWriter(new FileOutputStream(refinedJWO), "UTF-8"))
    refinedJWOModel.close()

  }
}