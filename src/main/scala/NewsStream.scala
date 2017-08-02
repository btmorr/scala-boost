package com.github.btmorr.tutorial

import com.github.btmorr.tutorial.Schemas.{Article, NewsApiResponse}

/* Common functionality used in both NewsPoll and NewsStream defined here
 */
object NewsOps {
  def makeNewsApiRequest(url: String): String = {
    val client = new org.apache.http.impl.client.DefaultHttpClient()
    val request = new org.apache.http.client.methods.HttpGet( url )
    val response = client.execute( request )
    val handler = new org.apache.http.impl.client.BasicResponseHandler()
    handler.handleResponse( response ).trim
  }

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

/* This version of the app makes a single request and then processes it. For a version that makes recurring requests and
 * handles the results as an input stream, see `NewsStream` below.
 */
object NewsPoll extends App {
  import NewsOps._

  // Before running this app, the NEWSAPI_KEY environment variable must be
  val newsApiKey = sys.env.getOrElse( "NEWSAPI_KEY", throw new Exception( "NEWSAPI_KEY environment variable must be set before running this application" ) )

  val sc = SparkInit.sparkContext

  val source = "bbc-news"
  val requestString = s"https://newsapi.org/v1/articles?source=$source&sortBy=top&apiKey=$newsApiKey"

  val rdd = sc.parallelize( Seq( requestString ) )
  val contentsRDD = rdd.map( makeNewsApiRequest )
  val articlesRDD = contentsRDD
    .map( Schemas.Ops.deserialize )
    .flatMap( _.articles )
  val articles = articlesRDD.collect

  articles foreach prettyPrintArticle

  sc.stop()
}

/* This version of the app makes repeated requests to the API on a timer and processes the result as a stream.
 */
object NewsStream extends App {
  import NewsOps._
  // Before running this app, the NEWSAPI_KEY environment variable must be
  val newsApiKey = sys.env.getOrElse( "NEWSAPI_KEY", throw new Exception( "NEWSAPI_KEY environment variable must be set before running this application" ) )

  val batchFrequencySeconds = 1
  val ssc = SparkInit.streamingContext( batchFrequencySeconds )

  val source = "bbc-news"
  val requestString = s"https://newsapi.org/v1/articles?source=$source&sortBy=top&apiKey=$newsApiKey"

  // make a request to NewsAPI once per minute
  val requestFrequencySeconds = 60
  val uriStream = ssc.receiverStream(new PollingSource(requestFrequencySeconds, requestString))

  val articleStream = for {
    uri <- uriStream
    respString = makeNewsApiRequest( uri )
    respObj = Schemas.Ops.deserialize(respString)
    article <- respObj.articles
  } yield article

  // todo: replace this print with an updateIfNotExists to a database
  articleStream.foreachRDD( rdd => {
    rdd collect() foreach prettyPrintArticle
  })

  ssc.start()
  ssc.awaitTerminationOrTimeout(5 * 5 * 1000)
}
