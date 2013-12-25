package utils
import com.hp.hpl.jena.rdf.model.ResourceFactory
import javax.swing.ImageIcon
import java.net.URL

object Utils {
  val skosNs = "http://www.w3.org/2004/02/skos/core#"
  val skosPrefLabel = ResourceFactory.createProperty(skosNs + "prefLabel")

  def getResource(res: String): URL = {
    return getClass().getClassLoader().getResource(res);
  }

  val classIcon0 = new ImageIcon(getResource("resources/class_icon_0.png"));
  val classIcon1 = new ImageIcon(getResource("resources/class_icon_1.png"));
  val classIcon2 = new ImageIcon(getResource("resources/class_icon_2.png"));
  val classIcon3 = new ImageIcon(getResource("resources/class_icon_3.png"));
  val classIcon4 = new ImageIcon(getResource("resources/class_icon_4.png"));
  val classIcon5 = new ImageIcon(getResource("resources/class_icon_5.png"));
  val classIcon6 = new ImageIcon(getResource("resources/class_icon_6.png"));
  val classIcon7 = new ImageIcon(getResource("resources/class_icon_7.png"));
}