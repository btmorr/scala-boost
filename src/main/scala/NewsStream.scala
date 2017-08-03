package com.github.btmorr.tutorial

/* This version of the app makes repeated requests to the API on a timer and processes the result as a stream.
 */
object NewsStream extends App {
  import step0.ApiOps._
  import step0.SparkInit
  import step1.Schemas

  // Before running this app, the NEWSAPI_KEY environment variable must be
  val newsApiKey = sys.env.getOrElse( "NEWSAPI_KEY", throw new Exception( "NEWSAPI_KEY environment variable must be set before running this application" ) )

  val batchFrequencySeconds = 5
  val ssc = SparkInit.streamingContext( batchFrequencySeconds )

  val source = "bbc-news"
  val requestString = s"https://newsapi.org/v1/articles?source=$source&sortBy=top&apiKey=$newsApiKey"

  // make a request to NewsAPI once per minute
  val requestFrequencySeconds = 10
  val uriStream = ssc.receiverStream(new PollingSource(requestFrequencySeconds, requestString))

  val articleStream = for {
    uri <- uriStream
    respString = makeNewsApiRequest( uri )
    respObj = Schemas.Ops.deserialize(respString)
    article <- respObj.articles
  } yield article

  // todo: replace this print with an updateIfNotExists to a database
  articleStream.foreachRDD( rdd => {
    rdd collect() foreach Schemas.prettyPrintArticle
  })

  ssc.start()
  /* `awaitTerminationOrTimeout` times out after the specified amount of time, even if operations are being conducted.
   * Using `awaitTermination` to allow the operation to run until Ctl-C is pressed.
   */
  //ssc.awaitTerminationOrTimeout(10 * 5 * 1000)
  ssc.awaitTermination

  val stopSparkContext = true
  val shutdownGracefully = true
  ssc.stop(stopSparkContext, shutdownGracefully)
}
