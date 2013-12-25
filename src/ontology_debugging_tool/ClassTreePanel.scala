package ontology_debugging_tool

import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.mutable.Map
import scala.collection.mutable.Set
import scala.swing.ListView
import scala.swing.BorderPanel
import scala.swing.Button
import scala.swing.Component
import scala.swing.GridPanel
import scala.swing.Label
import scala.swing.ScrollPane
import scala.swing.Scrollable
import scala.swing.SplitPane
import scala.swing.TextArea
import scala.swing.TextField
import org.scalaquery.ql.basic.BasicDriver.Implicit.baseColumnToColumnOps
import org.scalaquery.ql.basic.BasicDriver.Implicit.queryToQueryInvoker
import org.scalaquery.ql.basic.BasicDriver.Implicit.tableToQuery
import org.scalaquery.ql.basic.BasicDriver.Implicit.valueToConstColumn
import org.scalaquery.ql.basic.BasicTable
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.session.Database
import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.tdb.TDBFactory
import com.hp.hpl.jena.vocabulary.RDFS
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeModel
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JTree
import utils.Utils
import scala.swing.Action
import scala.collection.mutable.ListBuffer
import scala.swing.event.ListSelectionChanged
import javax.swing.tree.TreePath
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent

class ClassTreePanel(val propertyRefinementPanel: PropertyRefinementPanel) extends BorderPanel {

  val directory = "ontologies/refined_jwo_tdb";
  val tdbModel = TDBFactory.createModel(directory);

  val resourceNodeMap = Map[Resource, ClassTreeNode]()

  def getClassTreeNode(clsRes: Resource): ClassTreeNode = {
    resourceNodeMap.get(clsRes) match {
      case Some(classTreeNode) =>
        return classTreeNode
      case None =>
        val classTreeNode = new ClassTreeNode(clsRes)
        resourceNodeMap.put(clsRes, classTreeNode)
        return classTreeNode
    }
  }

  def expandAll() = {
    var row = 0;
    while (row < classTree.peer.getRowCount()) {
      classTree.peer.expandRow(row);
      row += 1
    }
  }

  val searchField = new TextField(30)
  val searchResultListVew = new ListView[ClassTreeNode]() {
    selection.intervalMode = ListView.IntervalMode.Single
  }

  listenTo(searchResultListVew.selection)
  reactions += {
    case ListSelectionChanged(source, range, live) =>
      if (source == searchResultListVew) {
        if (0 < searchResultListVew.selection.items.size) {
          val selectedNode = searchResultListVew.selection.items(0)
          println(selectedNode)
          val treeModel = classTree.peer.getModel().asInstanceOf[DefaultTreeModel]
          println(classTreeModel.getPathToRoot(selectedNode))
          println(treeModel.getPathToRoot(selectedNode))
          val path = new TreePath(treeModel.getPathToRoot(selectedNode));
          println(path)
          classTree.peer.setSelectionPath(path);
          classTree.peer.scrollPathToVisible(path);
        }
      }
  }

  def isMatchedClassNode(node: ClassTreeNode, searchText: String): Boolean = {
    val labelSet = node.prefLabelSet ++ node.labelSet
    for (label <- labelSet) {
      if (label.matches(searchText)) {
        return true
      }
    }
    return false
  }
  val searchButton = new Button(Action("検索") {

    val treeModel = newClassTree.getModel().asInstanceOf[DefaultTreeModel]
    val enum = treeModel.getRoot().asInstanceOf[DefaultMutableTreeNode].depthFirstEnumeration()
    var node: DefaultMutableTreeNode = null
    while (enum.hasMoreElements()) {
      node = enum.nextElement().asInstanceOf[DefaultMutableTreeNode]
      println(node)
    }
    val path = new TreePath(treeModel.getPathToRoot(node));
    println(path)
    classTree.peer.setSelectionPath(path);
    classTree.peer.scrollPathToVisible(path);

    val searchText = searchField.text
    if (searchText.size == 0) {
      searchResultListVew.listData = ListBuffer[ClassTreeNode]()
    } else {
      val resultList = ListBuffer[ClassTreeNode]()
      for (node <- resourceNodeMap.values) {
        if (isMatchedClassNode(node, searchText)) {
          resultList += node
        }
      }
      searchResultListVew.listData = resultList
    }
  })

