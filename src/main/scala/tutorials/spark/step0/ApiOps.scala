package com.github.btmorr
package tutorials.spark.step0

object ApiOps {
  def makeNewsApiRequest(url: String): String = {
    val client = new org.apache.http.impl.client.DefaultHttpClient()
    val request = new org.apache.http.client.methods.HttpGet(url)
    val response = client.execute(request)
    val handler = new org.apache.http.impl.client.BasicResponseHandler()
    val res = handler.handleResponse(response).trim
    println(s"====> Res: ${res.take(80)}...")
    res
  }

}
