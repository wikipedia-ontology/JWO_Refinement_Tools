package test
import org.scardf.Graph
import org.scardf.UriRef
import org.scardf.Vocabulary
import org.scardf.Branch
import org.scardf.RDF
import org.scardf.ObjSet

object PeopleVocabulary extends Vocabulary("http://person.eg#") {
  val Person = uriref("Person")
  val Hobby = uriref("Hobby")
  val Swimming = uriref("Swimming")
  val Science = uriref("Science")
  val likes = prop("Likes")
  val name = prop("name")
  val isMale = prop("IsMale")
  val height = propInt("Height")
  val given = prop("given")
  val family = prop("family")
}

object ScalaRDFTest {

  def main(args: Array[String]) {
    val john = UriRef("http://doe.eg#john")
    val g = Graph.build(john - (
      RDF.Type -> PeopleVocabulary.Person,
      PeopleVocabulary.isMale -> true,
      PeopleVocabulary.name -> Branch(PeopleVocabulary.given -> "John", PeopleVocabulary.family -> "Doe"),
      PeopleVocabulary.height -> 167,
      PeopleVocabulary.likes -> ObjSet(PeopleVocabulary.Swimming, PeopleVocabulary.Science)))
  }
}