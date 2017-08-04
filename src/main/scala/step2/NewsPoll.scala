package com.github.btmorr.tutorial
package step2

// Add at least one filter function
object NewsPoll extends App {
  import step0.SparkInit
  import step0.ApiOps._
  import step1.Schemas.Ops._

  // Before running this app, the NEWSAPI_KEY environment variable must be set
  val newsApiKey = sys.env.getOrElse( "NEWSAPI_KEY", throw new Exception( "NEWSAPI_KEY environment variable must be set before running this application" ) )

  val session = SparkInit.sparkSession
  val sc = session.sparkContext
  val sqlContext = session.sqlContext

  val source = "bbc-news"
  val requestString = s"https://newsapi.org/v1/articles?source=$source&sortBy=top&apiKey=$newsApiKey"

  val rdd = sc.parallelize( Seq( requestString ) )
  val contentsRDD = rdd.map( makeNewsApiRequest )
  val articlesRDD = contentsRDD
    .map( deserialize )
    .flatMap( _.articles )

  val articlesDF = sqlContext.createDataFrame(articlesRDD)

  // This import is needed to use the $-notation
  import session.implicits._

  articlesDF.printSchema()
  val filteredArticlesDF = articlesDF.select("title", "description")
    .filter( $"title".contains( "health" ) )

  println("\nBefore persistence:")
  filteredArticlesDF.show()

  filteredArticlesDF.write.saveAsTable( "filtered_articles" )

  val reloadDF = sqlContext.read.table( "filtered_articles" )

  println("\nAfter persistence:")
  reloadDF.write.saveAsTable( "filtered_articles" )

  sc.stop()
}
