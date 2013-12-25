package ontology_debugging_tool
import java.awt.Dimension
import scala.swing.Action
import scala.swing.BorderPanel
import scala.swing.Button
import scala.swing.CheckBox
import scala.swing.FlowPanel
import scala.swing.GridPanel
import scala.swing.ListView
import scala.swing.MainFrame
import scala.swing.ScrollPane
import scala.swing.SimpleSwingApplication
import scala.swing.Table
import com.hp.hpl.jena.rdf.model.Property
import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.rdf.model.ResourceFactory
import com.hp.hpl.jena.tdb.TDBFactory
import javax.swing.BorderFactory
import scala.collection.JavaConversions._
import scala.collection.mutable.Map
import scala.collection.mutable.Set
import com.hp.hpl.jena.vocabulary.RDFS
import javax.swing.table.DefaultTableModel
import scala.swing.event.ListSelectionChanged
import scala.swing.event.TableRowsSelected

class PropertyRefinementPanel extends GridPanel(4, 1) {
  val propertyInfoDefinitionClassSetMap = Map[PropertyInfo, Set[Resource]]()
  val propertyInfoCommonSiblingClassSetMap = Map[PropertyInfo, Set[Resource]]()
  val inheritedPropertyTable = new Table() {
    override lazy val model = super.model.asInstanceOf[javax.swing.table.DefaultTableModel]
    model.addColumn("継承プロパティ")
    model.addColumn("定義上位クラス数")
    peer.setAutoCreateRowSorter(true)
  }
  val inheritedPropertyDefinedClassListView = new ListView[Resource]()
  val removeInheritedPropertiesButton = new Button(Action("定義クラスからプロパティを削除") {})
  val showPropertiesOfDefinedClassButton = new Button(Action("定義クラスのプロパティを表示") {})
  val showInheritedPropertiesDefinedInSpecificPropertiesButton = new CheckBox("固有プロパティに定義されている継承プロパティのみ表示")
  val inheritedPropertyPanel = new BorderPanel() {
    val listPanel = new GridPanel(1, 2) {
      contents += new ScrollPane(inheritedPropertyTable)
      contents += new ScrollPane(inheritedPropertyDefinedClassListView) {
        border = BorderFactory.createTitledBorder("継承プロパティの定義クラス")
      }
    }
    val buttonPanel = new GridPanel(2, 1) {
      contents += new FlowPanel(FlowPanel.Alignment.Left)(removeInheritedPropertiesButton, showPropertiesOfDefinedClassButton)
      contents += new FlowPanel(FlowPanel.Alignment.Left)(showInheritedPropertiesDefinedInSpecificPropertiesButton)
    }
    add(listPanel, BorderPanel.Position.Center)
    add(buttonPanel, BorderPanel.Position.South)
  }

  val siblingCommonPropertyTable = new Table() {
    override lazy val model = super.model.asInstanceOf[javax.swing.table.DefaultTableModel]
    model.addColumn("兄弟クラス共通プロパティ")
    model.addColumn("定義兄弟クラス数")
    model.addColumn("定義兄弟クラス数の割合")
    peer.setAutoCreateRowSorter(true)
  }
  val siblingCommonPropertyDefinedClassListView = new ListView[Resource]()
  val moveSiblingCommonPropertiesToUpperClassButton = new Button(Action("上位クラスに兄弟クラス共通プロパティを移動") {})
  val suggestedPropertyPanel = new BorderPanel() {
    add(new ScrollPane(siblingCommonPropertyTable), BorderPanel.Position.Center)
    add(new ScrollPane(siblingCommonPropertyDefinedClassListView) {
      border = BorderFactory.createTitledBorder("定義兄弟クラス")
    }, BorderPanel.Position.East)
    add(new FlowPanel(FlowPanel.Alignment.Left)(moveSiblingCommonPropertiesToUpperClassButton), BorderPanel.Position.South)
  }

  val specificPropertyTable = new Table() {
    override lazy val model = super.model.asInstanceOf[javax.swing.table.DefaultTableModel]
    model.addColumn("固有プロパティ")
    model.addColumn("定義インスタンス数")
    model.addColumn("定義インスタンス数の割合")
    peer.setAutoCreateRowSorter(true)
  }
  val subClassListView = new ListView[Resource]()
  val removeSpecificPropertiesButton = new Button(Action("固有プロパティの削除") {})
  val moveSpecificPropertiesToSubClassButton = new Button(Action("下位クラスに固有プロパティを移動") {})
  val specificPropertyPanel = new BorderPanel() {
    val centerPanel = new BorderPanel() {
      add(new ScrollPane(specificPropertyTable), BorderPanel.Position.Center)
      add(new ScrollPane(subClassListView) {
        border = BorderFactory.createTitledBorder("下位クラス")
      }, BorderPanel.Position.East)
    }
    val buttonPanel = new FlowPanel(FlowPanel.Alignment.Left)(removeSpecificPropertiesButton, moveSpecificPropertiesToSubClassButton)
    add(centerPanel, BorderPanel.Position.Center)
    add(buttonPanel, BorderPanel.Position.South)
  }

