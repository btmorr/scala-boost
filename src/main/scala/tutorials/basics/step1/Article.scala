package tutorials.basics.step1

case class Article(
  uuid: Int,
  title: String,
  author: String,
  publishedAt: String,
  description: String,
  url: String
) {
  val toDbRecord: (Article => String) = article =>
    s"${article.title}\t${article.author}\t${article.publishedAt}\t${article.description}\t${article.url}"
}

object Article {
  val fromTSV: (String => Option[Article]) = input => {
    val splitInput = input.stripLineEnd.split("\t")
    splitInput match {
      case split if split.length == 6 => Some( Article(
        split(0).toInt, // head,
        split(1), // drop 1 head,
        split(2), // drop 2 head,
        split(3), // drop 3 head,
        split(4), // drop 4 head,
        split(5)  // drop 5 head
      ))
      case _ => None
    }
  }

}
