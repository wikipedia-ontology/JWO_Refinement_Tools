package test

import scala.io.Source
import scala.collection.mutable.ListBuffer
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.FileOutputStream
object ErrorCheck {
  def main(args: Array[String]) {
    val classList = ListBuffer[String]()
    for (c <- Source.fromFile("merged-class-list.txt").getLines) {
      classList += c
    }
    println(classList.size)
    val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("class-instance-refinement-results-new.txt"), "UTF-8"))
    for (line <- Source.fromFile("class-instance-refinement-results.txt").getLines) {
      val orgCls = line.split("\t")(1)
      if (classList.contains(orgCls)) {
        println(line)
        writer.write(line)
        writer.newLine
      }
    }
    writer.close
  }
}