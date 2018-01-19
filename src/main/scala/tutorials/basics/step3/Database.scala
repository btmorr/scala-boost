package tutorials.basics
package step3

import tutorials.basics.step1.Article

import scala.util.Try

case class Database(name: String) {
  private var tables: Set[Table] = Set.empty
  private var currentTable: Option[Table] = None
  def useTable(tableName: String): Unit = {
    val table = tables
      .flatMap( t => if (t.name == tableName) Some( t ) else None )
      .headOption
      .getOrElse( Table( tableName ) )
    tables = tables + table
    currentTable = Some( table )
  }

  // functions to make it possible for the app using the Database to call methods from the Table, even though
  // it is a private case class
  def insert(article: Article): Option[Int] = currentTable.flatMap( _.insert( article ) )
  def get(id: Int): Option[Article] = currentTable.flatMap( _.get( id ) )
}

private case class Table(name: String) {
  // Notice that this is a map of Int -> String. While a purely in-memory database _could_ contain actual Article
  // objects, we are replicating the behavior of a database that would store objects in files, so it has to save
  // them as strings and then turn them from strings back into objects
  private var records: Map[Int, String] = Map.empty

  def insert(article: Article): Option[Int] = {
    val nextID = records.keySet.max + 1
    // This Try isn't strictly necessary, but indicates that in a real database this operation could theoretically fail
    Try {
      val record: String = article.serialize()
      records = records + (nextID -> record)
      nextID
    }.toOption
  }

  def get(id: Int): Option[Article] =
    if ( records.keySet contains id ) {
      Article.deserialize( records( id ) )
    } else None
}
