package jwo_jwn_alignment_tool

import java.awt.Dimension
import java.awt.Font
import java.io.BufferedWriter
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.bufferAsJavaList
import scala.collection.mutable.Set
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import scala.io.Source
import scala.swing.event.ListSelectionChanged
import scala.swing.event.TableRowsSelected
import scala.swing.Action
import scala.swing.BorderPanel
import scala.swing.Button
import scala.swing.Component
import scala.swing.FileChooser
import scala.swing.FlowPanel
import scala.swing.GridPanel
import scala.swing.Label
import scala.swing.ListView
import scala.swing.MainFrame
import scala.swing.Menu
import scala.swing.MenuBar
import scala.swing.MenuItem
import scala.swing.ScrollPane
import scala.swing.SimpleSwingApplication
import scala.swing.SplitPane
import scala.swing.Table
import scala.swing.TextArea
import scala.swing.TextField
import org.scalaquery.ql.basic.BasicDriver.Implicit.baseColumnToColumnOps
import org.scalaquery.ql.basic.BasicDriver.Implicit.queryToQueryInvoker
import org.scalaquery.ql.basic.BasicDriver.Implicit.tableToQuery
import org.scalaquery.ql.basic.BasicDriver.Implicit.valueToConstColumn
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.session.Database
import com.hp.hpl.jena.rdf.model.ResourceFactory
import com.hp.hpl.jena.tdb.TDBFactory
import com.hp.hpl.jena.vocabulary.RDFS
import class_instance_extractor.ClassInstanceList
import data.JWNSynset
import javax.swing.BorderFactory
import scala.swing.ComboBox

/**
 * 3. Aligning JWO classes and JWN synsets
 * 3-4. jwo_jwn_alignment_tool.JWOandJWNAlignmentTool.scala
 * - Inputs
 * -- inputs_and_outputs/alignment-target-class-list.txt
 * -- inputs_and_outputs/jpwn1.1_synonyms_ja.txt
 * -- inputs_and_outputs/merged_class_instance_list.db
 * -- ontologies/jwn1.1_tdb
 * - Outputs
 * -- inputs_and_outputs/alignment_results.db
 * -- inputs_for_DODDLE/inputWordList.txt
 * -- inputs_for_DODDLE/inputWordConceptList.txt
 * -- inputs_and_outputs/jwo_jwn_alignment_results_20120306.txt
 */
object JWOandJWNAlignmentTool extends SimpleSwingApplication {

  val alignmentTargetClassList = "inputs_and_outputs/alignment-target-class-list.txt"
  val jwoClassList: ListBuffer[String] = ListBuffer()
  for (rootClass <- Source.fromFile(alignmentTargetClassList).getLines) {
    jwoClassList += rootClass
  }

  val JWNJaSynsets = "inputs_and_outputs/jpwn1.1_synonyms_ja.txt"
  val jwnSynsetList: ListBuffer[List[String]] = ListBuffer()
  for (line <- Source.fromFile(JWNJaSynsets).getLines) {
    jwnSynsetList += line.split(",").toList
  }

  val numberOfResultsComboBox = new ComboBox(List(5, 10, 15, 20, 25, 30, 35, 40, 45, 50)) {
    //border = BorderFactory.createTitledBorder("上位N件")
    border = BorderFactory.createTitledBorder("Top N")
  }

  val restNumLabel = new Label {
    font = new Font("", Font.PLAIN, 30)
  }
  restNumLabel.text = jwoClassList.size.toString()

  val northWestPanel = new GridPanel(1, 2) {
    contents += restNumLabel
    contents += numberOfResultsComboBox
  }

  val searchTextField = new TextField(30) {
    font = new Font("", Font.PLAIN, 30)
  }

  //val searchButton = new Button(Action("アライメント") {
  val searchButton = new Button(Action("Alignment") {
    val jwoCls = searchTextField.text
    val num = numberOfResultsComboBox.selection.item
    val similaritySynset = new JWOandJWNAlignment(jwnSynsetList, jwoCls)
    val alignmentResultList = similaritySynset.getAlignmentResultList(num)
    while (alignmentResultsTable.model.getRowCount() != 0) {
      alignmentResultsTable.model.removeRow(0)
    }
    for (r <- alignmentResultList) {
      alignmentResultsTable.model.addRow(Array[AnyRef](r.jwnSynset, r.similarity.toString(), r.method))
    }
  })

  val searchPanel = new BorderPanel() {
    preferredSize = new Dimension(50, 50)
    add(northWestPanel, BorderPanel.Position.West)
    add(searchTextField, BorderPanel.Position.Center)
    add(searchButton, BorderPanel.Position.East)
  }

