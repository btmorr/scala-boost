package tutorials.basics.step1

case class Article(
  title: String,
  author: String,
  publishedAt: String,
  description: String,
  url: String
) {
  def toDbRecord: String =
    s"$title\t$author\t$publishedAt\t$description\t$url"
}

object Article {
  val fromDbRecord: (String => Option[Article]) = input => {
    val splitInput = input.stripLineEnd.split("\t")
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
