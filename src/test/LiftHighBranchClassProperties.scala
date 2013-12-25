package test

import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import scala.collection.mutable.Map
import scala.collection.mutable.Set
import scala.collection.JavaConversions._
import scala.io.Source

object LiftHighBranchClassProperties {
  def main(args: Array[String]) {
    val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("class-property-remove-highbranch.txt"), "UTF-8"))
    val cntWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("class-property-cnt-remove-highbranch.csv"), "SJIS"))
    val clsPropertySetMap = Map[String, Set[String]]()

    val file = new File("class-property.txt")
    val source = Source.fromFile(file, "utf-8")
    for (line <- source.getLines()) {
      val Array(clsOld, property) = line.split("\t")
      var cls = clsOld
      if (clsOld.split("の").size == 2) {
        cls = clsOld.split("の")(1)
      }
      clsPropertySetMap.get(cls) match {
        case Some(list) => list.add(property)
        case None =>
          val propertySet = Set[String]()
          propertySet.add(property)
          clsPropertySetMap.put(cls, propertySet)
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