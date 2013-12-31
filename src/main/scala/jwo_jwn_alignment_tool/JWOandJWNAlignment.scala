package jwo_jwn_alignment_tool

import java.io.BufferedWriter
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.Calendar

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import scala.io.Source

/**
 * 3. Aligning JWO classes and JWN synsets
 * 3-2. jwo_jwn_alignment_tool.JWOandJWNAlignment.scala
 * - Inputs
 * -- inputs_and_outputs/alignment-target-class-list.txt
 * -- inputs_and_outputs/jpwn1.1_synonyms_ja.txt
 * - Output
 * -- inputs_and_outputs/calculating_jwo_jwn_similarity_results.txt
 */
class JWOandJWNAlignment(jwnSynsetList: ListBuffer[List[String]], val jwoCls: String) {
  val prefixSimilaritySynsetMap: Map[(String, Double), ListBuffer[List[String]]] = Map()
  val suffixSimilaritySynsetMap: Map[(String, Double), ListBuffer[List[String]]] = Map()
  val ngramSimilaritySynsetMap: Map[(String, Double), ListBuffer[List[String]]] = Map()
  val editDistanceSimilaritySynsetMap: Map[(String, Double), ListBuffer[List[String]]] = Map()
  val JWOandJWNSimilarityResults = "inputs_and_outputs/calculating_jwo_jwn_similarity_results.txt"
  val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(JWOandJWNSimilarityResults, true), "UTF-8"))

  /**
   * Prefix, Suffix, 編集距離, N-Gramの中で最も類似度の高い値を返す（集合類似度は、jaccard係数を用いる)
   */
  def getMaxTermSimilarity(jpwnTerm: String): Double = {
    val prefixSimilarities = StringSimilarityUtil.calcPrefixSimilarity(jwoCls, jpwnTerm)
    val suffixSimilarities = StringSimilarityUtil.calcSuffixSimilarity(jwoCls, jpwnTerm)
    val levenshteinDistanceSimilarity = StringSimilarityUtil.calcLevenshteinDistanceSimilarity(jwoCls, jpwnTerm)
    val ngramSimilarities = StringSimilarityUtil.calcNgramSimilarity(3, jwoCls, jpwnTerm)
    // 後方文字列照合の重みを5倍にする
    Array(prefixSimilarities(0), suffixSimilarities(0) * 5, levenshteinDistanceSimilarity, ngramSimilarities(0)).reduceLeft(_ max _)
  }

  for (jwnSynset <- jwnSynsetList) {
    var prefixMax = 0.0
    var suffixMax = 0.0
    var ngramMax = 0.0
    var editDistanceMax = 0.0
    for (jwnTerm <- jwnSynset.tail) {
      prefixMax = Math.max(prefixMax, StringSimilarityUtil.calcPrefixSimilarity(jwoCls, jwnTerm)(0))
      suffixMax = Math.max(suffixMax, StringSimilarityUtil.calcSuffixSimilarity(jwoCls, jwnTerm)(0))
      ngramMax = Math.max(ngramMax, StringSimilarityUtil.calcNgramSimilarity(3, jwoCls, jwnTerm)(0))
      editDistanceMax = Math.max(editDistanceMax, StringSimilarityUtil.calcLevenshteinDistanceSimilarity(jwoCls, jwnTerm))
    }
    setPrefixSimilaritySynsetMap(jwnSynset, prefixMax)
    setSuffixSimilaritySynsetMap(jwnSynset, suffixMax)
    setNgramSimilaritySynsetMap(jwnSynset, ngramMax)
    setEditDistanceSimilaritySynsetMap(jwnSynset, editDistanceMax)
  }

  def setSimilaritySynsetMap(similaritySynsetMap: Map[(String, Double), ListBuffer[List[String]]], jwnSynset: List[String], similarity: Double) = {
    val key = (jwnSynset.head, similarity)
    similaritySynsetMap.get(key) match {
      case Some(list) => list += jwnSynset
      case None => similaritySynsetMap(key) = ListBuffer(jwnSynset)
    }
  }

  def setPrefixSimilaritySynsetMap(jwnSynset: List[String], similarity: Double) = {
    setSimilaritySynsetMap(prefixSimilaritySynsetMap, jwnSynset, similarity)
  }

  def setSuffixSimilaritySynsetMap(jwnSynset: List[String], similarity: Double) = {
    setSimilaritySynsetMap(suffixSimilaritySynsetMap, jwnSynset, similarity)
  }

  def setNgramSimilaritySynsetMap(jwnSynset: List[String], similarity: Double) = {
    setSimilaritySynsetMap(ngramSimilaritySynsetMap, jwnSynset, similarity)
  }

  def setEditDistanceSimilaritySynsetMap(jwnSynset: List[String], similarity: Double) = {
    setSimilaritySynsetMap(editDistanceSimilaritySynsetMap, jwnSynset, similarity)
  }

  def getSimilarityResults(similaritySynsetMap: Map[(String, Double), ListBuffer[List[String]]], num: Integer, method: String): ListBuffer[AlignmentResult] = {
    val alignmentResultList = ListBuffer[AlignmentResult]()
    for (key <- similaritySynsetMap.keySet.toList.sortBy { case (jpwnId, sim) => (-sim, jpwnId) }.slice(0, num)) {
      alignmentResultList += new AlignmentResult(jwoCls, key._1, key._2, method)
    }
    return alignmentResultList
  }

  def getPrefixSimilarityResults(num: Integer, method: String): ListBuffer[AlignmentResult] = {
    return getSimilarityResults(prefixSimilaritySynsetMap, num, method)
  }

  def getSuffixSimilarityResults(num: Integer, method: String): ListBuffer[AlignmentResult] = {
    return getSimilarityResults(suffixSimilaritySynsetMap, num, method)
  }

  def getNgramSimilarityResults(num: Integer, method: String): ListBuffer[AlignmentResult] = {
    return getSimilarityResults(ngramSimilaritySynsetMap, num, method)
  }

  def getEditDistanceSimilarityResults(num: Integer, method: String): ListBuffer[AlignmentResult] = {
    return getSimilarityResults(editDistanceSimilaritySynsetMap, num, method)
  }

  def getAlignmentResultList(num: Integer): ListBuffer[AlignmentResult] = {
    val alignmentResultList = ListBuffer[AlignmentResult]()
    alignmentResultList ++= getPrefixSimilarityResults(num, "Prefix")
    alignmentResultList ++= getSuffixSimilarityResults(num, "Suffix")
    alignmentResultList ++= getNgramSimilarityResults(num, "N-Gram")
    alignmentResultList ++= getEditDistanceSimilarityResults(num, "Edit Distance")
    return alignmentResultList
  }

  def writeResults(num: Integer) = {
    for (result <- getAlignmentResultList(num)) {
      println(result.toString())
      writer.write(result.toString())
      writer.newLine()
    }
    writer.close()
  }
}

