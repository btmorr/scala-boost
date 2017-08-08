package tutorials.basics.step1

object Schemas {
  case class Article(
    uuid: String,
    title: String,
    author: String,
    publishedAt: String,
    description: String,
    url: String
  )
}