  val removedPropertyListView = new ListView[String]()
  val returnSpecificPropertiesButton = new Button(Action("固有プロパティに戻す") {})
  val removedPropertyPanel = new BorderPanel() {
    add(new ScrollPane(removedPropertyListView) {
      border = BorderFactory.createTitledBorder("削除プロパティ")
    }, BorderPanel.Position.Center)
    add(new FlowPanel(FlowPanel.Alignment.Left)(returnSpecificPropertiesButton), BorderPanel.Position.South)
  }

  def getSelectedValue(table: Table): PropertyInfo = {
    val selectedIndex = table.selection.rows.leadIndex
    //        println(selectedIndex)
    //        println(alignmentResultsTable.selection.rows.size)
    if (0 <= selectedIndex && 0 < table.selection.rows.size) {
      val propertyInfo = table.model.getValueAt(selectedIndex, 0)
      return propertyInfo.asInstanceOf[PropertyInfo]
    }
    return null
  }
  listenTo(inheritedPropertyTable.selection, siblingCommonPropertyTable.selection, specificPropertyTable.selection)
  reactions += {
    case ListSelectionChanged(source, range, live) =>
    case TableRowsSelected(source, range, live) =>
      if (source == inheritedPropertyTable) {
        val propertyInfo = getSelectedValue(inheritedPropertyTable)
        propertyInfoDefinitionClassSetMap.get(propertyInfo) match {
          case Some(classSet) =>
            inheritedPropertyDefinedClassListView.listData = classSet.toList
          case None =>
        }
        println("継承プロパティ：" + propertyInfo)
      } else if (source == siblingCommonPropertyTable) {
        val propertyInfo = getSelectedValue(siblingCommonPropertyTable)
        propertyInfoCommonSiblingClassSetMap.get(propertyInfo) match {
          case Some(classSet) =>
            siblingCommonPropertyDefinedClassListView.listData = classSet.toList
          case None =>
        }
        println("共通兄弟プロパティ：" + propertyInfo)
      } else if (source == specificPropertyTable) {
        val propertyInfo = getSelectedValue(specificPropertyTable)
        println("固有プロパティ：" + propertyInfo)
      }
  }

  contents += inheritedPropertyPanel
  contents += suggestedPropertyPanel
  contents += specificPropertyPanel
  contents += removedPropertyPanel

  val directory = "refined_jwo_tdb";
  val tdbModel = TDBFactory.createModel(directory);
  val rdfType = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
  def getDefinitionPropertyInfoSet(clsRes: Resource): Set[PropertyInfo] = {
    val propertyInfoMap = Map[Property, PropertyInfo]()
    val instanceList = tdbModel.listSubjectsWithProperty(rdfType, clsRes).toList
    for (instanceRes <- instanceList) {
      val propertySet = Set[Property]() // あるインスタンスに含まれるプロパティのセット 
      for (stmt <- tdbModel.listStatements(instanceRes, null, null).toList) {
        val property = stmt.getPredicate()
        if (property != rdfType) {
          propertySet.add(property)
        }
      }
      for (property <- propertySet) {
        propertyInfoMap.get(property) match {
          case Some(info) => info.num += 1
          case None =>
            propertyInfoMap.put(property, PropertyInfo(property, 1))
        }
      }
    }
    val propertyInfoSet = Set[PropertyInfo]()
    for (pinfo <- propertyInfoMap.values) {
      propertyInfoSet.add(pinfo)
    }
    return propertyInfoSet
  }

  def getSuperClassSet(clsRes: Resource, supClassSet: Set[Resource]): Unit = {
    for (stmt <- clsRes.listProperties(RDFS.subClassOf).toList()) {
      val supClsRes = stmt.getObject().asResource()
      supClassSet.add(supClsRes)
      getSuperClassSet(supClsRes, supClassSet)
    }
  }

  def getSubClassSet(clsResource: Resource): Set[Resource] = {
    val subClassSet = Set[Resource]()
    for (stmt <- tdbModel.listStatements(null, RDFS.subClassOf, clsResource).toList()) {
      val subClsRes = stmt.getSubject()
      subClassSet.add(subClsRes)
    }
    return subClassSet
  }

  def getSiblingClassSet(clsRes: Resource): Set[Resource] = {
    val siblingClassSet = Set[Resource]()
    for (stmt <- clsRes.listProperties(RDFS.subClassOf).toList()) {
      val supClsRes = stmt.getObject().asResource()
      siblingClassSet ++= tdbModel.listSubjectsWithProperty(RDFS.subClassOf, supClsRes).toList() - clsRes
    }
    return siblingClassSet
  }

  def clearTable(table: Table) = {
    while (table.model.getRowCount() != 0) {
      table.model.asInstanceOf[DefaultTableModel].removeRow(0)
    }
  }

  val jwoPropertyNs = "http://www.yamaguti.comp.ae.keio.ac.jp/wikipedia_ontology/property/"

