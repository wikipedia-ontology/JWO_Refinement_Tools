package test

import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import scala.collection.mutable.Map
import scala.collection.JavaConversions._
import scala.io.Source
import net.java.sen.SenFactory
import net.java.sen.StringTagger
import net.java.sen.dictionary.Token
object RemoveHighBranchClasses {

  def getClassRemovingHighBranch(orgCls: String): String = {
    val tagger: StringTagger = SenFactory.getStringTagger(null);
    val tokens: java.util.List[Token] = tagger.analyze(orgCls);
    var newCls = ""
    var prevSurface = ""
    for (token <- tokens) {
      //      println(token.getSurface())
      //      println(token.getMorpheme())
      val surface = token.getSurface()
      if (surface == "の" || (prevSurface == "東京" && surface == "都") || surface == "北海道" ||
        ((prevSurface == "大阪" || prevSurface == "京都") && surface == "府") || surface == "県") {
        newCls = ""
      } else {
        newCls += token.getSurface()
      }
      prevSurface = surface
    }
    newCls = newCls.split("（")(0)
    //    println(orgCls + " -> " + newCls)
    return newCls
  }

  def write(clsInstanceCntMap: Map[String, Int], writer: BufferedWriter, orgCls: String, instance: String) = {
    var cls = getClassRemovingHighBranch(orgCls)
    clsInstanceCntMap.get(cls) match {
      case Some(cnt) => clsInstanceCntMap.put(cls, cnt + 1)
      case None => clsInstanceCntMap.put(cls, 1)
    }
    writer.write(instance + "\t" + cls)
    writer.newLine()
    //      println(instance + "\t" + cls)
  }

  def main(args: Array[String]) {
    //    getClassRemovingHighBranch("CEROレーティング18才以上のみ対象ソフト(test)".replaceAll("\\(", "（"))
    //    getClassRemovingHighBranch("大阪大学の人物".replaceAll("\\(", "（"))
    //    exit(0)
    val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("class-instance-remove-highbranch.txt"), "UTF-8"))
    val cntWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("class-instance-cnt-remove-highbranch.csv"), "SJIS"))
    val clsInstanceCntMap = Map[String, Int]()
    val file = new File("class-instance.txt")
    val source = Source.fromFile(file, "utf-8")
    for (line <- source.getLines()) {
      val Array(instance, orgCls) = line.replaceAll("\\(", "（").split("\t")
      write(clsInstanceCntMap, writer, orgCls, instance)
    }
    // 重複が発生する可能性があるので、ちゃんと、セットでクラスーインスタンス関係を調べた方が良い
    val file2 = new File("class-instance_from_triples.txt")
    val source2 = Source.fromFile(file2, "utf-8")
    for (line <- source2.getLines()) {
      val Array(instance, orgCls) = line.replaceAll("\\(", "（").split("\t")
      write(clsInstanceCntMap, writer, orgCls, instance)
    }

    for (entry <- clsInstanceCntMap.entrySet()) {
      cntWriter.write(entry.getKey() + "," + entry.getValue())
      cntWriter.newLine()
      //      println(entry.getKey() + "\t" + entry.getValue())
    }
    cntWriter.close

    println("インスタンスを持つクラス数: " + clsInstanceCntMap.keySet.size)
  }
}