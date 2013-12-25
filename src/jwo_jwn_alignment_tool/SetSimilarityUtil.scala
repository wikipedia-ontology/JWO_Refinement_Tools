
package jwo_jwn_alignment_tool

object SetSimilarityUtil {

  /**
   * jaccard index 
   * 集合XとYの和集合に対する積集合の割合 
   */
  def calcJaccardIndex(X: Set[String], Y: Set[String]): Double = {  
    (X & Y).size.toDouble / (X | Y).size.toDouble
  }
    
  /**
   * dice coefficient
   * 集合XとYの共通要素数を各集合の要素数の平均で割ったもの
   */
  def calcDiceCoefficient(X: Set[String], Y: Set[String]): Double = {
    2 * (X & Y).size.toDouble / (X.size + Y.size).toDouble
  }

  /**
   * simpson coefficient
   * 集合XとYの共通要素数を各集合の要素数の最小値で割ったもの
   */
  def calcSimpsonCoefficient(X: Set[String], Y: Set[String]): Double = {
    (X & Y).size.toDouble / Math.min(X.size, Y.size).toDouble
  }

  /**
   * jaccard index 
   * 集合XとYの和集合に対する積集合の割合 (積集合の要素数を第一引数に与えた場合） 
   */
  def calcJaccardIndex(XandY: Double, X: Set[Char], Y: Set[Char]): Double = {
    XandY / (X | Y).size.toDouble
  }

  /**
   * dice coefficient
   * 集合XとYの共通要素数を各集合の要素数の平均で割ったもの (積集合の要素数を第一引数に与えた場合） 
   */
  def calcDiceCoefficient(XandY: Double, X: Set[Char], Y: Set[Char]): Double = {
    2 * XandY / (X.size + Y.size).toDouble
  }

  /**
   * simpson coefficient
   * 集合XとYの共通要素数を各集合の要素数の最小値で割ったもの (積集合の要素数を第一引数に与えた場合） 
   */
  def calcSimpsonCoefficient(XandY: Double, X: Set[Char], Y: Set[Char]): Double = {
    XandY / Math.min(X.size, Y.size).toDouble
  }

}