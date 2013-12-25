package property_elevator
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.asScalaIterator
import scala.collection.JavaConversions.mutableMapAsJavaMap
import scala.collection.mutable.Map
import scala.collection.mutable.Set
import org.scalaquery.ql.basic.BasicDriver.Implicit.columnBaseToInsertInvoker
import org.scalaquery.ql.basic.BasicDriver.Implicit.queryToQueryInvoker
import org.scalaquery.ql.basic.BasicDriver.Implicit.scalaQueryDriver
import org.scalaquery.ql.basic.BasicDriver.Implicit.tableToQuery
import org.scalaquery.ql.basic.BasicTable
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.session.Database
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.rdf.model.ResourceFactory
import com.hp.hpl.jena.util.FileManager
import com.hp.hpl.jena.vocabulary.RDFS
import class_property_extractor.ClassPropertyList
import scala.collection.mutable.ListBuffer

object ClassPropertyWithLabelList extends BasicTable[(String, String, String, Int)]("ClassPropertyWithLabelListTable") {
  def jwoClass = column[String]("jwoClass")
  def jwoProperty = column[String]("jwoProperty")
  def label = column[String]("label")
  def depthOfClass = column[Int]("depthOfClass")

  def * = jwoClass ~ jwoProperty ~ label ~ depthOfClass
}

case class JWOProperty(property: String, var usageCount: Int, var count: Int, var label: String)

case class PromotingProgressInfo(var bnCompleteness: Int, var bnCandidate: Int, var bnDefault: Int, var pnCompleteness: Int, var pnCandidate: Int, var anCompleteness: Int, var anCandidate: Int, var anDefault: Int) {
  /**
   * bnCompleteness: プロモート前のcompletenessラベルがついたプロパティ定義数
   * bnCandidate: プロモート前のcandidateラベルがついたプロパティ定義数
   * bnDefault: プロモート前のdefaultラベルがついたプロパティ定義数
   * pnCompleteness: completenessラベル付きでプロモートされたプロパティ数
   * pnCandidate: candidateラベル付きでプロモートされたプロパティ数
   * anCompleteness: プロモート後のcompletenessラベルがついたプロパティ定義数
   * anCandidate: プロモート後のcandidateラベルがついたプロパティ定義数
   * anDefault:  プロモート後のdefaultラベルがついたプロパティ定義数
   *
   */
  override def toString: String = {
    bnCompleteness + "," + bnCandidate + "," + bnDefault + "," + pnCompleteness + "," + pnCandidate + "," + anCompleteness + "," + anCandidate + "," + anDefault
  }
}

/**
 * 6. Defining the domains of properties based on a consideration of property inheritance
 * 6-2. property_elevator.PropertyElevator.scala
 * - Inputs
 * -- ontologies/merged_ontology_20130912.owl
 * -- inputs_and_outputs/class_property_list_with_count.db
 * - Outputs
 * -- inputs_and_outputs/class_elevated_property_list_with_label_and_depth.db
 * -- inputs_and_outputs/class_elevated_property_list_with_label_and_depth_removing_inherited_properties.db
 */
object PropertyElevator {
  val depthPPInfoMap = Map[Int, PromotingProgressInfo]() // 深さ毎のプロモート状況を保存
  for (depth <- 0 to 13) {
    depthPPInfoMap.put(depth, PromotingProgressInfo(0, 0, 0, 0, 0, 0, 0, 0))
  }

  def makeClassPropertySetMap: Map[Resource, Set[JWOProperty]] = {
    val classPropertySetMap = Map[Resource, Set[JWOProperty]]()
    val classPropertyListDB = Database.forURL(url = "jdbc:sqlite:inputs_and_outputs/class_property_list_with_count.db", driver = "org.sqlite.JDBC") // リフト前のクラスとプロパティの対応を読み込む
    classPropertyListDB withSession {
      val q = for { result <- ClassPropertyList }
        yield result.jwoClass ~ result.jwoProperty ~ result.jwoPropertyCount
      for ((jwoClass, jwoProperty, jwoPropertyCount) <- q.list) {
        val property = JWOProperty(jwoProperty, jwoPropertyCount, 1, "default")
        val jwoClassRes = ResourceFactory.createResource(jwoClass)
        classPropertySetMap.get(jwoClassRes) match {
          case Some(propertySet) => propertySet.add(property)
          case None =>
            val propertySet = Set(property)
            classPropertySetMap.put(jwoClassRes, propertySet)
        }
      }
    }
    return classPropertySetMap
  }

