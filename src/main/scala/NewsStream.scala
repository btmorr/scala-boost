package com.github.btmorr.tutorial

import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming._
import org.apache.spark.streaming.receiver._

class PollingSource(sleepSeconds: Int, uri: String) extends Receiver[String](StorageLevel.MEMORY_AND_DISK_2) {

  def onStart() {
    // Start the thread that receives data over a connection
    new Thread("Dummy Source") {
      override def run() { receive() }
    }.start()
  }

  def onStop() {
    // There is nothing much to do as the thread calling receive()
    // is designed to stop by itself isStopped() returns false
  }

  /** Create a socket connection and receive data until receiver is stopped */
  private def receive() {
    while(!isStopped()) {
      store(uri) // this just takes whatever the uri is and emits it over and over
      Thread.sleep( sleepSeconds * 1000 )
    }
  }
}

object NewsPoll extends App {
  // Before running this app, the NEWSAPI_KEY environment variable must be
  val newsApiKey = sys.env.getOrElse( "NEWSAPI_KEY", throw new Exception( "NEWSAPI_KEY environment variable must be set before running this application" ) )

  val appName = "news-stream"
  val master = "local[2]"
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
    handler.handleResponse( response ).trim
  })

  val articlesRDD = contents.map( Schemas.Ops.deserialize ).map( _.articles )
  val articles = articlesRDD.collect

  articles foreach println

  sc.stop()
}

object NewsStream extends App {
  // Before running this app, the NEWSAPI_KEY environment variable must be
  val newsApiKey = sys.env.getOrElse( "NEWSAPI_KEY", throw new Exception( "NEWSAPI_KEY environment variable must be set before running this application" ) )

  val appName = "news-stream"
  val master = "local[2]"
  val conf = new SparkConf().setAppName( appName ).setMaster( master )
  val ssc = new StreamingContext( conf, Seconds(1) )

  val source = "bbc-news"
  val requestString = s"https://newsapi.org/v1/articles?source=$source&sortBy=top&apiKey=$newsApiKey"

  val uriStream = ssc.receiverStream(new PollingSource(1, requestString))
  val articleStream = for {
    uri <- uriStream
    client = new org.apache.http.impl.client.DefaultHttpClient()
    request = new org.apache.http.client.methods.HttpGet( uri )
    response = client.execute( request )
    handler = new org.apache.http.impl.client.BasicResponseHandler()
    respString = handler.handleResponse( response ).trim
    respObj = Schemas.Ops.deserialize(respString)
    article <- respObj.articles
  } yield article

  articleStream.foreachRDD( rdd => {
    rdd.collect().foreach( art =>
      println(
        s"""
           |
           |Article:
           |  Title: ${art.title}
           |  Author: ${art.author}
           |  Date: ${art.publishedAt}
           |  URL: ${art.url}
           |  Description: ${art.description}
           |
         """.stripMargin)
    )
  })

  ssc.start()
  ssc.awaitTerminationOrTimeout(5 * 5 * 1000)
}
