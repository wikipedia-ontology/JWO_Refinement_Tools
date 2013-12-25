package jwo_jwn_alignment_tool

import scala.collection.mutable.ListBuffer

object StringSimilarityUtil {

  /**
   * N-Gramセットを返す
   */
  def getNgramSet(n: Int, term: String): scala.collection.immutable.Set[String] = {
    val ngramArray = ListBuffer[String]()
    for (i <- 0 to term.size - n) {
      ngramArray += term.substring(i, i + n)
    }
    ngramArray.toSet
  }

  /**
   * N-Gramを用いた文字列類似度
   */
  def calcNgramSimilarity(n: Int, x: String, y: String): Array[Double] = {
    val xNgramSet = getNgramSet(n, x)
    val yNgramSet = getNgramSet(n, y)
    val jaccardIndex = SetSimilarityUtil.calcJaccardIndex(xNgramSet, yNgramSet)
    val diceCoefficient = SetSimilarityUtil.calcDiceCoefficient(xNgramSet, yNgramSet)
    val simpsonCoefficient = SetSimilarityUtil.calcSimpsonCoefficient(xNgramSet, yNgramSet)
    Array(jaccardIndex, diceCoefficient, simpsonCoefficient)
  }

  /**
   * プレフィックスで照合した文字数
   */
  def calcPrefixOverlap(x: String, y: String): Double = {
    if (x.size == 0 || y.size == 0) {
      return 0.0
    }
    val len = x.size.min(y.size)
    var index = 0.0
    for (i <- 0 until len) {
      if (x(i) != y(i)) {
        return index
      }
      index += 1
    }
    return index
  }

  def calcPrefixOrSuffixSimilarity(overlap: Double, x: String, y: String): Array[Double] = {
    val jaccardIndex = SetSimilarityUtil.calcJaccardIndex(overlap, x.toCharArray.toSet, y.toCharArray.toSet)
    val diceCoefficient = SetSimilarityUtil.calcDiceCoefficient(overlap, x.toCharArray.toSet, y.toCharArray.toSet)
    val simpsonCoefficient = SetSimilarityUtil.calcSimpsonCoefficient(overlap, x.toCharArray.toSet, y.toCharArray.toSet)
    Array(jaccardIndex, diceCoefficient, simpsonCoefficient)
  }

  /**
   * プレフィックスを用いた文字列類似度
   */
  def calcPrefixSimilarity(x: String, y: String): Array[Double] = {
    val prefixOverlap = calcPrefixOverlap(x, y)
    calcPrefixOrSuffixSimilarity(prefixOverlap, x, y)
  }

  /**
   * サフィックスを用いた文字列類似度
   */
  def calcSuffixSimilarity(x: String, y: String): Array[Double] = {
    val suffixOverlap = calcPrefixOverlap(x.reverse, y.reverse)
//    if (1 < suffixOverlap) {
//      println(x.reverse + ": " + y.reverse + ": " + suffixOverlap)
//    }
    calcPrefixOrSuffixSimilarity(suffixOverlap, x, y)
  }

  /**
   * 編集距離を用いた類似度
   */
  def calcLevenshteinDistanceSimilarity(x: String, y: String): Double = {
    val levenshteinDistance = calcLevenshteinDistance(x, y)
    if (x.size < levenshteinDistance) {
      return 0
    }
    1 - levenshteinDistance.toDouble / x.size.toDouble
  }

  /**
   * 編集距離を求める関数
   */
  def calcLevenshteinDistance(x: String, y: String): Int = {

    def min(a: Int, b: Int, c: Int): Int = {
      a.min(b).min(c)
    }

    val n = x.length
    val m = y.length
    if (n == 0) {
      return m
    }
    if (m == 0) {
      return n
    }
    val d = new Array[Array[Int]](n + 1)
    for (i <- 0 to n) {
      d(i) = new Array[Int](m + 1)
    }

    for (i <- 0 to n) {
      d(i)(0) = i
    }

    for (i <- 0 to m) {
      d(0)(i) = i
    }
    var cost = 0
    for (i <- 1 to n) {
      var xi = x(i - 1)
      for (j <- 1 to m) {
        var yj = y(j - 1)
        if (xi == yj) {
          cost = 0
        } else {
          cost = 1
        }
        d(i)(j) = min(d(i - 1)(j) + 1, d(i)(j - 1) + 1, d(i - 1)(j - 1) + cost)
      }

    }
    return d(n)(m)
  }

}