  val jwoClassListView = new ListView[String] {
    selection.intervalMode = ListView.IntervalMode.Single
  }
  jwoClassListView.listData = jwoClassList

  val jwoInstanceListView = new ListView[String] {
    selection.intervalMode = ListView.IntervalMode.Single
  }

  val jwoPanel = new GridPanel(2, 1) {
    preferredSize = new Dimension(200, 400)
    contents += new ScrollPane(jwoClassListView) {
      //border = BorderFactory.createTitledBorder("JWOクラスリスト")
      border = BorderFactory.createTitledBorder("Alignment Target Classes")
    }
    contents += new ScrollPane(jwoInstanceListView) {
      //border = BorderFactory.createTitledBorder("JWOインスタンスリスト")
      border = BorderFactory.createTitledBorder("Instance List")
    }
  }

  val jwnSynsetIdTextField = new TextField() {
    border = BorderFactory.createTitledBorder("JWN Synset ID")
  }

  val jwnSynsetListView = new ListView[String] {
    selection.intervalMode = ListView.IntervalMode.Single
  }

  val jwnJaDefinitionArea = new TextArea(5, 5) {
    lineWrap = true
    editable = false
  }

  val jwnEnDefinitionArea = new TextArea(5, 5) {
    lineWrap = true
    editable = false
  }

  val jwnPanel = new BorderPanel() {
    preferredSize = new Dimension(200, 400)
    add(jwnSynsetIdTextField, BorderPanel.Position.North)
    add(new GridPanel(3, 1) {
      contents += new ScrollPane(jwnSynsetListView) {
        //border = BorderFactory.createTitledBorder("JWN同義語リスト")
        border = BorderFactory.createTitledBorder("JWN Synsets")
      }
      contents += new ScrollPane(jwnJaDefinitionArea) {
        //border = BorderFactory.createTitledBorder("JWN日本語定義文")
        border = BorderFactory.createTitledBorder("JWN Japanese gloss")
      }
      contents += new ScrollPane(jwnEnDefinitionArea) {
        //border = BorderFactory.createTitledBorder("JWN英語定義文")
        border = BorderFactory.createTitledBorder("JWN English gloss")
      }
    }, BorderPanel.Position.Center)
  }

  val alignmentResultsTable = new Table {
    override lazy val model = super.model.asInstanceOf[javax.swing.table.DefaultTableModel]
    model.addColumn("JWN Synset ID")
    //model.addColumn("類似度")
    model.addColumn("Similarity")
    //model.addColumn("手法")
    model.addColumn("Method")
  }

  val jwnSupClassListView = new ListView[JWNSynset] {
    selection.intervalMode = ListView.IntervalMode.Single
  }

  val jwnSiblingClassListView = new ListView[JWNSynset] {
    selection.intervalMode = ListView.IntervalMode.Single
  }

  val jwnSubClassListView = new ListView[JWNSynset] {
    selection.intervalMode = ListView.IntervalMode.Single
  }

  val jwnSynsetPanel = new GridPanel(1, 3) {
    preferredSize = new Dimension(200, 200)
    contents += new ScrollPane(jwnSupClassListView) {
      //border = BorderFactory.createTitledBorder("JWN上位クラス")
      border = BorderFactory.createTitledBorder("JWN Super Synsets")
    }
    contents += new ScrollPane(jwnSiblingClassListView) {
      //border = BorderFactory.createTitledBorder("JWN兄弟クラス")
      border = BorderFactory.createTitledBorder("JWN Sibling Synsets")
    }
    contents += new ScrollPane(jwnSubClassListView) {
      //border = BorderFactory.createTitledBorder("JWN下位クラス")
      border = BorderFactory.createTitledBorder("JWN Sub Synsets")
    }
  }

  val directory = "jwn1.1_tdb";
  val model = TDBFactory.createModel(directory);

  val alignmentResultsDB = Database.forURL(url = "jdbc:sqlite:inputs_and_outputs/alignment_results.db", driver = "org.sqlite.JDBC")
  val classInstanceDB = Database.forURL(url = "jdbc:sqlite:inputs_and_outputs/merged_class_instance_list.db", driver = "org.sqlite.JDBC")

  def setAlignmentResults() = {
    val selectedClass = jwoClassListView.selection.items(0)
    searchTextField.text = selectedClass
    alignmentResultsDB withSession {
      val q = for { result <- AlignmentResults if result.jwoClass === selectedClass }
        yield result.jwnSynsetID ~ result.similarity ~ result.method
      for ((jwnSynsetID, similarity, method) <- q.list) {
        //            println(jwnSynsetID + "," + similarity + "," + method)
        alignmentResultsTable.model.addRow(Array[AnyRef](jwnSynsetID, similarity, method))
      }
    }
  }

