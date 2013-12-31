package ontology_builder

import com.hp.hpl.jena.util.FileManager
import scala.io.Source
import scala.collection.mutable.Set
import scala.collection.mutable.Map
import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.vocabulary.RDFS
import scala.collection.JavaConversions._
import com.hp.hpl.jena.rdf.model.ResourceFactory
import com.hp.hpl.jena.rdf.model.Literal
import com.hp.hpl.jena.rdf.model.Statement
import java.io.OutputStreamWriter
import java.io.FileOutputStream
import com.hp.hpl.jena.vocabulary.OWL
import com.hp.hpl.jena.rdf.model.ModelFactory

/**
 * 4. Integrating JWO and JWN using DODDLE-OWL
 * 4-2. ontology_builder.OntologyBuilder.scala
 * - Inputs
 * -- ontologies/ontology_constructed_by_doddle.owl
 * -- inputs_and_outputs/jwo_jwn_alignment_results_20120306.txt
 * -- inputs_and_outputs/class-instance-refinement-results-20120302.txt
 * - Output
 * -- ontologies/merged_ontology_20120316.owl
 */
object OntologyBuilder {
  val jwnNs = "http://nlpwww.nict.go.jp/wn-ja/"
  val jwoClassNs = "http://www.yamaguti.comp.ae.keio.ac.jp/wikipedia_ontology/class/"
  val skosNs = "http://www.w3.org/2004/02/skos/core#"
  val skosPrefLabel = ResourceFactory.createProperty(skosNs + "prefLabel")
  val rdfType = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
  val inputOntology = "ontologies/ontology_constructed_by_doddle.owl"
  val model = FileManager.get().loadModel(inputOntology)
  val jwoClassJWNIDMap = Map[String, String]()

  for (stmt <- model.listStatements(null, skosPrefLabel, null).toList) {
    model.remove(stmt)
  }

  def applyAlignmentResults() = {
    val JWOandJWNAlignmentResults = "inputs_and_outputs/jwo_jwn_alignment_results_20120306.txt"
    for (line <- Source.fromFile(JWOandJWNAlignmentResults).getLines) {
      val Array(term, id, rel) = line.split("\t")
      val res = model.getResource(jwnNs + id)
      if (rel.matches("同値関係.*")) {
        val literal = ResourceFactory.createPlainLiteral(term)
        println(res + ": " + term)
        model.add(res, skosPrefLabel, literal)
        jwoClassJWNIDMap.put(term, id)
      } else if (rel.matches("Is-a関係.*")) {
        val subClass = ResourceFactory.createResource(jwoClassNs + term)
        model.add(subClass, rdfType, OWL.Class)
        model.add(subClass, RDFS.subClassOf, res)
        val literal = ResourceFactory.createPlainLiteral(term)
        model.add(subClass, skosPrefLabel, literal)
        //        println("A: " + subClass + "->" + res)
      } else if (rel.matches("不明")) {
        val root = ResourceFactory.createResource("http://www.yamaguti.comp.ae.keio.ac.jp/doddle#CLASS_ROOT")
        val subClass = ResourceFactory.createResource(jwoClassNs + term)
        model.add(subClass, rdfType, OWL.Class)
        model.add(subClass, RDFS.subClassOf, root)
        val literal = ResourceFactory.createPlainLiteral(term)
        model.add(subClass, skosPrefLabel, literal)
        //        println("不明: " + subClass + "->" + root)
      }
    }
  }

  def applyClassInstanceRefinementResults() = {
    val classInstanceRefinementResults = "inputs_and_outputs/class-instance-refinement-results-20120302.txt"
    for (line <- Source.fromFile(classInstanceRefinementResults).getLines) {
      val Array(isCorrect, orgClass, refinedClass, supClass) = line.split("\t")
      if (isCorrect == "true" && supClass != "-") {
        println(supClass + " - " + refinedClass)
        val supClassRes = jwoClassJWNIDMap.get(supClass) match {
          case Some(jwnID) => ResourceFactory.createResource(jwnNs + jwnID)
          case None => ResourceFactory.createResource(jwoClassNs + supClass)
        }
        val subClassRes = ResourceFactory.createResource(jwoClassNs + refinedClass)
        model.add(subClassRes, rdfType, OWL.Class)
        model.add(subClassRes, RDFS.subClassOf, supClassRes)
        val literal = ResourceFactory.createPlainLiteral(refinedClass)
        model.add(subClassRes, skosPrefLabel, literal)
        println("CI: " + subClassRes + "->" + supClassRes)
      }
    }
  }

  def main(args: Array[String]) {
    applyAlignmentResults()
    applyClassInstanceRefinementResults()
    println("クラス数: " + model.listStatements(null, rdfType, OWL.Class).toList.size)
    val outputOntology = "ontologies/merged_ontology_20120316.owl"
    model.write(new OutputStreamWriter(new FileOutputStream(outputOntology), "UTF-8"))
    model.close()
  }
}