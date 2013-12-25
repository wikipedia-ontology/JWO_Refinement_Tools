import scala.collection.mutable.ListBuffer
import scala.io.Source
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.FileOutputStream
object AnalyzeAlignmentExperiments {
  def main(args: Array[String]) {
    val clsList = ListBuffer[String]()
    var eqCnt = 0
    var eqmCnt = 0
    var isaCnt = 0
    var isamCnt = 0
    var noCnt = 0
    for (line <- Source.fromFile("alignment_results_20120306.txt").getLines) {
      val Array(jwo, jwn, rel) = line.split("\t")
      rel match {
        case "同値関係" => eqCnt += 1
        case "Is-a関係" => isaCnt += 1
        case "同値関係（手動）" => eqmCnt += 1
        case "Is-a関係（手動）" => isamCnt += 1
        case "不明" => noCnt += 1
      }
    }
    println(eqCnt)
    println(eqmCnt)
    println(isaCnt)
    println(isamCnt)
    println(noCnt)
  }
}