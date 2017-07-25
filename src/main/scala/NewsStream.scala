package com.github.btmorr.tutorial

import org.apache.spark.{ SparkContext, SparkConf }

object NewsStream extends App {
  // Before running this app, the NEWSAPI_KEY environment variable must be
  val newsApiKey = sys.env.getOrElse( "NEWSAPI_KEY", throw new Exception( "NEWSAPI_KEY environment variable must be set before running this application" ) )

  val appName = "news-stream"
  val master = "local"
  val conf = new SparkConf().setAppName( appName ).setMaster( master )
  val sc = new SparkContext( conf )

  val source = "bbc-news"
  val requestString = s"https://newsapi.org/v1/articles?source=$source&sortBy=top&apiKey=$newsApiKey"

  val rdd = sc.parallelize( Seq( requestString ) )
  val contents = rdd.map(url => {
    val client = new org.apache.http.impl.client.DefaultHttpClient()
    val request = new org.apache.http.client.methods.HttpGet( url )
    val response = client.execute( request )
    val handler = new org.apache.http.impl.client.BasicResponseHandler()
    handler.handleResponse( response ).trim.toString
  })

  val results = contents.map( Schemas.Ops.deserialize ).collect()
  results foreach println

  sc.stop()
}