  def setInstanceList() = {
    val selectedClass = jwoClassListView.selection.items(0)
    classInstanceDB withSession {
      val instanceList = ListBuffer[String]()
      val q = for { result <- ClassInstanceList if result.jwoClass === selectedClass }
        yield result.jwoClass ~ result.jwoInstance
      for ((jwoClass, jwoInstance) <- q.list.take(100)) {
        //            println(jwoClass + "," + jwoInstance)
        instanceList += jwoInstance
      }
      jwoInstanceListView.listData = instanceList
    }
  }

  def clearData() = {
    searchTextField.text = ""
    jwoInstanceListView.listData = ListBuffer[String]()
    jwnSynsetIdTextField.text = ""
    jwnSynsetListView.listData = ListBuffer[String]()
    jwnSupClassListView.listData = ListBuffer[JWNSynset]()
    jwnSiblingClassListView.listData = ListBuffer[JWNSynset]()
    jwnSubClassListView.listData = ListBuffer[JWNSynset]()
    jwnEnDefinitionArea.text = ""
    jwnJaDefinitionArea.text = ""
    while (alignmentResultsTable.model.getRowCount() != 0) {
      alignmentResultsTable.model.removeRow(0)
    }
  }

  listenTo(jwoClassListView.selection, alignmentResultsTable.selection, jwnSupClassListView.selection, jwnSiblingClassListView.selection, jwnSubClassListView.selection)
  reactions += {
    case ListSelectionChanged(source, range, live) =>
      if (source == jwoClassListView) {
        clearData()
        if (jwoClassListView.selection.items.size == 1) {
          setAlignmentResults()
          setInstanceList()
        }
      } else if (source == jwnSupClassListView) {
        if (jwnSupClassListView.selection.items.size == 1) {
          val jwnSynset = jwnSupClassListView.selection.items(0)
          setJWNSynset(jwnSynset)
        }
      } else if (source == jwnSiblingClassListView) {
        if (jwnSiblingClassListView.selection.items.size == 1) {
          val jwnSynset = jwnSiblingClassListView.selection.items(0)
          setJWNSynset(jwnSynset)
        }
      } else if (source == jwnSubClassListView) {
        if (jwnSubClassListView.selection.items.size == 1) {
          val jwnSynset = jwnSubClassListView.selection.items(0)
          setJWNSynset(jwnSynset)
        }
      }
    case TableRowsSelected(source, range, live) =>
      if (source == alignmentResultsTable) {
        val selectedIndex = alignmentResultsTable.selection.rows.leadIndex
        //        println(selectedIndex)
        //        println(alignmentResultsTable.selection.rows.size)
        if (0 <= selectedIndex && 0 < alignmentResultsTable.selection.rows.size) {
          val selectedId = alignmentResultsTable.model.getValueAt(selectedIndex, 0)
          setJWNSynsetsAndDefinition(selectedId.toString())
        }
      }
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
        //        synsetList.add(label.getString())
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
    return new JWNSynset(res.getURI().split("ja/")(1), synsetList.toList, enDescription, jaDescription)
  }

  def setJWNSynsetsAndDefinition(id: String) = {
    val res = ResourceFactory.createResource("http://nlpwww.nict.go.jp/wn-ja/" + id)
    val targetJWNSynset = getJWNSynset(res.getURI())
    val supClassSet = Set[JWNSynset]()
    val siblingClassSet = Set[JWNSynset]()
    val subClassSet = Set[JWNSynset]()
    for (stmt <- model.listStatements(res, RDFS.subClassOf, null).toList) {
      val supRes = stmt.getObject().asResource()
      val supJWNSynset = getJWNSynset(supRes.getURI())
      supClassSet.add(supJWNSynset)
      for (stmt2 <- model.listStatements(null, RDFS.subClassOf, supRes).toList) {
        val siblingRes = stmt2.getSubject()
        val siblingJWNSynset = getJWNSynset(siblingRes.getURI())
        if (siblingJWNSynset.id != targetJWNSynset.id) {
          siblingClassSet.add(siblingJWNSynset)
        }
      }
    }
    for (stmt <- model.listStatements(null, RDFS.subClassOf, res).toList) {
      val subRes = stmt.getSubject().asResource()
      val subJWNSynset = getJWNSynset(subRes.getURI())
      subClassSet.add(subJWNSynset)
    }
    setJWNSynset(targetJWNSynset)
    jwnSupClassListView.listData = supClassSet.toList
    jwnSiblingClassListView.listData = siblingClassSet.toList
    jwnSubClassListView.listData = subClassSet.toList
  }

