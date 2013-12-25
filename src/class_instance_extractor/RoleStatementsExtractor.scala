package class_instance_extractor

import java.io.BufferedWriter
import java.io.FileOutputStream
import java.io.OutputStreamWriter

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.mutable.Map

import com.hp.hpl.jena.rdf.model.ResourceFactory
import com.hp.hpl.jena.util.FileManager

object RoleStatementsExtractor {

  def main(args: Array[String]) {
    val inputInstances = "ontologies/wikipediaontology_instance_20101114ja.rdf"
    val inputOntology = "ontologies/wikipediaontology_class_20101114ja.rdf"
    val outputText = "inputs_and_outputs/tests/role_statements.txt"
      
    val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputText), "UTF-8"))

    val clsInstanceCntMap = Map[String, Int]()
    val model = FileManager.get().loadModel(inputInstances)
    val ontModel = FileManager.get().loadModel(inputOntology)

    for (stmt <- model.listStatements().toList()) {
      if (stmt.getObject().isResource() && stmt.getObject().asResource().getURI().split("instance/").size == 2) {
        val clsName = stmt.getObject().asResource().getURI().split("instance/")(1)
        val cls = ResourceFactory.createResource("http://www.yamaguti.comp.ae.keio.ac.jp/wikipedia_ontology/class/" + clsName)
        if (0 < ontModel.listStatements(cls, null, null).toList().size()) {
          val sub = stmt.getSubject().getURI().split("instance/")(1)
          val pre = stmt.getPredicate().getURI().split("property/")(1)
          val obj = clsName
          writer.write(sub + "\t" + pre + "\t" + obj)
          writer.newLine()
          println(sub + "\t" + pre + "\t" + obj)
        }
      }
    }
    writer.close()
  }
}