  def printJWOandJWNClassCount(ontology: Model) = {
    var jwoClassCount = 0
    var jwnClassCount = 0
    val classSet = Set[String]()
    for (stmt <- ontology.listStatements(null, RDFS.subClassOf, null).toList()) {
      val supClass = stmt.getSubject().getURI()
      val subClass = stmt.getObject().asResource().getURI()
      classSet += supClass
      classSet += subClass
    }
    for (c <- classSet) {
      if (c.contains("wn-ja")) {
        jwnClassCount += 1
      } else {
        jwoClassCount += 1
      }
    }
    println("JWOクラス数：" + jwoClassCount)
    println("JWNクラス数：" + jwnClassCount)
    println("全クラス数：" + (jwoClassCount + jwnClassCount))
  }

  def extractLeafClassSet(ontology: Model): Set[Resource] = {
    val leafClassSet = Set[Resource]()
    for (stmt <- ontology.listStatements(null, RDFS.subClassOf, null).toList()) {
      val subClass = stmt.getSubject()
      if (ontology.listStatements(null, RDFS.subClassOf, subClass).toList().size() == 0) {
        leafClassSet.add(subClass)
      }
      //println(subClass + " subClassOf " + supClass)
    }
    return leafClassSet
  }

  def getMaxDepthClassSet(classSet: Set[Resource], classDepthMap: Map[Resource, Int]): (Int, Set[Resource]) = {
    var maxDepth = 0
    for (c <- classSet) {
      classDepthMap.get(c) match {
        case Some(depth) =>
          if (maxDepth < depth) {
            maxDepth = depth
          }
        case None =>
      }
    }
    val maxDepthClassSet = Set[Resource]()
    for (c <- classSet) {
      classDepthMap.get(c) match {
        case Some(depth) =>
          if (depth == maxDepth) {
            maxDepthClassSet.add(c)
          }
        case None =>
      }
    }
    //println("maxdepth: " + maxDepth)
    //println(classSet.size + ": " + classSet)
    //println(maxDepthClassSet.size + ": " + maxDepthClassSet)
    return (maxDepth, maxDepthClassSet)
  }

  def countPropertyLabel(clsSet: Set[Resource], ppInfo: PromotingProgressInfo, classPropertySetMap: Map[Resource, Set[JWOProperty]]) = {
    for (c <- clsSet) {
      classPropertySetMap.get(c) match {
        case Some(propertySet) =>
          for (p <- propertySet) {
            p.label match {
              case "completeness" => ppInfo.bnCompleteness += 1
              case "candidate" => ppInfo.bnCandidate += 1
              case "default" => ppInfo.bnDefault += 1
            }
          }
        case None =>
      }
    }
  }

