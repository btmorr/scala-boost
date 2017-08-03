package com.github.btmorr.tutorial
package step2

// Store the results in a database
object NewsPoll extends App {
  import step0.SparkInit
  import step0.ApiOps._
  import step1.Schemas.Ops._

  // Before running this app, the NEWSAPI_KEY environment variable must be
  val newsApiKey = sys.env.getOrElse( "NEWSAPI_KEY", throw new Exception( "NEWSAPI_KEY environment variable must be set before running this application" ) )

  val sc = SparkInit.sparkContext

  val source = "bbc-news"
  val requestString = s"https://newsapi.org/v1/articles?source=$source&sortBy=top&apiKey=$newsApiKey"

  val rdd = sc.parallelize( Seq( requestString ) )
  val contentsRDD = rdd.map( makeNewsApiRequest )
  val articlesRDD = contentsRDD
    .map( deserialize )
    .flatMap( _.articles )
  val articles = articlesRDD.collect

  articles foreach prettyPrintArticle

  sc.stop()
}
