package class_instance_extractor

import java.io.BufferedWriter
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import com.hp.hpl.jena.util.FileManager
import com.hp.hpl.jena.rdf.model.ResourceFactory
import scala.collection.JavaConversions._

/**
 * 1. Extracting class-instance relationships
 * 1-2. class_instance_extractor.ClassInstanceExtractorFromRole.scala
 * - Input
 * -- ontologies/wikipediaontology_instance_20101114ja.rdf
 * - Oputput
 * -- inputs_and_outputs/class-instance_from_role.txt
 */
object ClassInstanceExtractorFromRole {

  def main(args: Array[String]) {
    val inputOntology = "ontologies/wikipediaontology_instance_20101114ja.rdf"
    val outputText = "inputs_and_outputs/class-instance_from_role.txt"

    val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputText), "UTF-8"))

    val clsInstanceCntMap = Map[String, Int]()
    val model = FileManager.get().loadModel(inputOntology)
    val roleProperties = List("種別", "職業", "種類")
    for (p <- roleProperties) {
      val property = ResourceFactory.createProperty("http://www.yamaguti.comp.ae.keio.ac.jp/wikipedia_ontology/property/" + p)
      for (stmt <- model.listStatements(null, property, null).toList()) {
        val instance = stmt.getSubject().getURI().split("instance/")(1)
        if (stmt.getObject().isResource() && stmt.getObject().asResource().getURI().split("instance/").size == 2) {
          var clsName = stmt.getObject().asResource().getURI().split("instance/")(1)
          clsName = clsName.replaceAll("、", "，")
          clsName = clsName.replaceAll("_", "")
          clsName = clsName.replaceAll(",", "，")
          if (1 < clsName.split("，").size) {
            println("split: " + clsName)
            for (c <- clsName.split("，")) {
              writer.write(instance + "\t" + c + "\t" + p + "\tS")
              writer.newLine()
              println(instance + "\t" + c + "\t" + p + "\tS")
            }
          } else {
            println("not-split: " + clsName)
            writer.write(instance + "\t" + clsName + "\t" + p + "\tO")
            writer.newLine()
            println(instance + "\t" + clsName + "\t" + p + "\tO")
          }
        } else {
          println(instance + "\t" + stmt.getObject().asLiteral().toString() + "\t" + p + "\tL")
        }
      }
    }
    writer.close()
  }
}