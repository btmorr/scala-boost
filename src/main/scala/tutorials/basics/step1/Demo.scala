package tutorials.basics
package step1

object Demo extends App {
  val home = sys.env.getOrElse("HOME", "~")
  val filename = s"$home/code/scala-boost/data/basics_data.tsv"
  val rows = scala.io.Source.fromFile(filename).mkString.split("\n")

  val articleFromTSV: (String => Option[Article]) = input => {
    val splitInput = input.stripLineEnd.split("\t")
    splitInput match {
      case split if split.length == 6 => Some(Article(
        split(0), // head,
        split(1), // drop 1 head,
        split(2), // drop 2 head,
        split(3), // drop 3 head,
        split(4), // drop 4 head,
        split(5)  // drop 5 head
      ))
      case _ => None
    }
  }


  rows map articleFromTSV foreach println
}
