
import java.io._

import scala.io._
import scala.collection.mutable._
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.util._
import com.hp.hpl.jena.query._

/**
 * 日本語WordNetからの同義語抽出
 * (Extracting Synsets from Jpanese WordNet)
 *
 * @author takeshi morita
 *
 * 3. Aligning JWO classes and JWN synsets
 *  3-1. jwo_jwn_alignment_tool.SynonymExtractorFromJpWn.scala
 * - Inputs
 * -- ontologies/JPNWN1.1.owl
 * -- inputs_and_outputs/extract_synonyms_from_jpwn.sparql
 * - Outputs
 * -- inputs_and_outputs/jpwn1.1_synonyms_ja.txt
 */
object SynonymExtractorFromJpWn {

  def main(args: Array[String]) {
    val inputOntology = "ontologies/JPNWN1.1.owl"
    val inputSPARQLQuery = "inputs_and_outputs/extract_synonyms_from_jpwn.sparql"
    val outputText = "inputs_and_outputs/jpwn1.1_synonyms_ja.txt"

    val builder = new StringBuilder
    for (line <- Source.fromFile(inputSPARQLQuery).getLines) {
      builder.append(line)
      builder.append("\n")
    }
    val queryString = builder.toString
    val query = QueryFactory.create(queryString);
    val f = new File(inputOntology)
    val model = FileManager.get().loadModel(f.getAbsolutePath)
    val qexec = QueryExecutionFactory.create(query, model);
    val results = qexec.execSelect();
    val map = Map[String, ListBuffer[String]]()
    while (results.hasNext) {
      val qs = results.nextSolution
      val id = qs.getResource("x").getURI.split("wn-ja/")(1)
      val label = qs.getLiteral("label").getString

      if ("\\d+-n".r.findFirstIn(id) != None) { // synset(noun)のみを抽出
        map.get(id) match {
          case Some(labelList) => labelList += label
          case None => map(id) = ListBuffer[String](label)
        }
      }
    }

    val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputText), "UTF-8"))
    for ((key, value) <- map) {
      writer.write(key + "," + value.mkString(","))
      writer.newLine
    }
    writer.close
  }
}
