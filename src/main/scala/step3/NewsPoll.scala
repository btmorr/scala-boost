package com.github.btmorr.tutorial
package step3

import org.apache.spark.sql.DataFrame

// Store results to a database
object NewsPoll extends App {
  import step0.SparkInit
  import step0.ApiOps._
  import step1.Schemas.Ops._

  case class FlagRecord(key: String, value: String)

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

  /* Set this as a flag to use in the filter function later. It's slightly artificial to write this and then
   * immediately read it, but the write would normally be done in a different app, so this is useful to do for
   * practice.
   */
  // try to read the table. if it doesn't exist, add the record
  val flagTableName = "flags"
  val flagTable: DataFrame = try {
    // why doesn't this pass when the table exists? (successively fails on attempt to write)
    // Is it because the directory is empty? (that might be another problem...
    val table = sqlContext.read.table( flagTableName )
    println("====>Flag table exists, yay!")
    table
  } catch {
    case e: org.apache.spark.sql.catalyst.analysis.NoSuchTableException =>
      println("====>Flag table doesn't exist, populating it")
      val filterCriteriaDF = sqlContext
        .createDataset(
          Seq( FlagRecord( "filterString", "health" ) )
        ).toDF
      println("Writing to table")
      filterCriteriaDF.write.saveAsTable( flagTableName )
      println("Done writing to table")
      filterCriteriaDF
  }

  val keyword = flagTable
    .select("key", "value")
    .as[FlagRecord]
    .filter( _.key == "filterString" )
    .collect()
    .headOption

  // if a filter criteria is found, filter, else return the whole dataset
  val filteredArticlesDF = keyword match {
    case Some(kw) => articlesDF
      .select("title", "description")
      .filter( ( $"title" contains kw.value ) || ( $"description" contains kw.value ) )
    case None => articlesDF
  }

  println("\nBefore persistence:")
  filteredArticlesDF.show()

  // todo: still need to read the article table first and drop duplicates
  val articleTableName = "filtered_articles"
  filteredArticlesDF.write.insertInto( articleTableName )

  val reloadDF = sqlContext.read.table( articleTableName )

  println("\nAfter persistence:")
  reloadDF.write.saveAsTable( "filtered_articles" )

  // todo: move this and all other stops to shutdown hooks that still happen in the case of exceptions
  sc.stop()

}
