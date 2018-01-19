package tutorials.basics
package step2

import step1.Article

object Demo extends App {
  if( args.length != 1) println("Please input exactly one argument for the input file")
  else {
    val filename = args(0)
    val rows = scala.io.Source.fromFile(filename).mkString.split("\n")


    val articles = rows.map(Article.deserialize(_))
    println("\n===>Articles:")
    articles.flatten.foreach(println)

    val isAboutBears: ( Article => Boolean ) = article =>
      ( article.title contains "bear" ) || ( article.description contains "bear" )

    val filteredArticles = articles.flatten.filter(isAboutBears)
    println("\n===>Filtered articles:")
    filteredArticles foreach println

    val importantBits = filteredArticles.map( article => s"<a href=${article.url}>${article.title}</a>" )
    println("\n===>Important bits:")
    importantBits foreach println
  }
}
