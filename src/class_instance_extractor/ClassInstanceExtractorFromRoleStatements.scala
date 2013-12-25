package class_instance_extractor

import com.hp.hpl.jena.rdf.model.ResourceFactory
import com.hp.hpl.jena.util.FileManager
import java.io._
import scala.collection.JavaConversions._
import scala.collection.mutable.Map
import scala.collection.mutable.ListBuffer
import com.hp.hpl.jena.vocabulary.OWL
import scala.io.Source

object ClassInstanceExtractorFromRoleStatements {
  def main(args: Array[String]) {
    val inputText = "inputs_and_outputs/tests/role_statements.txt"
    val outputText1 =  "inputs_and_outputs/tests/role-property-class.txt"
    val outputText2 =  "inputs_and_outputs/tests/class-instance_from_role_statements.txt"
    val outputCSV = "inputs_and_outputs/tests/role-property-class-cnt.csv"
    val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputText1), "UTF-8"))
    val writer2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputText2), "UTF-8"))
    val cntWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputCSV), "SJIS"))

    val rolePropertyClsListMap = Map[String, ListBuffer[String]]()
    val file = new File(inputText)
    val source = Source.fromFile(file, "utf-8")
    for (line <- source.getLines()) {
      println(line)
      val Array(instance, property, cls) = line.split("\t")
      if (property == "種別" || property == "ジャンル" || property == "業種" || property == "職業" || property == "種類") {
          writer2.write(instance + "\t" + cls)
          writer2.newLine()
      }
      rolePropertyClsListMap.get(property) match {
        case Some(list) => list.add(cls)
        case None =>
          val list = ListBuffer[String]()
          list.add(cls)
          rolePropertyClsListMap.put(property, list)
      }
    }
    for (entry <- rolePropertyClsListMap.entrySet()) {
      val list = entry.getValue().toSet
      for (cls <- list) {
        writer.write(entry.getKey() + "\t" + cls)
        writer.newLine()
      }
      cntWriter.write(entry.getKey() + "," + entry.getValue().size)
      cntWriter.newLine()
    }

    writer.close
    writer2.close
    cntWriter.close
  }
}