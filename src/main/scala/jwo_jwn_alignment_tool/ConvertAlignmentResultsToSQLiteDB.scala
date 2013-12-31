package jwo_jwn_alignment_tool

import org.scalaquery.session.Database
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.ql.basic.BasicTable
import org.scalaquery.ql.basic.BasicDriver.Implicit._
import java.sql.Timestamp
import java.util.Date
import java.io.File
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.collection.mutable.Map

object AlignmentResults extends BasicTable[(String, String, String, String)]("AlignmentResultsTable") {
  def jwoClass = column[String]("jwoClass")
  def jwnSynsetID = column[String]("jwnSynsetID")
  def similarity = column[String]("similarity")
  def method = column[String]("method")

  def * = jwoClass ~ jwnSynsetID ~ similarity ~ method
}

object ConvertAlignmentResultsToSQLiteDB {
  val database = Database.forURL(url = "jdbc:sqlite:alignment_results.db", driver = "org.sqlite.JDBC")

  def convertTextToSQLiteDB() {
    database withSession {
      (AlignmentResults.ddl).create

      var file = new File("jwo_jwn_alignment_results_20120302.txt")
      var source = Source.fromFile(file, "utf-8")
      for (line <- source.getLines()) {
        val Array(jwoClass, jwnSynsetID, similarity, method) = line.split(",")
        println(jwoClass + "," + jwnSynsetID + "," + similarity + "," + method)
        AlignmentResults.insert(jwoClass, jwnSynsetID, similarity, method)
      }
    }
  }

  def main(args: Array[String]) {
    convertTextToSQLiteDB()
    val database = Database.forURL(url = "jdbc:sqlite:alignment_results.db", driver = "org.sqlite.JDBC")
    database withSession {
      val referedClass = "人物"
      val q = for { result <- AlignmentResults if result.jwoClass === referedClass }
        yield result.jwoClass ~ result.jwnSynsetID ~ result.similarity ~ result.method
      for ((jwoClass, jwnSynsetID, similarity, method) <- q.list) {
        println(jwoClass + "," + jwnSynsetID + "," + similarity + "," + method)
      }
    }
  }
}