  def promoteProperties(classSet: Set[Resource], ontology: Model, classPropertySetMap: Map[Resource, Set[JWOProperty]], classDepthMap: Map[Resource, Int]): Int = {
    if (classSet.size == 0) {
      return 0
    }
    val (depth, maxDepthClassSet) = getMaxDepthClassSet(classSet, classDepthMap)
    val ppInfo = depthPPInfoMap.getOrElse(depth, PromotingProgressInfo(0, 0, 0, 0, 0, 0, 0, 0))
    countPropertyLabel(maxDepthClassSet, ppInfo, classPropertySetMap)
    var supClassSet = Set[Resource]()
    supClassSet ++= classSet -- maxDepthClassSet
    val classSubClassSetMap = Map[Resource, Set[Resource]]()
    for (c <- maxDepthClassSet) {
      for (supClass <- ontology.listObjectsOfProperty(c, RDFS.subClassOf)) {
        classSubClassSetMap.get(supClass.asResource()) match {
          case Some(subClassSet) => subClassSet.add(c)
          case None =>
            val subClassSet = Set(c)
            classSubClassSetMap.put(supClass.asResource(), subClassSet)
        }
      }
    }
    // println(classSubClassSetMap.keySet.size)
    for ((cls, subClassSet) <- classSubClassSetMap) {
      // println(cls + " -> " + subClassSet.size)
      val propertyCountMap = Map[String, Int]() // 兄弟クラスセットごとに用意
      for (subClass <- subClassSet) {
        val promotedPropertySet = getPromotedPropertySet(subClass, propertyCountMap, subClassSet, classPropertySetMap)
        //if (cls.getURI() == "http://nlpwww.nict.go.jp/wn-ja/07950920-n") {
        //    println(subClass + " promoted " + promotedPropertySet)
        // }
        for (pp <- promotedPropertySet) {
          if (pp.count == subClassSet.size) {
            pp.label = "completeness"
            ppInfo.pnCompleteness += 1
          } else {
            pp.label = "candidate"
            ppInfo.pnCandidate += 1
          }
        }
        promotePropertySet(cls, subClass, promotedPropertySet, classPropertySetMap, ppInfo)
      }
    }
    if (depth == 0) {
      ppInfo.anCompleteness = ppInfo.bnCompleteness
      ppInfo.anCandidate = ppInfo.bnCandidate
      ppInfo.anDefault = ppInfo.bnDefault
    }
    println(depth + "," + depthClassNumMap.getOrElse(depth, 0) + "," + ppInfo)
    supClassSet ++= classSubClassSetMap.keySet
    promoteProperties(supClassSet, ontology, classPropertySetMap, classDepthMap)

    return supClassSet.size
  }

  def promotePropertySet(cls: Resource, subClass: Resource, promotedPropertySet: Set[JWOProperty], classPropertySetMap: Map[Resource, Set[JWOProperty]], ppInfo: PromotingProgressInfo) = {
    //    println("上位クラス：" + cls)
    classPropertySetMap.get(cls) match {
      case Some(propertySet) =>
        for (pp <- promotedPropertySet) {
          var isIncluded = false
          for (p <- propertySet) {
            if (pp.property == p.property) {
              isIncluded = true
            }
          }
          if (!isIncluded) {
            propertySet.add(pp) // 存在していない場合上位クラスにプロパティを追加
          }
        }
      case None =>
        classPropertySetMap.put(cls, promotedPropertySet) // 上位クラスがプロパティを持っていなかった場合には，そのままプロモートプロパティセットを追加
    }
    //    println("下位クラス：" + subClass)
    // プロモートしたプロパティを削除
    classPropertySetMap.get(subClass) match {
      case Some(propertySet) =>
        //println("befo: " + propertySet.size + ": " + promotedPropertySet.size)
        //println(propertySet)
        //println(promotedPropertySet)
        val newPropertySet = propertySet -- promotedPropertySet
        classPropertySetMap.put(subClass, newPropertySet)
        //        println("aft: " + newPropertySet.size)
        //        println(newPropertySet)
        for (p <- newPropertySet) {
          p.label match {
            case "completeness" =>
              ppInfo.anCompleteness += 1
            case "candidate" =>
              ppInfo.anCandidate += 1
            case "default" => ppInfo.anDefault += 1
          }
        }
      case None =>
    }
  }

  def getPromotedPropertySet(subClass: Resource, propertyCountMap: Map[String, Int], subClassSet: Set[Resource], classPropertySetMap: Map[Resource, Set[JWOProperty]]): Set[JWOProperty] = {
    val promotedPropertySet = Set[JWOProperty]()
    classPropertySetMap.get(subClass) match {
      case Some(propertySet) =>
        // println(subClass + ": " + propertySet)
        for (property <- propertySet) {
          var count = 0
          if (propertyCountMap.containsKey(property.property)) {
            count = propertyCountMap.getOrElse(property.property, 0)
          } else {
            count = getCommonPropertyCount(property, subClassSet, classPropertySetMap)
            propertyCountMap.put(property.property, count)
          }
          if (1 < count) {
            property.count = count
            promotedPropertySet.add(property)
          }
        }
      case None =>
    }
    return promotedPropertySet
  }

