package com.github.btmorr
package tutorials.spark.step1

/* Common functionality used in both NewsPoll and NewsStream defined here
 */
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
    import io.sphere.json._
    import io.sphere.json.generic._

    implicit val jsonArticle: JSON[Article] = jsonProduct(Article.apply _)
    implicit val jsonNewsApiResponse: JSON[NewsApiResponse] = jsonProduct(NewsApiResponse.apply _)

    def deserialize(json: String): NewsApiResponse =
      fromJSON[NewsApiResponse](json)
        .toOption.getOrElse(
        NewsApiResponse( "Deserialization failure", "", "", Nil )
      )

    def prettyPrintResponse(apiResponse: NewsApiResponse) = println(
      s"""
         |
     |Response:
         |  Status: ${apiResponse.status}
         |  Source: ${apiResponse.source}
         |  Articles: ${apiResponse.articles.map( _.title).mkString(", ")}
         |
    """.stripMargin)

    def prettyPrintArticle(article: Article) = println(
      s"""
         |
     |Article:
         |  Title: ${article.title}
         |  Author: ${article.author}
         |  Date: ${article.publishedAt}
         |  URL: ${article.url}
         |  Description: ${article.description}
         |
    """.stripMargin)
  }
}
