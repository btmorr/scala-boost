package tutorials.basics
package step1

case class Article(
  title: String,
  author: String,
  publishedAt: String,
  description: String,
  url: String
) {
  def serialize(delimiter: String=Article.defaultDelimiter): String =
    Seq(title, author, publishedAt, description, url).mkString(delimiter)
}

object Article {
  val defaultDelimiter = "\t"

  def deserialize(input: String, delimiter: String=defaultDelimiter): Option[Article] = {
    val splitInput = input.stripLineEnd.split(delimiter)
    splitInput match {
      case split if split.length == 5 => Some( Article(
        split(0), // head,
        split(1), // drop 1 head,
        split(2), // drop 2 head,
        split(3), // drop 3 head,
        split(4) // drop 4 head
      ))
      case _ => None
    }
  }

}
