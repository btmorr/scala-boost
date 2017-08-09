package tutorials.basics
package step1

object Demo extends App {
  val home = sys.env.getOrElse("HOME", "~")
  val filename = s"$home/code/scala-boost/data/basics_data.tsv"
  val rows = scala.io.Source.fromFile(filename).mkString.split("\n")


  rows map Article.fromTSV foreach println
}
