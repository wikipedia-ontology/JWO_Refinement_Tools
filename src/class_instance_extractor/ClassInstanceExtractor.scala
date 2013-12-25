package class_instance_extractor

import com.hp.hpl.jena.rdf.model.ResourceFactory
import com.hp.hpl.jena.util.FileManager
import java.io._
import scala.collection.JavaConversions._
import scala.collection.mutable.Map
import com.hp.hpl.jena.vocabulary.OWL

/**
 * 1. Extracting class-instance relationships
 *  1-1. class_instance_extractor.ClassInstanceExtractor.scala
 * - Input
 * --  ontologies/wikipediaontology_instance_20101114ja.rdf
 * - Outputs
 * -- inputs_and_outputs/class-instance.txt
 * -- inputs_and_outputs/class-instance-cnt.csv
 */
object ClassInstanceExtractor {

  def main(args: Array[String]) {
    val inputOntology = "ontologies/wikipediaontology_instance_20101114ja.rdf";
    val outputText = "inputs_and_outputs/class-instance.txt"
    val outputCSV = "inputs_and_outputs/class-instance-cnt.csv"

    val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputText), "UTF-8"))
    val cntWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputCSV), "SJIS"))
    val typeProperty = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")

    val clsInstanceCntMap = Map[String, Int]()
    val model = FileManager.get().loadModel(inputOntology)

    for (stmt <- model.listStatements(null, typeProperty, null).toList()) {
      println("subject: " + stmt.getSubject().getURI())
      println("object: " + stmt.getObject())
      if (!stmt.getObject().equals(OWL.Class)) {
        val instance = stmt.getSubject().getURI().split("instance/")(1)
        val cls = stmt.getObject().asResource().getURI().split("class/")(1)
        clsInstanceCntMap.get(cls) match {
          case Some(cnt) => clsInstanceCntMap.put(cls, cnt + 1)
          case None => clsInstanceCntMap.put(cls, 1)
        }
        writer.write(instance + "\t" + cls)
        writer.newLine()
        println(instance + "\t" + cls)
      }
    }

    writer.close

    for (entry <- clsInstanceCntMap.entrySet()) {
      cntWriter.write(entry.getKey() + "," + entry.getValue())
      cntWriter.newLine()
      println(entry.getKey() + "\t" + entry.getValue())
    }
    cntWriter.close

    println("Classes with instances: " + clsInstanceCntMap.keySet.size)
  }

}