  val searchPanel = new BorderPanel() {
    val northPanel = new BorderPanel() {
      add(searchField, BorderPanel.Position.Center)
      add(searchButton, BorderPanel.Position.East)
    }
    add(northPanel, BorderPanel.Position.North)
    val searchResultListViewScrollPane = new ScrollPane(searchResultListVew) {
      border = BorderFactory.createTitledBorder("検索結果")
    }
    add(searchResultListViewScrollPane, BorderPanel.Position.Center)
  }
  val uriLabel = new Label() {
    border = BorderFactory.createTitledBorder("URI")
  }
  val prefLabelListView = new ListView[String]()
  val labelListView = new ListView[String]()
  val labelPanel = new GridPanel(1, 2) {
    contents += new ScrollPane(prefLabelListView) {
      border = BorderFactory.createTitledBorder("skos:prefLabel")
    }
    contents += new ScrollPane(labelListView) {
      border = BorderFactory.createTitledBorder("rdfs:label")
    }
  }
  val descriptionTextArea = new TextArea(3, 3) {
    lineWrap = true
    editable = false
  }
  val classAttributePanel = new BorderPanel() {
    add(uriLabel, BorderPanel.Position.North)
    add(labelPanel, BorderPanel.Position.Center)
    add(new ScrollPane(descriptionTextArea) {
      border = BorderFactory.createTitledBorder("説明文")
    }, BorderPanel.Position.South)
  }
  val classTreeNorthPanel = new GridPanel(2, 1) {
    contents += searchPanel
    contents += classAttributePanel
  }

  def loadClassTree(): ClassTreeNode = {
    var rootNode: ClassTreeNode = null
    for (stmt <- tdbModel.listStatements(null, RDFS.subClassOf, null).toList) {
      val subClassRes = stmt.getSubject()
      val supClassRes = stmt.getObject().asResource()

      val subClassNode = getClassTreeNode(subClassRes)
      val supClassNode = getClassTreeNode(supClassRes)

      supClassNode.add(subClassNode)
      if (supClassNode.label == "CLASS_ROOT") {
        rootNode = supClassNode
      }
    }
    return rootNode
  }

  def setClassAttribute(node: ClassTreeNode) = {
    val jwnNs = "http://nlpwww.nict.go.jp/wn-ja/"
    val jwoClassNs = "http://www.yamaguti.comp.ae.keio.ac.jp/wikipedia_ontology/class/"
    if (node.resource.getURI().matches(jwnNs + ".*")) {
      uriLabel.text = node.resource.getURI().replace(jwnNs, "jwn:")
    } else if (node.resource.getURI().matches(jwoClassNs + ".*")) {
      uriLabel.text = node.resource.getURI().replace(jwoClassNs, "jwo_class:")
    } else {
      uriLabel.text = ""
    }
    prefLabelListView.listData = node.prefLabelSet.toList
    labelListView.listData = node.labelSet.toList
    descriptionTextArea.text = node.description
    propertyRefinementPanel.setPropertyInfo(node.resource.getURI())
  }

  class ClassTreeKeyAdapter extends KeyAdapter {
    override def keyPressed(e: KeyEvent): Unit = {
      val selPath = classTree.peer.getSelectionPath()
      if (selPath == null) return
      val node = selPath.getLastPathComponent().asInstanceOf[ClassTreeNode]
      setClassAttribute(node)
    }
  }

  class ClassTreeMouseAdapter extends MouseAdapter {
    override def mouseClicked(e: MouseEvent): Unit = {
      val selPath = classTree.peer.getPathForLocation(e.getX(), e.getY())
      if (selPath == null) return
      val node = selPath.getLastPathComponent().asInstanceOf[ClassTreeNode]
      setClassAttribute(node)
    }
  }

