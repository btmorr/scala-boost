//package tutorials.basics
//package step2
//
//object Demo extends App {
//  val home = sys.env.getOrElse("HOME", "~")
//  val filename = s"$home/code/scala-boost/data/basics_data.tsv"
//  val rows = scala.io.Source.fromFile(filename).mkString.split("\n")
//
//  import step1.Article
//
//  val articles = rows map Article.fromDbRecord
//  println("\n===>Articles:")
//  articles.flatten.foreach(println)
//
//  val isAboutBears: ( Article => Boolean ) = article =>
//    ( article.title contains "bear" ) || ( article.description contains "bear" )
//
//  val filteredArticles = articles.flatten.filter(isAboutBears)
//  println("\n===>Filtered articles:")
//  filteredArticles foreach println
//
//  val importantBits = filteredArticles.map( article => s"<a href=${article.url}>${article.title}</a>" )
//  println("\n===>Important bits:")
//  importantBits foreach println
//}
