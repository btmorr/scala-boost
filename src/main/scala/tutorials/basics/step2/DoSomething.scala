package tutorials.basics
package step2

object DoSomething extends App {
  val home = sys.env.getOrElse("HOME", "~")
  val filename = s"$home/code/scala-boost/data/basics_data.tsv"
  val rows = scala.io.Source.fromFile(filename).mkString.split("\n")

  import step1.Schemas._

  val articleFromTSV: (String => Option[Article]) = input => {
    val splitInput = input.stripLineEnd.split("\t")
    splitInput match {
      case split if split.length == 6 => Some(Article(
        split(0),
        split(1),
        split(2),
        split(3),
        split(4),
        split(5)
      ))
      case _ => None
    }
  }

  val articles = rows map articleFromTSV
  println("\n===>Articles:")
  articles.flatten.foreach(println)

  val isAboutBears: ( Article => Boolean ) = article =>
    ( article.title contains "bear" ) || ( article.description contains "bear" )

  val filteredArticles = articles.flatten.filter(isAboutBears)
  println("\n===>Filtered articles:")
  filteredArticles foreach println

  val importantBits = filteredArticles.map( article => s"<a href=${article.url}>${article.title}</a>" )
  println("\n===>Important bits:")
  importantBits foreach println
}
