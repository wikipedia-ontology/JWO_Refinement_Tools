
package test

import scala.collection.JavaConversions._
import scala.collection.mutable._
import scala.io._
import java.io._
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.util.FileManager
import com.hp.hpl.jena.vocabulary.RDFS

object WikiontIsaToDot {

  def writeIsaGraph(sub: String, sup: String, supSubSetMap: Map[String, Set[String]], supSubSet: Set[String], idSet: Set[String]): Unit = {
    supSubSet += <isa>"{ sup }" -> "{ sub }" [dir = back];</isa>.text
    idSet += sub
    idSet += sup
    supSubSetMap.get(sub) match {
      case Some(subSubSet) =>
        for (subSub <- subSubSet) {
          writeIsaGraph(subSub, sub, supSubSetMap, supSubSet, idSet)
        }
      case None =>
    }
  }

  def writeNodeInfo(idSet: Set[String], writer: BufferedWriter) = {
    val ns = "http://www.yamaguti.comp.ae.keio.ac.jp/wikipedia_ontology/class/";
    for (id <- idSet) {
      val label = id.replaceAll(ns, "")
      writer.write(<label>"{ id }" [label="{ label }" fontname="MSUIGOTHIC.ttf"];</label>.text)
      writer.newLine
    }
  }

  def writeDot(idSet: Set[String], supSubSet: Set[String], writer: BufferedWriter) = {
    writeNodeInfo(idSet, writer)
    writer.write(supSubSet.mkString("\n"))
    writer.write("}")
    writer.newLine()
    writer.close
  }

  def main(args: Array[String]) {
    val supSubSetMap: Map[String, Set[String]] = Map()

    val ontology: Model = FileManager.get().loadModel("merged_ontology_20120306.owl");
    for (stmt <- ontology.listStatements(null, RDFS.subClassOf, null).toList) {
      val sub = stmt.getSubject().getURI()
      val sup = stmt.getObject().asResource().getURI()
      supSubSetMap.get(sup) match {
        case Some(subSet) => subSet.add(sub)
        case None =>
          val subSet = Set[String]()
          subSet.add(sub)
          supSubSetMap.put(sup, subSet)
      }
    }

    var writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("after.dot"), "UTF-8"))
    //    writer.write("digraph wikiont_jpwn_isa {\ngraph [size = \"330,40\" page = \"330,40\"];")
    writer.write("digraph wikiont_isa {")
    writer.newLine()

    val supSubSet: Set[String] = Set()
    val idSet: Set[String] = Set()
    for ((sup, subList) <- supSubSetMap) {
      for (sub <- subList) {
        writeIsaGraph(sub, sup, supSubSetMap, supSubSet, idSet)
      }
    }
    writeDot(idSet, supSubSet, writer)
  }
}