class AlignmentResult(val jwoCls: String, val jwnSynset: String, val similarity: Double, val method: String) {
  override def toString(): String = {
    return jwoCls + "," + jwnSynset + "," + similarity + "," + method
  }
}

object JWOandJWNAlignment {

  def main(args: Array[String]) {
    val alignmentTargetClassListFile = "inputs_and_outputs/alignment-target-class-list.txt"
    val jwnSynsetsFile = "inputs_and_outputs/jpwn1.1_synonyms_ja.txt"

    val jwoClassList: ListBuffer[String] = ListBuffer()
    for (cls <- Source.fromFile(alignmentTargetClassListFile).getLines) {
      jwoClassList += cls
    }

    val jwnSynsetList: ListBuffer[List[String]] = ListBuffer()
    for (line <- Source.fromFile(jwnSynsetsFile).getLines) {
      jwnSynsetList += line.split(",").toList
    }
    var count = 1
    for (jwoCls <- jwoClassList) {
      println(count + ": " + jwoCls + ": " + Calendar.getInstance.getTime)
      count += 1
      val similaritySynset = new JWOandJWNAlignment(jwnSynsetList, jwoCls)
      similaritySynset.writeResults(10)
    }
    //    val similaritySynset = new JWOandJWNAlignment(jwnSynsetList, "推理作家")
    //    similaritySynset.writeResults(5)
  }
}