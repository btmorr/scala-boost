package tutorials.basics.step1

case class Article(
  title: String,
  author: String,
  publishedAt: String,
  description: String,
  url: String
) {
  def toDbRecord: String =
    Seq(title, author, publishedAt, description, url).mkString(Article.separator)
}

object Article {
  val separator = ";"

  val fromDbRecord: (String => Option[Article]) = input => {
    val splitInput = input.stripLineEnd.split(";")
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