  def getCommonPropertyCount(property: JWOProperty, classSet: Set[Resource], classPropertySetMap: Map[Resource, Set[JWOProperty]]): Int = {
    var count = 0
    for (c <- classSet) {
      classPropertySetMap.get(c) match {
        case Some(propertySet) =>
          for (p <- propertySet) {
            if (p.property == property.property) {
              count += 1
            }
          }
        case None =>
      }
    }
    return count
  }

  def countPropertyDefinition(classPropertySetMap: Map[Resource, Set[JWOProperty]], classDepthMap: Map[Resource, Int]) = {
    var count = 0
    for ((cls, propertySet) <- classPropertySetMap) {
      count += propertySet.size
    }
    println("プロパティ定義数：" + count)
    println("プロパティを持つクラス数：" + classPropertySetMap.keySet.size)
  }

  def writeDB(dbName: String, classPropertySetMap: Map[Resource, Set[JWOProperty]], classDepthMap: Map[Resource, Int]) = {
    println("writing " + dbName)
    val classPropertyWithLabelListDB = Database.forURL(url = "jdbc:sqlite:" + dbName, driver = "org.sqlite.JDBC")
    classPropertyWithLabelListDB withSession {
      (ClassPropertyWithLabelList.ddl).create
      for ((c, propertySet) <- classPropertySetMap) {
        val depth = classDepthMap.getOrElse(c, 0)
        for (p <- propertySet) {
          // println(cls + "\t" + property + "\t" + count)          
          ClassPropertyWithLabelList.insert(c.getURI(), p.property, p.label, depth)
        }
      }
    }
    println("writing " + dbName + " done")
  }

  def makeClassDepthMap(ontology: Model, classDepthMap: Map[Resource, Int], depth: Int, targetClass: Resource): Int = {
    classDepthMap.put(targetClass, depth)
    for (stmt <- ontology.listStatements(null, RDFS.subClassOf, targetClass).toList()) {
      makeClassDepthMap(ontology, classDepthMap, depth + 1, stmt.getSubject())
    }
    return depth
  }

  /**
   * プロモートによりプロパティがゼロになってしまったクラスをマップから削除
   */
  def removeClassesWithNoProperty(classPropertySetMap: Map[Resource, Set[JWOProperty]]) = {
    val classSetWithNoProperty = Set[Resource]()
    for ((cls, propertySet) <- classPropertySetMap) {
      if (propertySet.size == 0) {
        classSetWithNoProperty.add(cls)
      }
    }
    for (c <- classSetWithNoProperty) {
      classPropertySetMap.removeKey(c)
    }
  }

  val depthClassNumMap = Map[Int, Int]()
  def setDepthClassNumMap(classDepthMap: Map[Resource, Int]) = {
    for (n <- 0 to 13) {
      depthClassNumMap.put(n, 0)
    }
    for ((cls, depth) <- classDepthMap) {
      depthClassNumMap.get(depth) match {
        case Some(num) => depthClassNumMap.put(depth, num + 1)
        case None => depthClassNumMap.put(depth, 1)
      }
    }
  }

  def removeRedundantProperties(supClassPropertySet: Set[JWOProperty], subClassList: java.util.List[Resource], classPropertySetMap: Map[Resource, Set[JWOProperty]], ontology: Model): Int = {
    for (subClass <- subClassList) {
      classPropertySetMap.get(subClass) match {
        case Some(propertySet) =>
          val removePropertySet = Set[JWOProperty]()
          for (p <- propertySet) {
            for (sp <- supClassPropertySet) {
              if (p.property == sp.property) {
                removePropertySet += p
              }
            }
          }
          propertySet --= removePropertySet
          //          if (0 < removePropertySet.size) {
          //            println(subClass + ": " + removePropertySet)
          //          }
          val subSubClassList = ontology.listSubjectsWithProperty(RDFS.subClassOf, subClass).toList()
          removeRedundantProperties(supClassPropertySet ++ propertySet, subSubClassList, classPropertySetMap, ontology)
        case None =>
          val subSubClassList = ontology.listSubjectsWithProperty(RDFS.subClassOf, subClass).toList()
          removeRedundantProperties(supClassPropertySet, subSubClassList, classPropertySetMap, ontology)
      }
    }
    return 0
  }

