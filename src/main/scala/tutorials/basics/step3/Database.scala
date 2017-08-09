package tutorials.basics
package step3

import java.io.{File, PrintWriter}
import java.nio.file._

import scala.io.Source
import step1.Article

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

    private def serialize(article: Article): String = article.toDbRecord

    private def deserialize(string: String): Option[Article] = Article.fromDbRecord( string )

    private def saveToFile(filename: String)(contents: String): Boolean = {
      try {
        val writer = new PrintWriter( new File( filename ) )
        writer.print(contents)
        writer.close()
        true
      } catch {
        case e: Exception =>
          println(s"Writing failed for record: $contents - Filename: $filename - Exception: $e")
          false
      }
    }

    def insert(article: Article): Boolean = {
      val record = article.toDbRecord
      val fileName = s"${maxUUID + 1}.$fileExtension"
      val successful: Boolean = saveToFile(fileName)(record)
      if( successful ) {
        maxUUID = maxUUID + 1
        uuids = uuids + maxUUID
      }
      successful
    }

    def update(uuid: Int)(article: Article): Boolean = {
      if( uuids contains uuid ) {
        ??? // overwrite file
        true
      } else {
        false
      }
    }

    def delete(uuid: Int): Boolean = {
      if( uuids contains uuid ) {
        ??? // delete file
        true
      } else {
        false
      }
    }

    def get(uuid: Int): Option[Article] =
      if ( uuids contains uuid ) {
        val targetFile = tableDir.resolve( s"$uuid.$fileExtension" ).toString
        val contents = Source.fromFile( targetFile ).mkString
        Article.fromDbRecord( contents )
      } else None

  }
}