  def setPropertyInfo(cls: String) = {
    val clsRes = tdbModel.getResource(cls)
    val instanceNum: Double = tdbModel.listSubjectsWithProperty(rdfType, clsRes).toList.size
    println("instance num : " + instanceNum)
    val defPropertyInfoSet = getDefinitionPropertyInfoSet(clsRes)
    val clsPropertyInfoSetMap = Map[Resource, Set[PropertyInfo]]()

    val siblingClassSet = getSiblingClassSet(clsRes)
    val subClassSet = getSubClassSet(clsRes)
    val superClassSet = Set[Resource]()
    getSuperClassSet(clsRes, superClassSet)

    def setSpecificPropertyInfo() = {
      clearTable(specificPropertyTable)
      for (p <- defPropertyInfoSet.toSeq.sortBy(-_.num)) {
        val ratio = (p.num / instanceNum).formatted("%1.3f")
        specificPropertyTable.model.addRow(Array[AnyRef](p, p.num.toString, ratio))
        println("定義プロパティ: " + p.propertyRes + ": " + p.num + ": " + instanceNum)
      }
      clsPropertyInfoSetMap.put(clsRes, defPropertyInfoSet)
    }
    def setPropertyInfoDefinitionClassSetMap() = {
      for (supClass <- superClassSet) {
        val propertyInfoSet = getDefinitionPropertyInfoSet(supClass)
        clsPropertyInfoSetMap.put(supClass, propertyInfoSet)
        for (p <- propertyInfoSet) {
          propertyInfoDefinitionClassSetMap.get(p) match {
            case Some(classSet) => classSet.add(supClass)
            case None =>
              propertyInfoDefinitionClassSetMap.put(p, Set[Resource](supClass))
          }
        }
      }
    }
    def setInheritedPropertyInfo() = {
      clearTable(inheritedPropertyTable)
      for ((propertyInfo, clsSet) <- propertyInfoDefinitionClassSetMap.toSeq.sortWith(_._2.size > _._2.size)) {
        inheritedPropertyTable.model.addRow(Array[AnyRef](propertyInfo, clsSet.size.toString))
        if (defPropertyInfoSet.contains(propertyInfo)) {
          println("(+定義）継承プロパティ: " + propertyInfo.propertyRes)
        } else {
          println("継承プロパティ: " + propertyInfo.propertyRes)
        }
        println("定義クラス: " + clsSet)
      }
      println(propertyInfoDefinitionClassSetMap.keySet.size)
    }
    def setSubClassListView() = {
      subClassListView.listData = subClassSet.toList
    }

    def setPropertyInfoCommonSiblingClassSetMap() = {
      println(siblingClassSet)
      for (siblingClass <- siblingClassSet) {
        val propertyInfoSet = getDefinitionPropertyInfoSet(siblingClass)
        clsPropertyInfoSetMap.put(siblingClass, propertyInfoSet)
        val commonSiblingPropertySet = defPropertyInfoSet & propertyInfoSet
        for (cp <- commonSiblingPropertySet) {
          propertyInfoCommonSiblingClassSetMap.get(cp) match {
            case Some(classSet) => classSet.add(siblingClass)
            case None =>
              val classSet = Set[Resource](siblingClass)
              propertyInfoCommonSiblingClassSetMap.put(cp, classSet)
          }
        }
      }
    }

    def setSiblingCommonPropertyInfo() = {
      clearTable(siblingCommonPropertyTable)
      for ((propertyInfo, cset) <- propertyInfoCommonSiblingClassSetMap.toSeq.sortWith(_._2.size > _._2.size)) {
        val siblingClassNum: Double = siblingClassSet.size
        val ratio = (cset.size / siblingClassNum).formatted("%1.3f")
        siblingCommonPropertyTable.model.addRow(Array[AnyRef](propertyInfo, cset.size.toString, ratio.toString))
        println(propertyInfo.propertyRes + ": " + cset.size + ": " + siblingClassSet.size)
        println(cset)
      }
    }

    setSpecificPropertyInfo()
    setPropertyInfoDefinitionClassSetMap()
    setInheritedPropertyInfo()
    setSubClassListView()
    setPropertyInfoCommonSiblingClassSetMap()
    setSiblingCommonPropertyInfo()
  }
}

object PropertyRefinementPanelTest extends SimpleSwingApplication {

  val propertyRefinementPanel = new PropertyRefinementPanel()
  propertyRefinementPanel.setPropertyInfo("http://www.yamaguti.comp.ae.keio.ac.jp/wikipedia_ontology/class/日本の小説家")
  //  propertyRefinementPanel.setPropertyInfo("http://nlpwww.nict.go.jp/wn-ja/08412749-n")

  def top = new MainFrame {
    title = "プロパティ洗練パネル"
    contents = propertyRefinementPanel
    size = new Dimension(1024, 700)
    centerOnScreen()
  }

}

case class PropertyInfo(val propertyRes: Property, var num: Int) {
  val jwoPropertyNs = "http://www.yamaguti.comp.ae.keio.ac.jp/wikipedia_ontology/property/"
  override def equals(x: Any): Boolean = propertyRes == x.asInstanceOf[PropertyInfo].propertyRes
  override def toString(): String = {
    return propertyRes.getURI().replace(jwoPropertyNs, "")
  }
}
