package test

import com.hp.hpl.jena.rdf.model.ResourceFactory
import com.hp.hpl.jena.util.FileManager
import java.io._
import scala.collection.JavaConversions._
import scala.collection.mutable.Map
import scala.collection.mutable.Set
import com.hp.hpl.jena.vocabulary.OWL

// ブランチを削除した結果を反映するように修正すべき（もう少し後で修正）

object ClassPropertyExtractor {
  def main(args: Array[String]) {
    val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("class-property.txt"), "UTF-8"))
    val cntWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("class-property-cnt.txt"), "SJIS"))
    val typeProperty = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")

    val clsPropertySetMap = Map[String, Set[String]]()
    val model = FileManager.get().loadModel("wikipediaontology_instance_20101114ja.rdf")
    for (stmt <- model.listStatements().toList()) {
      if (!stmt.getPredicate().equals(typeProperty)) {
        for (tstmt <- stmt.getSubject().listProperties(typeProperty)) {
          if (!tstmt.getObject().equals(OWL.Class) && stmt.getPredicate().getURI().contains("property/")) {
            val cls = tstmt.getObject().asResource().getURI().split("class/")(1)
            val property = stmt.getPredicate().getURI().split("property/")(1)
            clsPropertySetMap.get(cls) match {
              case Some(list) => list.add(property)
              case None =>
                val propertySet = Set[String]()
                propertySet.add(property)
                clsPropertySetMap.put(cls, propertySet)
            }
          }
        }
      }
    }

    for (entry <- clsPropertySetMap.entrySet()) {
      for (property <- entry.getValue()) {
        writer.write(entry.getKey() + "\t" + property)
        writer.newLine()
        println(entry.getKey() + "\t" + property)
      }
      cntWriter.write(entry.getKey() + "," + entry.getValue().size)
      cntWriter.newLine()
      println(entry.getKey() + "\t" + entry.getValue().size)
    }
    writer.close
    cntWriter.close

    println("プロパティを持つクラス数: " + clsPropertySetMap.keySet.size)
  }
}