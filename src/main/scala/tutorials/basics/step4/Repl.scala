package tutorials.basics
package step4

import tutorials.basics.step1.Article
import tutorials.basics.step3.Database

import scala.util.Try

object Repl extends App {
  var exitFlag = false
  val databases: Map[String, Database] = Map.empty
  var currentDb: Option[Database] = None

  println("Welcome to NewbieDB! Type 'help' for a list of commands, or 'exit' to quit (commands are not case-sensitive).")
  while( !exitFlag ) {
    print("> ")
    val input = scala.io.StdIn.readLine.stripLineEnd.split(" ")

    input.head.toLowerCase match {
      case "help" => println(
        """
          |Valid commands:
          |  help            -  Display this screen
          |  exit            -  Leave the application
          |  usedb <name>    -  Create/open a database by name
          |  usetable <name> -  Create/open a table within the current database by name
          |  insert <record> -  Add a record to the database. Records must be in the format:
          |                       Title;Author;PublishedAt;Description;URL
          |                     If successful, this command will display the ID of the record
          |                     that has been written. Example:
          |                       > insert title;author;date;desc;url
          |                       123
          |                       >
          |  get <uuid>      -  Get a record from the database by ID. To retrieve the record
          |                     written in the example above:
          |                       > get 123
          |                       title;author;date;desc;url
          |                       >
          |
        """.stripMargin)

      case "usedb" =>
        val dbName = input.tail.mkString("_")
        if( databases.contains( dbName ) ) {
          currentDb = Some( databases( dbName ) )
          println( s"Using database $dbName" )
        } else {
          val newDb = Database( dbName )
          databases ++ Seq(( dbName, newDb ))
          currentDb = Some( newDb )
          println( s"Created database $dbName" )
        }

      case "usetable" =>
        val tableName = input.tail.mkString("_")
        currentDb match {
          case Some( db ) =>
            db.useTable( tableName )
            println( s"Using table $tableName" )
          case None =>
            println( "No database selected--please enter `usedb <dbname>` first" )
        }

      case "insert" =>
        currentDb match {
          case Some( db ) =>
            val tail = input.tail.head
            val article = Article.deserialize(tail)
            article match {
              case Some(art) =>
                db.insert( art ) match {
                  case Some( id ) => println( id )
                  case None => println( "No table selected--please enter `usetable <tablename>` first" )
                }
              case None =>
                println( s"Incorrectly formatted record: $tail")
            }
          case None =>
            println( "No database selected--please enter `usedb <dbname>` first" )
        }

      case "get" =>
        currentDb match {
          case Some( db ) =>
            val tail = input.tail.head
            val id: Option[Int] = Try { tail.toInt }.toOption
            id match {
              case Some(i) =>
                db.get( i ) match {
                  case Some( article ) => println( article.serialize )
                  case None => println( s"No article found for id: $i. Have you selected a table (`usetable <tablename>`)?")
                }
              case None =>
                println( s"Get failed. $tail is not an integer" )
            }
          case None =>
            println( "No database selected--please enter `usedb <dbname>` first" )
        }

      case "exit" => exitFlag = true

      case other => println( s"Invalid input: $other" )
    }
  }
}
