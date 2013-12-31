package ontology_debugging_tool

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
import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.rdf.model.Property
import com.hp.hpl.jena.vocabulary.RDFS
import scala.swing.SimpleSwingApplication
import scala.swing.MainFrame
import java.awt.Dimension
import scala.swing.BorderPanel
import scala.swing.SplitPane
import javax.swing.JTree
import scala.swing.ScrollPane
import javax.swing.tree.TreeModel
import scala.swing.Scrollable
import scala.swing.Component
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import scala.swing.Orientation

object OntologyDebuggingTool extends SimpleSwingApplication {

  val propertyRefinementPanel = new PropertyRefinementPanel()
  val classTreePanel = new ClassTreePanel(propertyRefinementPanel)

  val attributePanel = new BorderPanel() {
  }

  def top = new MainFrame {
    title = "オントロジーデバッギングツール ver.2012.03.19"
    //    menuBar = getMenuBar
    contents = new BorderPanel() {
      val splitPane = new SplitPane(Orientation.Vertical) {
        dividerLocation = 400
        oneTouchExpandable = true
        leftComponent = new ScrollPane(classTreePanel)
        rightComponent = propertyRefinementPanel
      }
      add(splitPane, BorderPanel.Position.Center)
    }
    size = new Dimension(1024, 700)
    centerOnScreen()
  }

}