  val rootNode = loadClassTree()
  //  val rootNode = new DefaultMutableTreeNode("root")
  //  val aNode = new DefaultMutableTreeNode("a")
  //  val bNode = new DefaultMutableTreeNode("b")
  //  rootNode.add(aNode)
  //  rootNode.add(bNode)
  val classTreeModel = new DefaultTreeModel(rootNode)
  val classTree = new Tree(classTreeModel)
  val newClassTree = new JTree(classTreeModel) {
  }
  classTree.peer.setCellRenderer(new ClassTreeCellRenderer())
  classTree.peer.addMouseListener(new ClassTreeMouseAdapter())
  classTree.peer.addKeyListener(new ClassTreeKeyAdapter())
  expandAll()

  val splitPane = new SplitPane() {
    dividerLocation = 300
    oneTouchExpandable = true
    topComponent = classTreeNorthPanel
    bottomComponent = classTree
//    bottomComponent = new Component {
//      override lazy val peer = newClassTree
//    }
  }
  add(splitPane, BorderPanel.Position.Center)
}

class Tree(model: TreeModel) extends Component with Scrollable.Wrapper {
  override lazy val peer: JTree = new JTree(model) with SuperMixin
  protected def scrollablePeer = peer
}

object ClassInstanceCntList extends BasicTable[(String, Int)]("instance_count_table") {
  def jwoClass = column[String]("jwoClass")
  def instanceCnt = column[Int]("count")

  def * = jwoClass ~ instanceCnt
}

class ClassTreeNode(val resource: Resource) extends DefaultMutableTreeNode {
  var instanceNum = 0
  val classInstanceDB = Database.forURL(url = "jdbc:sqlite:refined_class_instance_list_removing_redundant_type.db", driver = "org.sqlite.JDBC")
  classInstanceDB withSession {
    val q = for { result <- ClassInstanceCntList if result.jwoClass === resource.getURI }
      yield result.jwoClass ~ result.instanceCnt
    for ((jwoc, cnt) <- q.list) {
      instanceNum = cnt
    }
  }
  val prefLabelSet = Set[String]()
  val labelSet = Set[String]()
  var description = ""
  for (stmt <- resource.listProperties(Utils.skosPrefLabel).toList) {
    prefLabelSet.add(stmt.getObject().asLiteral().getString())
  }
  for (stmt <- resource.listProperties(RDFS.label).toList) {
    labelSet.add(stmt.getObject().asLiteral().getString())
  }
  for (stmt <- resource.listProperties(RDFS.comment).toList) {
    val literal = stmt.getObject().asLiteral()
    if (literal.getLanguage() == "ja") {
      description = literal.getString()
    }
  }
  var label = ""
  if (0 < prefLabelSet.size) {
    label = prefLabelSet.toList(0)
  } else {
    if (0 < labelSet.size) {
      label = labelSet.toList(0)
    } else {
      label = resource.getLocalName()
    }
  }

  override def toString(): String = {
    return label
  }
}

class ClassTreeCellRenderer extends DefaultTreeCellRenderer {
  override def getTreeCellRendererComponent(tree: JTree, value: Object,
    selected: Boolean, expanded: Boolean, leaf: Boolean,
    row: Int, hasFocus: Boolean): java.awt.Component = {
    val c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    val label = c.asInstanceOf[JLabel]
    val node = value.asInstanceOf[ClassTreeNode];

    if (node.instanceNum == 0) {
      label.setIcon(Utils.classIcon0)
    } else if (0 < node.instanceNum && node.instanceNum <= 50) {
      label.setIcon(Utils.classIcon1)
    } else if (50 < node.instanceNum && node.instanceNum <= 100) {
      label.setIcon(Utils.classIcon2)
    } else if (100 < node.instanceNum && node.instanceNum <= 300) {
      label.setIcon(Utils.classIcon3)
    } else if (300 < node.instanceNum && node.instanceNum <= 500) {
      label.setIcon(Utils.classIcon4)
    } else if (500 < node.instanceNum && node.instanceNum <= 1000) {
      label.setIcon(Utils.classIcon5)
    } else if (1000 < node.instanceNum && node.instanceNum <= 3000) {
      label.setIcon(Utils.classIcon6)
    } else if (3000 < node.instanceNum) {
      label.setIcon(Utils.classIcon7)
    }
    return c;
  }
}
