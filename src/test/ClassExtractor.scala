package test

import java.io.File
import scala.io.Source
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.FileOutputStream
import scala.collection.mutable.ListBuffer

object ClassExtractor {
  def main(args: Array[String]) {
    val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("class-list.txt"), "UTF-8"))
    var file = new File("class-instance-remove-highbranch.txt")
    val clsList = ListBuffer[String]()
    var source = Source.fromFile(file, "utf-8")
    for (line <- source.getLines()) {
      //println(line)
      if (line.split("\t").size == 2) {
        val cls = line.split("\t")(1)
        clsList += cls
      }
    }
    /*
    println(clsList.toSet.size)
    val file2 = new File("class-instance_from_triples.txt")
    val source2 = Source.fromFile(file2, "utf-8")
    for (line <- source2.getLines()) {
      val cls = line.split("\t")(1)
      clsList += cls
    }
    */
    println(clsList.toSet.size)
    for (cls: String <- clsList.toSet) {
      writer.write(cls)
      writer.newLine()
    }
    writer.close
  }
}