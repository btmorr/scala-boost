package tutorials.basics
package step3

import java.nio.file._

import step1.Article

import scala.io.Source

object Demo extends App {
  Database(sys.env.get("HOME").get + "/dbroot")("testdb").createTable("testtable")
}

object Ops {

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
    private var uuids: Set[Int] = tableDir.toFile.listFiles.toSet
      .map( _.getName.split("[.]").head.toInt )
    private var maxUUID = uuids.lastOption.getOrElse(-1)

    private def serialize(article: Article): String = ???

    private def deserialize(string: String): Option[Article] = ???

    private def saveToFile(filename: String): Boolean = ???

    def insert(article: Article): Boolean =
      if ( uuids contains article.uuid ) false
      else {
        val record = article.toDbRecord
        // write file
        // update uuids
        // update maxUUID
        true
      }

    def update(article: Article): Boolean = ???

    def delete(article: Article): Boolean = ???

    def get(uuid: Int): Option[Article] =
      if ( uuids contains uuid ) {
        val targetFile = tableDir.resolve( s"$uuid.$fileExtension" ).toString
        val contents = Source.fromFile( targetFile )
        Article.fromTSV( s"$uuid\t$contents" )
      } else None

  }
}
