package com.github.btmorr
package tutorials.spark.step0

// Create a SparkContext, make a request to NewsAPI, print the raw result
object NewsPoll extends App {
  import ApiOps._

  // Before running this app, the NEWSAPI_KEY environment variable must be set
  val newsApiKey = sys.env.getOrElse( "NEWSAPI_KEY", throw new Exception( "NEWSAPI_KEY environment variable must be set before running this application" ) )

  val sc = SparkInit.sparkContext

  val source = "bbc-news"
  val requestString = s"https://newsapi.org/v1/articles?source=$source&sortBy=top&apiKey=$newsApiKey"

  val rdd = sc.parallelize( Seq( requestString ) )
  val contentsRDD = rdd.map( makeNewsApiRequest )

  contentsRDD.collect foreach println

  sc.stop()
}
