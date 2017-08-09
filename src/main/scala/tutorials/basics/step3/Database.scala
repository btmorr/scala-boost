package tutorials.basics
package step3

import java.nio.file._

import step1.Article

import scala.io.Source

object Demo extends App {
  Database(sys.env.get("HOME").get + "/dbroot")("testdb").createTable("testtable")
}

object Ops {
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
}
case class Database(rootDir: String)(name: String) {
  private val fileExtension = "rec"

  // If a database by this name exists, open it, else create an empty db with this name
  private val databaseDir = Files.createDirectories( Paths.get( rootDir, name ) )
  println(s"Database root: $databaseDir")

  def createTable(name: String): Table = Table(name)

  case class Table(name: String) {
    // If a table by this name exists, open it, else create an empty table with this name
    private val tableDir = Files.createDirectories( databaseDir.resolve( name ) )

    /* - List the files in the directory, and convert the list to a Seq
     * - Extract the name of the file (without the file extension), and convert each to an Int
     * - Take the highest number in the list, or -1 if it is empty (the number is incremented
     *     before assigning the next UUID, so -1 for empty results in a 0-origin Seq of UUIDs
     *     for each table
     *
     * Note: the current implementation will fail if any file in the directory has a non-numerical
     * filename, after removing the file extension.
     */
    private var uuids: Seq[Int] = tableDir.toFile.listFiles.toSeq
      .map( _.getName.split("[.]").head.toInt )
    private var maxUUID = uuids.headOption.getOrElse(-1)

    private def serialize(article: Article): String = ???

    private def deserialize(string: String): Option[Article] = ???

    private def saveToFile(filename: String): Boolean = ???

    def insert(article: Article): Boolean = ???

    def update(article: Article): Boolean = ???

    def delete(article: Article): Boolean = ???

    def get(uuid: Int): Option[Article] =
      if ( uuids contains uuid ) {
        val targetFile = tableDir.resolve( s"$uuid.$fileExtension" ).toString
        val contents = Source.fromFile( targetFile )
        Ops.articleFromTSV( s"$uuid\t$contents" )
      } else None

  }
}
