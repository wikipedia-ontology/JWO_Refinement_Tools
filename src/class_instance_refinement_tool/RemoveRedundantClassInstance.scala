package class_instance_refinement_tool

import com.hp.hpl.jena.util.FileManager
import scala.io.Source
import scala.collection.mutable.Set
import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.vocabulary.RDFS
import scala.collection.JavaConversions._
import com.hp.hpl.jena.rdf.model.ResourceFactory
import com.hp.hpl.jena.rdf.model.Literal
import com.hp.hpl.jena.rdf.model.Statement
import java.io.OutputStreamWriter
import java.io.FileOutputStream
import com.hp.hpl.jena.vocabulary.OWL
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
 * 5. Removing redundant class-instance relationships
 *  5-1. class_instance_refinement_tool.RemoveRedundantClassInstance.scala
 * - Inputs
 * -- ontologies/merged_ontology_revised_by_hand_20130912.owl
 * -- inputs_and_outputs/refined_class_instance_list2.db
 * - Outputs
 * -- inputs_and_outputs/refined_class_instance_list_removing_redundant_type.db
 * -- inputs_and_outputs/redundant_type_set.txt
 */
object RemoveRedundantClassInstance {
  // 自分自身をサブクラスに持つクラスが存在しているので，手動で除去する必要あり
  val inputOntology = "ontologies/merged_ontology_revised_by_hand_20130912.owl"
  val model = FileManager.get().loadModel(inputOntology)

  def getSuperClassSet(clsRes: Resource, supClassSet: Set[Resource]): Unit = {
    for (stmt <- clsRes.listProperties(RDFS.subClassOf).toList()) {
      val supClsRes = stmt.getObject().asResource()
      supClassSet.add(supClsRes)
      getSuperClassSet(supClsRes, supClassSet)
    }
  }

  def main(args: Array[String]) {
    val jwnNs = "http://nlpwww.nict.go.jp/wn-ja/"
    val jwoClassNs = "http://www.yamaguti.comp.ae.keio.ac.jp/wikipedia_ontology/class/"
    val jwoInstanceNs = "http://www.yamaguti.comp.ae.keio.ac.jp/wikipedia_ontology/instance/"
    val skosNs = "http://www.w3.org/2004/02/skos/core#"
    val skosPrefLabel = ResourceFactory.createProperty(skosNs + "prefLabel")
    val rdfType = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")

    val refinedClassInstanceDB = Database.forURL(url = "jdbc:sqlite:inputs_and_outputs/refined_class_instance_list2.db", driver = "org.sqlite.JDBC")
    val refinedClassInstanceDB2 = Database.forURL(url = "jdbc:sqlite:inputs_and_outputs/refined_class_instance_list_removing_redundant_type.db", driver = "org.sqlite.JDBC")
    val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("inputs_and_outputs/redundant_type_set.txt"), "UTF-8"))

    val instanceTypeSetMap = Map[String, Set[String]]()
    refinedClassInstanceDB withSession {
      val q = for { result <- ClassInstanceList }
        yield result.jwoClass ~ result.jwoInstance
      for ((jwoClass, jwoInstance) <- q.list) {
        instanceTypeSetMap.get(jwoInstance) match {
          case Some(typeSet) => typeSet.add(jwoClass)
          case None =>
            val typeSet = Set(jwoClass)
            instanceTypeSetMap.put(jwoInstance, typeSet)
        }
      }
    }
    println(instanceTypeSetMap.keySet.size)
    val instanceTypeResSetMap = Map[String, Set[Resource]]()
    for (i <- instanceTypeSetMap.keySet) {
      val typeSet = instanceTypeSetMap(i)
      val typeResSet = Set[Resource]()
      for (t <- typeSet) {
        for (stmt <- model.listStatements(null, skosPrefLabel, t).toList) {
          typeResSet.add(stmt.getSubject())
        }
      }
      if (2 <= typeResSet.size) {
        val redundantTypeSet = Set[Resource]()
        for (tr <- typeResSet) {
          val superClassSet = Set[Resource]()
          getSuperClassSet(tr, superClassSet)
          for (ot <- typeResSet - tr) {
            if (superClassSet.contains(ot)) {
              redundantTypeSet.add(ot)
            }
          }
        }
        if (0 < redundantTypeSet.size) {
          writer.write(i + ": " + redundantTypeSet)
          writer.newLine()
          println(i + ": " + redundantTypeSet)
          val refinedTypeResSet = typeResSet -- redundantTypeSet
          instanceTypeResSetMap.put(i, refinedTypeResSet)
        } else {
          instanceTypeResSetMap.put(i, typeResSet)
        }
      } else {
        instanceTypeResSetMap.put(i, typeResSet)
      }
      //      println(i + ": " + typeResSet)
    }
    writer.close
    var cnt = 0
    refinedClassInstanceDB2 withSession {
      (ClassInstanceList.ddl).create
      for ((i, typeSet) <- instanceTypeResSetMap) {
        for (t <- typeSet) {
          ClassInstanceList.insert(t.getURI(), i)
          cnt += 1
          if (cnt % 10000 == 0) {
            println(cnt)
          }
        }
      }
    }
  }
}