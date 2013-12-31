package class_instance_refinement_tool

import java.awt.Dimension
import java.awt.Font
import java.io.BufferedWriter
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.swing.event.ListSelectionChanged
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
import scala.swing.TextField
import org.scalaquery.ql.basic.BasicDriver.Implicit._
import org.scalaquery.ql.basic.BasicTable
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.session.Database
import javax.swing.BorderFactory
import class_instance_extractor.ClassInstanceList

/**
 * 2. Refining class-instance relationships and identifying alignment target classes
 * 2-2. class_instance_refinement_tool.ClassInstanceRefinementTool.scala
 * - Inputs
 * -- inputs_and_outputs/merged_class_instance_list.db
 * -- inputs_and_outputs/merged-class-list.txt
 * - Output
 * -- class-instance-refinement-results-20120302.txt
 */
object ClassInstanceRefinementTool extends SimpleSwingApplication {
  val inputClassList = "inputs_and_outputs/merged-class-list.txt"
  val jwoClassList: ListBuffer[String] = ListBuffer()
  for (cls <- Source.fromFile(inputClassList).getLines) {
    jwoClassList += cls
  }

  val restNumLabel = new Label {
    font = new Font("", Font.PLAIN, 30)
  }
  restNumLabel.text = jwoClassList.size.toString()

  val searchTextField = new TextField(30) {
    font = new Font("", Font.PLAIN, 30)
  }

  //val searchButton = new Button(Action("クラス検索") {
  val searchButton = new Button(Action("Search Class") {
    val searchText = searchTextField.text
    if (searchText.size == 0) {
      jwoClassListView.listData = jwoClassList
    } else {
      jwoClassListView.listData = jwoClassList.filter {
        c => c.matches(searchText)
      }
    }
  })

  val searchPanel = new BorderPanel() {
    preferredSize = new Dimension(50, 50)
    add(restNumLabel, BorderPanel.Position.West)
    add(searchTextField, BorderPanel.Position.Center)
    add(searchButton, BorderPanel.Position.East)
  }

  //val refinedClassTextLabel = new Label("修正クラス名：")
  val refinedClassTextLabel = new Label("Refined Class Name：")
  val refinedClassTextField = new TextField(30) {
    font = new Font("", Font.PLAIN, 30)
  }

  //val supClassTextLabel = new Label("上位クラス名：")
  val supClassTextLabel = new Label("Super Class Name：")
  val supClassTextField = new TextField(30) {
    font = new Font("", Font.PLAIN, 30)
  }

  val refinedClassPanel = new BorderPanel() {
    preferredSize = new Dimension(50, 50)
    add(refinedClassTextLabel, BorderPanel.Position.West)
    add(refinedClassTextField, BorderPanel.Position.Center)
  }

  val supClassPanel = new BorderPanel() {
    preferredSize = new Dimension(50, 50)
    add(supClassTextLabel, BorderPanel.Position.West)
    add(supClassTextField, BorderPanel.Position.Center)
  }

  val northPanel = new GridPanel(3, 1) {
    contents += searchPanel
    contents += refinedClassPanel
    contents += supClassPanel
  }

  val jwoClassListView = new ListView[String] {
    selection.intervalMode = ListView.IntervalMode.MultiInterval
  }
  jwoClassListView.listData = jwoClassList

  val jwoInstanceListView = new ListView[String] {
    selection.intervalMode = ListView.IntervalMode.Single
  }

  val correctJWOClassTable = new Table {
    override lazy val model = super.model.asInstanceOf[javax.swing.table.DefaultTableModel]
    //model.addColumn("修正前のクラス名")
    model.addColumn("Original Class")
    //model.addColumn("修正後のクラス名")
    model.addColumn("Refined Class")
    //model.addColumn("上位クラス名")
    model.addColumn("Super Class")
  }

  val wrongJWOClassListView = new ListView[String] {
    selection.intervalMode = ListView.IntervalMode.MultiInterval
  }

  val resultPanel = new GridPanel(2, 1) {
    contents += new ScrollPane(correctJWOClassTable) {
      //border = BorderFactory.createTitledBorder("正しいインスタンスを持つクラス")
      border = BorderFactory.createTitledBorder("Classes having correct instances")
    }
    contents += new ScrollPane(wrongJWOClassListView) {
      //border = BorderFactory.createTitledBorder("誤ったインスタンスを持つクラス")
      border = BorderFactory.createTitledBorder("Classes having wrong instances")
    }
  }

  val centerPanel = new GridPanel(1, 3) {
    contents += new ScrollPane(jwoClassListView) {
      //border = BorderFactory.createTitledBorder("クラスリスト")
      border = BorderFactory.createTitledBorder("Class List")
    }
    contents += new ScrollPane(jwoInstanceListView) {
      //border = BorderFactory.createTitledBorder("インスタンスリスト")
      border = BorderFactory.createTitledBorder("Instance List")
    }
    contents += resultPanel
  }

  //  val correctClassButton = new Button(Action("正しいクラス") {
  val correctClassButton = new Button(Action("Correct Class") {
    for (cls <- jwoClassListView.selection.items) {
      var refinedClass = refinedClassTextField.text
      var supClass = supClassTextField.text
      if (refinedClass.size == 0) {
        refinedClass = cls
      }
      if (supClass.size == 0) {
        supClass = "-"
      }
      correctJWOClassTable.model.addRow(Array[AnyRef](cls, refinedClass, supClass))
    }
    jwoClassList --= jwoClassListView.selection.items
    jwoClassListView.listData = jwoClassListView.listData.filterNot { c => jwoClassListView.selection.items.contains(c) }
    jwoInstanceListView.listData = ListBuffer()
    refinedClassTextField.text = ""
    supClassTextField.text = ""
    restNumLabel.text = jwoClassList.size.toString
  })

