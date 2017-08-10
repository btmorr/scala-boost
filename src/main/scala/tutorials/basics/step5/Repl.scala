package tutorials.basics
package step5

import step1.Article

import scala.util.Try

object Repl extends App {
  var exitFlag = false
  val dbRootDir = sys.env("HOME") + "/.newbiedb"

  val databases: Map[String, Database] = Map.empty
  var currentDb: Option[Database] = None
  var currentTable: Option[Table] = None

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
          |                       Title;;Author;;PublishedAt;;Description;;URL
          |                     If successful, this command will display the UUID of the record
          |                     that has been written. Example:
          |                       > insert title;;author;;date;;desc;;url
          |                       123
          |                       >
          |  get <uuid>      -  Get a record from the database by UUID. To retrieve the record
          |                     written in the example above:
          |                       > get 123
          |                       title;;author;;date;;desc;;url
          |                       >
          |
        """.stripMargin)

      case "usedb" =>
        val dbName = input.tail.mkString("_")
        if( databases.contains( dbName ) ) {
          currentDb = Some( databases( dbName ) )
          println( s"Using database $dbName" )
        } else {
          val newDb = Database( dbRootDir )( dbName )
          databases ++ Seq(( dbName, newDb ))
          currentDb = Some( newDb )
          println( s"Created database $dbName" )
        }

      case "usetable" =>
        val tableName = input.tail.mkString("_")
        currentDb match {
          case Some( db ) =>
            currentTable = Some( Table( db )( tableName ) )
            println( s"Using table $tableName" )
          case None =>
            println( "No database selected--please enter `usedb <dbname>` first" )
        }

      case "insert" =>
        currentTable match {
          case Some( table ) =>
            val rebuiltTail = input.tail.mkString(" ")
            val record = rebuiltTail.split(";;").mkString("\t")
            val article = Article.fromDbRecord( record )
            article match {
              case Some(art) =>
                val newUUID = table.insert( art )
                println(newUUID)
              case None =>
                println( s"Insert failed on record: $rebuiltTail")
            }
          case None =>
            println( "No table selected--please enter `usetable <tablename>` first" )
        }

      case "get" =>
        currentTable match {
          case Some( table ) =>
            val rebuiltTail = input.tail.mkString(" ")
            val uuid = Try { rebuiltTail.toInt }.toOption
            uuid match {
              case Some(id) =>
                val r = table.get( id ).get
                println( s"${r.title};;${r.author};;${r.publishedAt};;${r.description};;${r.url}" )
              case None =>
                println( s"Get failed. $rebuiltTail is not an integer" )
            }
          case None =>
            println( "No table selected--please enter `usetable <tablename>` first" )
        }

      case "update" =>
        currentTable match {
          case Some( table ) =>
            val hd :: rest :: Nil = input.tail.toSeq
            val uuid = Try { hd.toInt }.toOption
            val rebuiltTail = rest.mkString(" ")
            val record = rebuiltTail.split(";;").mkString("\t")
            val article = Article.fromDbRecord( record )
            ( uuid, article ) match {
              case ( Some(id), Some(art) ) =>
                val newUUID = table.update( id )( art )
                println(newUUID)
              case _ =>
                println( s"Update failed on command: $input")
            }
          case None =>
            println( "No table selected--please enter `usetable <tablename>` first" )
        }

      case "delete" =>
        currentTable match {
          case Some( table ) =>
            val rebuiltTail = input.tail.mkString(" ")
            val uuid = Try { rebuiltTail.toInt }.toOption
            uuid match {
              case Some(id) =>
                val r = table.delete( id )
                println( s"Delete successful? $r" )
              case None =>
                println( s"Delete failed. $rebuiltTail is not an integer" )
            }
          case None =>
            println( "No table selected--please enter `usetable <tablename>` first" )
        }

      case "exit" => exitFlag = true

      case other => println( s"Invalid input: $other" )
    }
  }
}
