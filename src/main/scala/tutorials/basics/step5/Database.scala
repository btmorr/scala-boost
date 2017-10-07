//package tutorials.basics
//package step5
//
//import java.io.{ File, PrintWriter }
//import java.nio.file._
//
//import scala.io.Source
//import step1.Article
//
//case class Database(rootDir: String)(name: String) {
//  // all files are saved with the same extension (.rec for record)
//  val fileExtension: String = "rec"
//
//  // If a database by this name exists, open it, else create an empty db with this name
//  val databaseDir: Path = Files.createDirectories( Paths.get( rootDir, name ) )
//  println( s"Database root: $databaseDir" )
//
//  def useTable(tableName: String): Table = Table( this )( tableName )
//}
//
//case class Table(db: Database)(name: String) {
//  // If a table by this name exists, open it, else create an empty table with this name
//  private val tableDir: Path = Files.createDirectories( db.databaseDir.resolve( name ) )
//
//  /* - List the files in the directory, and convert the list to a Seq
//   * - Extract the name of the file (without the file extension), and convert each to an Int
//   * - Take the highest number in the list, or -1 if it is empty (the number is incremented
//   *     before assigning the next UUID, so -1 for empty results in a 0-origin Seq of UUIDs
//   *     for each table
//   *
//   * Note: the current implementation will fail if any file in the directory has a non-numerical
//   * filename, after removing the file extension.
//   */
//  private val filesOnLoad: Set[File] = tableDir.toFile.listFiles.toSet
//  private var uuids: Set[Int] = filesOnLoad.map( _.getName.split("[.]").head.toInt )
//  private var maxUUID = uuids.lastOption.getOrElse(-1)
//
//  private def serialize(article: Article): String = article.toDbRecord
//  private def deserialize(string: String): Option[Article] = Article.fromDbRecord( string )
//
//  private def saveToFile(path: Path)(contents: String): Boolean = {
//    try {
//      val writer = new PrintWriter( path.toFile )
//      writer.print(contents + "\n")
//      writer.close()
//      true
//    } catch {
//      case e: Exception =>
//        println(s"Writing failed for record: $contents - Path: $path - Exception: $e")
//        false
//    }
//  }
//
//  def insert(article: Article): Int = {
//    val record = article.toDbRecord
//    val path = tableDir.resolve( s"${maxUUID + 1}.${db.fileExtension}" )
//    val successful: Boolean = saveToFile( path )( record )
//    if( successful ) {
//      maxUUID = maxUUID + 1
//      uuids = uuids + maxUUID
//      maxUUID
//    } else -1
//  }
//
//  def update(uuid: Int)(article: Article): Boolean = {
//    if( uuids contains uuid ) {
//      ??? // overwrite file
//      true
//    } else {
//      false
//    }
//  }
//
//  def delete(uuid: Int): Boolean = {
//    if( uuids contains uuid ) {
//      ??? // delete file
//      true
//    } else {
//      false
//    }
//  }
//
//  def get(uuid: Int): Option[Article] =
//    if ( uuids contains uuid ) {
//      val targetFile = tableDir.resolve( s"$uuid.${db.fileExtension}" ).toString
//      val contents = Source.fromFile( targetFile ).mkString
//      Article.fromDbRecord( contents )
//    } else None
//
//}
