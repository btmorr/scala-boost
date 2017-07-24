package com.github.btmorr.tutorial

import io.sphere.json.generic._
import io.sphere.json._

object Schemas {
  
  // case class Article(
  //   author: String,
  //   title: String,
  //   description: String,
  //   url: String, // URI
  //   urlToImage: String, // URI
  //   publishedAt: String // DateTime
  //   )
  
  // case class Articles( @JSONEmbedded sa: Seq[Article])

  case class NewsApiResponse (
    status: String,
    source: String,
    sortBy: String,
    articles: String
    )

  object NewsApiResponse {
    implicit val json: JSON[NewsApiResponse] = jsonProduct(NewsApiResponse.apply _)
  }
  
}

object Implicits {
  import Schemas._

  // implicit val json0 = jsonProduct(Article.apply _)
  // implicit val json1 = jsonProduct(Articles.apply _)
  // implicit val json = jsonProduct(NewsApiResponse.apply _)

  def deserialize(json: String) = 
    fromJSON[NewsApiResponse](json).toOption

  // def write[A](a: A): String = {
  //   val res = a match {
  //     case evt: UserEvent => eventJson.write(evt)
  //     case cmd: UserCommand => commandJson.write(cmd)
  //     case other => throw new RuntimeException("What the hell? " + other)
  //   }

  //   res match {
  //     case JNothing => "{}"
  //     case jval => compact(render(jval))

  //   }
  // }
}
