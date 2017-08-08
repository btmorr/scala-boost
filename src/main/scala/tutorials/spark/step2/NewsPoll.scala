package com.github.btmorr
package tutorials.spark.step2

// Add at least one filter function
object NewsPoll extends App {
  import tutorials.spark.step0.ApiOps._
  import tutorials.spark.step0.SparkInit
  import tutorials.spark.step1.Schemas.Ops._

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
  articlesDF.select("title", "description")
    .filter( $"title".contains( "health" ) )
    .show()

  articlesDF.printSchema()

  sc.stop()
}