  //val wrongClassButton = new Button(Action("誤ったクラス") {
  val wrongClassButton = new Button(Action("Wrong class") {
    wrongJWOClassListView.listData ++= jwoClassListView.selection.items
    jwoClassList --= jwoClassListView.selection.items
    jwoClassListView.listData = jwoClassListView.listData.filterNot { c => jwoClassListView.selection.items.contains(c) }
    jwoInstanceListView.listData = ListBuffer()
    restNumLabel.text = jwoClassList.size.toString
  })

  //val undoButton = new Button(Action("元に戻す") {
  val undoButton = new Button(Action("Undo") {
    jwoClassListView.listData ++= wrongJWOClassListView.selection.items
    jwoClassList ++= wrongJWOClassListView.selection.items
    wrongJWOClassListView.listData = wrongJWOClassListView.listData.filterNot { c => wrongJWOClassListView.selection.items.contains(c) }
    for (selectedRow <- correctJWOClassTable.selection.rows.toList.reverse) {
      val selectedJWOClass = correctJWOClassTable.model.getValueAt(selectedRow, 0)
      jwoClassListView.listData = selectedJWOClass.toString :: jwoClassListView.listData.toList
      jwoClassList += selectedJWOClass.toString
      correctJWOClassTable.model.removeRow(selectedRow)
    }
    restNumLabel.text = jwoClassList.size.toString
  })

  val southPanel = new FlowPanel() {
    contents += correctClassButton
    contents += wrongClassButton
    contents += undoButton
  }

  val classInstanceDB = Database.forURL(url = "jdbc:sqlite:inputs_and_outputs/merged_class_instance_list.db", driver = "org.sqlite.JDBC")
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

  listenTo(jwoClassListView.selection)
  reactions += {
    case ListSelectionChanged(source, range, live) =>
      if (source == jwoClassListView) {
        if (jwoClassListView.selection.items.size == 1) {
          setInstanceList()
        }
      }
  }

  def saveClassInstanceRefinementResults(root: Component) = {
    val fileChooser = new FileChooser()
    fileChooser.showSaveDialog(root) match {
      case FileChooser.Result.Approve =>
        val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileChooser.selectedFile), "UTF-8"))
        for (i <- 0 until correctJWOClassTable.model.getRowCount()) {
          val jwoClass = correctJWOClassTable.model.getValueAt(i, 0)
          val refinedJWOClass = correctJWOClassTable.model.getValueAt(i, 1)
          val supClass = correctJWOClassTable.model.getValueAt(i, 2)
          writer.write(true + "\t" + jwoClass + "\t" + refinedJWOClass + "\t" + supClass)
          writer.newLine()
        }
        for (c <- wrongJWOClassListView.listData) {
          writer.write(false + "\t" + c + "\t-\t-")
          writer.newLine()
        }
        writer.close
      case FileChooser.Result.Cancel =>
      case FileChooser.Result.Error =>
    }
  }

  def openClassInstanceRefinementResults(root: Component) = {
    val fileChooser = new FileChooser()
    fileChooser.showOpenDialog(root) match {
      case FileChooser.Result.Approve =>
        val source = Source.fromFile(fileChooser.selectedFile, "utf-8")
        for (line <- source.getLines()) {
          val Array(isCorrect, jwoClass, refinedJWOClass, supClass) = line.split("\t")
          if (isCorrect == "true") {
            correctJWOClassTable.model.addRow(Array[AnyRef](jwoClass, refinedJWOClass, supClass))
          } else if (isCorrect == "false") {
            wrongJWOClassListView.listData = jwoClass :: wrongJWOClassListView.listData.toList
          }
          jwoClassList -= jwoClass
        }
        jwoClassListView.listData = jwoClassList
        restNumLabel.text = jwoClassList.size.toString
        println(jwoClassList)
      case FileChooser.Result.Cancel =>
      case FileChooser.Result.Error =>
    }
  }
  def getMenuBar: MenuBar = {
    new MenuBar {
      // val aMenu = new Menu("ファイル")
      val aMenu = new Menu("File")
      aMenu.contents += new MenuItem(Action("クラスーインスタンス関係洗練結果を開く") {
        openClassInstanceRefinementResults(this)
      })
      aMenu.contents += new MenuItem(Action("クラスーインスタンス関係洗練結果を保存") {
        saveClassInstanceRefinementResults(this)
      })
      aMenu.contents += new MenuItem(Action("終了") {
        System.exit(0)
      })
      contents += aMenu
    }
  }

  def top = new MainFrame {
    //  title = "クラスーインスタンス関係洗練ツール ver.2012.02.27"    
    title = "Class-instance relationships refinement tool"
    menuBar = getMenuBar
    contents = new BorderPanel() {
      add(northPanel, BorderPanel.Position.North)
      add(centerPanel, BorderPanel.Position.Center)
      add(southPanel, BorderPanel.Position.South)
    }
    size = new Dimension(1024, 700)
    centerOnScreen()
  }
}