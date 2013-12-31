package ontology_builder 

import com.hp.hpl.jena.rdf.model.ResourceFactory
import com.hp.hpl.jena.util.FileManager
import java.io._
import scala.collection.JavaConversions._
import scala.collection.mutable.Map
import scala.collection.mutable.Set
import com.hp.hpl.jena.vocabulary.OWL
import scala.collection.mutable.ListBuffer
import org.scalaquery.session.Database
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.ql.basic.BasicTable
import org.scalaquery.ql.basic.BasicDriver.Implicit._
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Set
import scala.collection.mutable.Map
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.FileOutputStream
import scala.io.Source
import com.hp.hpl.jena.tdb.TDBFactory
import com.hp.hpl.jena.rdf.model.ResourceFactory
import class_instance_extractor.ClassInstanceList

object ConvertSqliteToTDB {
  def main(args: Array[String]) {
    val ontology = FileManager.get().loadModel("merged_ontology_20120316.owl")
    val model = FileManager.get().loadModel("wikipediaontology_instance_20101114ja.rdf")
    val jwoInstanceNs = "http://www.yamaguti.comp.ae.keio.ac.jp/wikipedia_ontology/instance/"
    val rdfType = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
    val classInstanceDB = Database.forURL(url = "jdbc:sqlite:refined_class_instance_list_removing_redundant_type.db", driver = "org.sqlite.JDBC") // 最新版はtype2の方なので，作りなおす必要あり
    val directory = "refined_jwo_tdb";
    val tdbModel = TDBFactory.createModel(directory);
    tdbModel.add(ontology)
    println("init")
    classInstanceDB withSession {
      val q = for { result <- ClassInstanceList }
        yield result.jwoClass ~ result.jwoInstance
        var cnt = 0
      for ((cls, jwoInstance) <- q.list) {
        val classRes = ResourceFactory.createResource(cls)
        val instanceRes = ResourceFactory.createResource(jwoInstanceNs + jwoInstance)
        tdbModel.add(instanceRes, rdfType, classRes)
        for (stmt <- model.listStatements(instanceRes, null, null).toList()) {
          tdbModel.add(stmt)
          cnt += 1
          if (cnt % 10000 == 0) {
            println(cnt)
          }
        }
      }
    }
    tdbModel.close
  }
}