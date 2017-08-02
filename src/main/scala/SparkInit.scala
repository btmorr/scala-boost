package com.github.btmorr.tutorial

import org.apache.spark.streaming._
import org.apache.spark.{SparkConf, SparkContext}

object SparkInit {
  private val appName = "news-stream"
  private val master = "local[2]"
  private val conf = new SparkConf().setAppName( appName ).setMaster( master )

  lazy val sparkContext = new SparkContext( conf )

  lazy val streamingContext: (Int => StreamingContext) = batchFrequency => new StreamingContext( conf, Seconds( batchFrequency ) )
}
