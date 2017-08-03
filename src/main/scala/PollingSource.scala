package com.github.btmorr.tutorial

import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver

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