  def setJWNSynset(targetJWNSynset: JWNSynset) = {
    jwnSynsetIdTextField.text = targetJWNSynset.id
    jwnSynsetListView.listData = targetJWNSynset.synsetList
    jwnJaDefinitionArea.text = targetJWNSynset.jaDescription
    jwnEnDefinitionArea.text = targetJWNSynset.enDescription
  }

  def setAlignmentResultbyHandTable(relation: String) = {
    val jwoClass = jwoClassListView.selection.items(0)
    val jwnSynsetId = jwnSynsetIdTextField.text
    if (0 < jwoClass.size && 0 < jwnSynsetId.size) {
      alignmentResultsbyHandTable.model.addRow(Array[AnyRef](jwoClass, jwnSynsetId, relation))
      jwoClassListView.listData = jwoClassListView.listData.toList - jwoClass
    }
    restNumLabel.text = jwoClassListView.listData.size.toString()
  }

  //val setSameAsButton = new Button(Action("同値") {
  val setSameAsButton = new Button(Action("Equivalent relation") {
    setAlignmentResultbyHandTable("同値関係")
  })
  //val setIsaButton = new Button(Action("Is-a") {
  val setIsaButton = new Button(Action("Is-a relation") {
    setAlignmentResultbyHandTable("Is-a関係")
  })
  //val setSameAsManuallyButton = new Button(Action("同値（手動）") {
  val setSameAsManuallyButton = new Button(Action("Equivalent relation by hand") {
    setAlignmentResultbyHandTable("同値関係（手動）")
  })
  //val setIsaManuallyButton = new Button(Action("Is-a（手動）") {
  val setIsaManuallyButton = new Button(Action("Is-a relation by hand") {
    setAlignmentResultbyHandTable("Is-a関係（手動）")
  })
  //val setUnclearButton = new Button(Action("不明") {
  val setUnclearButton = new Button(Action("Unknown") {
    setAlignmentResultbyHandTable("不明")
  })

  val alignmentButtonPanel = new FlowPanel() {
    contents += setSameAsButton
    contents += setIsaButton
    contents += setSameAsManuallyButton
    contents += setIsaManuallyButton
    contents += setUnclearButton
  }

  //  val returnButton = new Button(Action("やり直し") {
  val returnButton = new Button(Action("Undo") {
    val selectedRow = alignmentResultsbyHandTable.selection.rows.head
    val selectedJWOClass = alignmentResultsbyHandTable.model.getValueAt(selectedRow, 0)
    alignmentResultsbyHandTable.model.removeRow(selectedRow)
    jwoClassListView.listData = selectedJWOClass.toString :: jwoClassListView.listData.toList
    restNumLabel.text = jwoClassListView.listData.size.toString()
  })

  val menuButtonPanel = new FlowPanel() {
    contents += returnButton
  }

  val buttonPanel = new GridPanel(1, 2) {
    contents += alignmentButtonPanel
    contents += menuButtonPanel
  }

  val centerPanel = new BorderPanel() {
    add(jwoPanel, BorderPanel.Position.West)
    val tablePanel = new BorderPanel() {
      add(new ScrollPane(alignmentResultsTable) {
        // border = BorderFactory.createTitledBorder("アライメント結果")
        border = BorderFactory.createTitledBorder("Alignment Results")
      }, BorderPanel.Position.Center)
      add(jwnSynsetPanel, BorderPanel.Position.South)
    }
    add(tablePanel, BorderPanel.Position.Center)
    add(jwnPanel, BorderPanel.Position.East)
    add(buttonPanel, BorderPanel.Position.South)
  }

  val alignmentResultsbyHandTable = new Table {
    override lazy val model = super.model.asInstanceOf[javax.swing.table.DefaultTableModel]
    //model.addColumn("JWOクラス")
    model.addColumn("Alignment Target Class")
    model.addColumn("JWN Synset ID")
    //model.addColumn("関係")
    model.addColumn("Relation")
  }

