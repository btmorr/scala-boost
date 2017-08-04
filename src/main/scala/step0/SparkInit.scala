package com.github.btmorr.tutorial
package step0

import org.apache.spark.streaming._
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession

object SparkInit {
  private val appName = "news-stream"
  private val master = "local[3]"
  private val conf = new SparkConf().setAppName( appName ).setMaster( master )

  lazy val sparkContext = new SparkContext( conf )

  lazy val streamingContext: (Int => StreamingContext) = batchFrequency => new StreamingContext( conf, Seconds( batchFrequency ) )

  lazy val sparkSession: SparkSession = SparkSession
    .builder()
    .appName( appName )
    .master( master )
    .getOrCreate()
}
