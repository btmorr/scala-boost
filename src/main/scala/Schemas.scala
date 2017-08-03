package com.github.btmorr.tutorial

object Schemas {
  case class Article(
    author: String,
    title: String,
    description: String,
    url: String, // URI
    urlToImage: String, // URI
    publishedAt: String // DateTime
    )
  
  case class NewsApiResponse (
    status: String,
    source: String,
    sortBy: String,
    articles: Seq[Article]
    )

  object Ops {
    import io.sphere.json.generic._
    import io.sphere.json._

    implicit val jsonArticle: JSON[Article] = jsonProduct(Article.apply _)
    implicit val jsonNewsApiResponse: JSON[NewsApiResponse] = jsonProduct(NewsApiResponse.apply _)
    
    def deserialize(json: String): NewsApiResponse =
      fromJSON[NewsApiResponse](json)
        .toOption.getOrElse(
          NewsApiResponse( "Deserialization failure", "", "", Nil )
        )
  }
}