  def saveAlignmentResults(root: Component) = {
    val fileChooser = new FileChooser()
    fileChooser.showSaveDialog(root) match {
      case FileChooser.Result.Approve =>
        val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileChooser.selectedFile), "UTF-8"))
        for (i <- 0 until alignmentResultsbyHandTable.model.getRowCount()) {
          val jwoClass = alignmentResultsbyHandTable.model.getValueAt(i, 0)
          val jwnSynsetId = alignmentResultsbyHandTable.model.getValueAt(i, 1)
          val relation = alignmentResultsbyHandTable.model.getValueAt(i, 2)
          //          println(jwoClass + "\t" + jwnSynsetId + "\t" + relation)
          writer.write(jwoClass + "\t" + jwnSynsetId + "\t" + relation)
          writer.newLine()
        }
        writer.close
      case FileChooser.Result.Cancel =>
      case FileChooser.Result.Error =>
    }
  }

  def saveInputWordAndConceptList(root: Component) = {
    val fileChooser = new FileChooser()
    fileChooser.fileSelectionMode = FileChooser.SelectionMode.DirectoriesOnly
    fileChooser.showSaveDialog(root) match {
      case FileChooser.Result.Approve =>
        val ns = "http://nlpwww.nict.go.jp/wn-ja/"
        val separator = System.getProperty("file.separator");
        val inputWordList = "inputWordList.txt"
        val inputWordConceptList = "inputWordConceptList.txt"
        val writer1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileChooser.selectedFile.getParent() + separator + inputWordList), "UTF-8"))
        val writer2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileChooser.selectedFile.getParent() + separator + inputWordConceptList), "UTF-8"))
        val jwnIdSet = Set[String]()
        val jwnWordIdSet = Map[String, Set[String]]()
        for (i <- 0 until alignmentResultsbyHandTable.model.getRowCount()) {
          val jwnSynsetId = alignmentResultsbyHandTable.model.getValueAt(i, 1)
          //          println(jwnSynsetId)
          jwnIdSet.add(jwnSynsetId.toString())
        }
        println("ID数: " + jwnIdSet.size)
        for (jwnSynset <- jwnSynsetList) {
          val id = jwnSynset(0)
          var w = jwnSynset(1)
          if (jwnIdSet.contains(id)) {
            jwnWordIdSet.get(w) match {
              case Some(idSet) => idSet.add(ns + id)
              case None => jwnWordIdSet.put(w, Set[String](ns + id))
            }
          }
        }
        for ((w, idSet) <- jwnWordIdSet) {
          writer1.write(w)
          writer1.newLine()
          writer2.write(w + "," + idSet.mkString(","))
          //          println(w + "," + idSet.mkString(","))
          writer2.newLine()
        }
        writer1.close
        writer2.close
      case FileChooser.Result.Cancel =>
      case FileChooser.Result.Error =>
    }
  }

  def openAlignmentResults(root: Component) = {
    val fileChooser = new FileChooser()
    fileChooser.showOpenDialog(root) match {
      case FileChooser.Result.Approve =>
        val source = Source.fromFile(fileChooser.selectedFile, "utf-8")
        for (line <- source.getLines()) {
          val Array(jwoClass, jwnSynsetId, relation) = line.split("\t")
          jwoClassListView.listData = jwoClassListView.listData.toList - jwoClass
          alignmentResultsbyHandTable.model.addRow(Array[AnyRef](jwoClass, jwnSynsetId, relation))
        }
        restNumLabel.text = jwoClassListView.listData.size.toString()
      case FileChooser.Result.Cancel =>
      case FileChooser.Result.Error =>
    }
  }

  def getMenuBar: MenuBar = {
    new MenuBar {
      //val aMenu = new Menu("ファイル")
      val aMenu = new Menu("File")
      aMenu.contents += new MenuItem(Action("アライメント結果を開く") {
        openAlignmentResults(this)
      })
      aMenu.contents += new MenuItem(Action("アライメント結果を保存") {
        saveAlignmentResults(this)
      })
      aMenu.contents += new MenuItem(Action("DODDLE-OWLの入力語と概念の対応結果して保存") {
        saveInputWordAndConceptList(this)
      })
      aMenu.contents += new MenuItem(Action("終了") {
        System.exit(0)
      })
      contents += aMenu
    }
  }

  def top = new MainFrame {
    //title = "JWOとJWNのアライメントツール ver.2012.03.01"
    title = "Alignment Tool for JWO and JWN"
    menuBar = getMenuBar
    contents = new BorderPanel() {
      val splitPane = new SplitPane() {
        dividerLocation = 500
        topComponent = centerPanel
        bottomComponent = new ScrollPane(alignmentResultsbyHandTable)
        oneTouchExpandable = true
      }
      add(searchPanel, BorderPanel.Position.North)
      add(splitPane, BorderPanel.Position.Center)
    }
    size = new Dimension(1024, 700)
    centerOnScreen()
  }

}