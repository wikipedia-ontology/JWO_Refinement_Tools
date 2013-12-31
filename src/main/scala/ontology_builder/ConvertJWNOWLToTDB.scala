import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.tdb.TDBFactory
import com.hp.hpl.jena.util.FileUtils
import com.hp.hpl.jena.util.FileManager
import java.io.File
import com.hp.hpl.jena.rdf.model.ResourceFactory
import com.hp.hpl.jena.vocabulary.RDFS
import scala.collection.JavaConversions._
import data.JWNSynset
import scala.collection.mutable.ListBuffer
import data.JWNSynset
import scala.collection.mutable.Map
import scala.collection.mutable.Set
import com.hp.hpl.jena.rdf.model.Statement
import com.hp.hpl.jena.rdf.model.RDFNode

/**
 * 3. Aligning JWO classes and JWN synsets
 * 3-3. ontology_builder.ConvertJWNOWLToTDB.scala
 * - Inputs
 * -- ontologies/JPNWN1.1.owl
 * -- ontologies/JPNWN1.1_tree.owl
 * - Output
 * -- ontologies/jwn1.1_tdb
 */
object ConvertJWNOWLToTDB {

  val directory = "ontologies/jwn1.1_tdb";
  val model = TDBFactory.createModel(directory);

  def convert() = {
    val JPNWNFile = "ontologies/JPNWN1.1.owl"
    val JPNWNTreeFile = "ontologies/JPNWN1.1_tree.owl"
    val f = new File(JPNWNFile)
    val jwnModel = FileManager.get().loadModel(f.getAbsolutePath)
    model.add(jwnModel)
    val f2 = new File(JPNWNTreeFile)
    val jwnTreeModel = FileManager.get().loadModel(f2.getAbsolutePath)
    model.add(jwnTreeModel)
    model.close()
  }

  def getJWNSynset(id: String): JWNSynset = {
    val res = ResourceFactory.createResource(id)
    val synsetList = ListBuffer[String]()
    for (stmt <- model.listStatements(res, RDFS.label, null).toList) {
      val label = stmt.getObject().asLiteral()
      if (label.getLanguage() == "ja") {
        synsetList.add(label.getString())
        //        println("ja: " + label.getString())
      } else if (label.getLanguage() == "en") {
        synsetList.add(label.getString())
        //        println("en: " + label.getString())
      }
    }
    var enDescription = ""
    var jaDescription = ""
    for (stmt <- model.listStatements(res, RDFS.comment, null).toList) {
      val label = stmt.getObject().asLiteral()
      if (label.getLanguage() == "ja") {
        jaDescription = label.getString()
        //        println("ja: " + label.getString())
      } else if (label.getLanguage() == "en") {
        enDescription = label.getString()
        //        println("en: " + label.getString())
      }
    }
    model.close()
    return new JWNSynset(id, synsetList.toList, enDescription, jaDescription)
  }

  def getJWNSubTree(id: String) = {
    val res = ResourceFactory.createResource(id)
    val targetJWNSynset = getJWNSynset(id)
    val supClassList = Set[JWNSynset]()
    val siblingClassList = Set[JWNSynset]()
    val subClassList = Set[JWNSynset]()
    for (stmt <- model.listStatements(res, RDFS.subClassOf, null).toList) {
      val supRes = stmt.getObject().asResource()
      val supJWNSynset = getJWNSynset(supRes.getURI())
      supClassList.add(supJWNSynset)
      for (stmt2 <- model.listStatements(null, RDFS.subClassOf, supRes).toList) {
        val siblingRes = stmt2.getSubject()
        val siblingJWNSynset = getJWNSynset(siblingRes.getURI())
        if (siblingJWNSynset.id != targetJWNSynset.id) {
          siblingClassList.add(siblingJWNSynset)
        }
      }
    }
    for (stmt <- model.listStatements(null, RDFS.subClassOf, res).toList) {
      val subRes = stmt.getObject().asResource()
      val subJWNSynset = getJWNSynset(subRes.getURI())
      subClassList.add(subJWNSynset)
    }
    println(supClassList)
    println(siblingClassList)
    println(subClassList)
  }

  def main(args: Array[String]) {
    convert()
    val jwnSynset = getJWNSynset("http://nlpwww.nict.go.jp/wn-ja/11375418-n")
    println(jwnSynset)
    getJWNSubTree("http://nlpwww.nict.go.jp/wn-ja/00007846-n")
  }
}