  def printPropertyCount(classPropertySetMap: Map[Resource, Set[JWOProperty]]) = {
    val ps = Set[String]()
    for ((cls, propertySet) <- classPropertySetMap) {
      for (p <- propertySet) {
        ps += p.property
      }
    }
    println("全プロパティ数：" + ps.size)
  }

  def printPropertyDefinedJWOandJWNClassCount(classPropertySetMap: Map[Resource, Set[JWOProperty]]) = {
    var jwoClassCount = 0
    var jwnClassCount = 0
    for ((cls, propertySet) <- classPropertySetMap) {
      if (cls.getURI().contains("wn-ja")) {
        jwnClassCount += 1
      } else {
        jwoClassCount += 1
      }
    }
    println("JWOにおけるプロパティが定義されたクラス数：" + jwoClassCount)
    println("JWNにおけるプロパティが定義されたクラス数：" + jwnClassCount)
  }

  def printNumberOfLabelsForEachDepthClasses(classPropertySetMap: Map[Resource, Set[JWOProperty]], classDepthMap: Map[Resource, Int]) = {
    val depthPropertyListMap = Map[Int, ListBuffer[JWOProperty]]() //全フィールドが同じ場合にSetでは定義域数が減ってしまうためListBufferとする
    for ((cls, propertySet) <- classPropertySetMap) {
      val depth = classDepthMap.getOrElse(cls, -1)
      depthPropertyListMap.get(depth) match {
        case Some(dpList) =>
          dpList ++= propertySet
        case None =>
          val dpList = ListBuffer[JWOProperty]()
          dpList ++= propertySet
          depthPropertyListMap.put(depth, dpList)
      }
    }
    for (depth <- 0 to 13) {
      depthPropertyListMap.get(depth) match {
        case Some(propertyList) =>
          var completeness = 0
          var candidate = 0
          var default = 0
          for (p <- propertyList) {
            p.label match {
              case "completeness" => completeness += 1
              case "candidate" => candidate += 1
              case "default" => default += 1
            }
          }
          println(depth + "," + completeness + "," + candidate + "," + default)
        case None =>
          println(depth + ",0,0,0")
      }
    }
  }

  def main(args: Array[String]) {
    val classPropertySetMap = makeClassPropertySetMap
    val inputOntology = "ontologies/merged_ontology_20130912.owl"
    val ontology = FileManager.get().loadModel(inputOntology)
    val leafClassSet = extractLeafClassSet(ontology)
    val classDepthMap = Map[Resource, Int]()
    val rootClass = ResourceFactory.createResource("http://nlpwww.nict.go.jp/wn-ja/JPNWN_ROOT")
    printJWOandJWNClassCount(ontology)
    printPropertyCount(classPropertySetMap)
    printPropertyDefinedJWOandJWNClassCount(classPropertySetMap)
    makeClassDepthMap(ontology, classDepthMap, 0, rootClass)
    setDepthClassNumMap(classDepthMap)
    countPropertyDefinition(classPropertySetMap, classDepthMap)
    promoteProperties(leafClassSet, ontology, classPropertySetMap, classDepthMap)
    removeClassesWithNoProperty(classPropertySetMap)
    countPropertyDefinition(classPropertySetMap, classDepthMap)
    printPropertyDefinedJWOandJWNClassCount(classPropertySetMap)
    writeDB("inputs_and_outputs/class_elevated_property_list_with_label_and_depth.db", classPropertySetMap, classDepthMap)
    val subClassList = ontology.listSubjectsWithProperty(RDFS.subClassOf, rootClass).toList()
    var supClassPropertySet = classPropertySetMap.getOrElse(rootClass, Set[JWOProperty]())
    removeRedundantProperties(supClassPropertySet, subClassList, classPropertySetMap, ontology)
    countPropertyDefinition(classPropertySetMap, classDepthMap)
    printPropertyDefinedJWOandJWNClassCount(classPropertySetMap)
    printNumberOfLabelsForEachDepthClasses(classPropertySetMap, classDepthMap)
    writeDB("inputs_and_outputs/class_elevated_property_list_with_label_and_depth_removing_inherited_properties.db", classPropertySetMap, classDepthMap)
  }
}