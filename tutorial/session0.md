# Do something interesting with Scala!

[under construction]

Background: I am a professional Scala developer, and have also worked in C, C++, and Python. I decided to work with Scala because of the types of problems it is being used to solve in industry, and because it has some language features that I like, such as a relatively robust type system and better support for functional programming (I'm not an FP fanatic, it's just a preference). When I started learning Scala, I found the written materials extremely focused on syntax rather than how to use Scala to solve a problem. 

In my experience, [syntax is something you can look up](http://docs.scala-lang.org/cheatsheets/), and learning to design the solution at a higher level is much more important. Many tutorials though, focus on getting practice with syntax. There is a reason for this, though I think we can improve it. In an interpreted language like Python, you can write a program with many kinds of bugs in it, and the program will execute right up until it tries to run the buggy line. In a compiled language like C or Scala, if there's something wrong with your syntax the program won't compile and won't even run the first statement, so getting the syntax wrong has a higher penalty up front. This is frustrating at first, but once you get a bit more familiar with it, it actually helps you figure out the correct solution to the problem sometimes! Having a primer on syntax is valuable, but too many tutorials are 90% syntax. If we don't do something more interesting than saving a string to a variable and finding the length of it, we get bored and stop learning.

The goal of this tutorial is to get the reader as quickly as possible to actually solve an interesting problem, and have a complete app that they can deploy. This tutorial will aim to use only the tools that are available in Scala itself, without external dependencies. Specifically, you will be building a simple relational database, along the lines of a minimum-viable-product for a database similar to [sqlite](https://www.sqlite.org/).

This tutorial assumes that you have at least basic experience programming in some language. Pretty much any one will do: Python, C, Java, R, Ruby, etc. It will touch on some advanced topics, including distributed systems, but does not require previous knowledge or experience with those topics. Working through the tutorial should take between \_?\_ and \_?\_ hours of work (estimate, based on the wonderful folks who have helped test and refine it, and learned Scala in the process). After the first few lines of starter code, instructions are provided on what to accomplish but little or no more code is given. Links will be provided to documentation, blog posts, etc where hints and solutions can be found, and if you get extremely stuck, this repo also contains [complete solutions to the tutorial](../src/main/scala/tutorials/basics) broken down into steps that roughly correspond to stages of the instructions, so you can look there if the links do not provide clear help. 

## Let's get started

### Intall Scala and sbt

If you have already installed the Java JDK (preferably version 8), Scala, and sbt, skip down to [Set up a new project](#set-up-a-new-project). If not, follow the instructions on [Scala's install page](https://www.scala-lang.org/documentation/getting-started-sbt-track/getting-started-with-scala-and-sbt-in-the-command-line.html).

sbt is currently the most common (though definitely not only) build tool used for Scala. The most common commands to know are:

- `sbt compile` - Compiles Scala code into Java bytecode that can be run. This will often involve downloading things from the internet, especially the first time you run it.
- `sbt test` - Runs any tests you have written (also compiles, if you have not done so).
- `sbt run` - Runs your application.

### Set up a new project

This repo provides the basic build structure already so that you don't have to manually set up the build tools used to compile Scala and run it. The important files and folders looks like this:

```
scala-boost/
 |- data/
 |   |- basics_data.tsv
 |
 |- project/
 |   |- build.properties
 |
 |- src/
 |   |- main/
 |       |- scala/
 |
 |- build.sbt
```

Source files go into "src/main/scala" (and its subdirectores). We'll refer to this as "the source directory" for the rest of the instructions. The "build.sbt" file and any files inside "project" are the build files, used to compile the source.

You can use this repository, and put your source into "src/main/scala", or follow the instructions from [this blog post](https://alvinalexander.com/scala/how-to-create-sbt-project-directory-structure-scala) to set up your own project from scratch. The only thing to change is the Scala version. Replace "2.10.0" with "2.11.11".

In the source directory, create a new file, named "Demo.scala". Open that file, and add this line to the top:

```scala
package demo
```

This creates a namespace. You can create multiple source files in the same project, and if they have the same package statement, they act as if they are all in the same file. We'll see more soon on how to use this for your advantage.

### Reading the data

In the root of this repository, there is a folder named "data" with a filed named "basics_data.tsv". This file contains news articles pulled from [NewsAPI](https://newsapi.org/) and written in tab-separated-value [TSV] format, which is like a spreadsheet where the tab character ("\\t") separates the columns. This is a common format for small sets of text data (comma-separated-value [CSV] is more common for numerical data, but commas occur in text and mess up comma-separated-value parsers, so TSV works well here).

The first thing to do is read the data from the file, and print it out, so we can get an idea of what's in there. The first thing to do is figure out where the file is located. Your user's home directory is saved in the `HOME` environment variable in your operating system. If you checked out this repository in your home directory, then the data file is located at `$HOME/scala-boost/data/basics_data.tsv`.

In Scala, all functionality has to be housed in an object. These can stand alone, or can "extend" other classes/traits to add functionality ("extends" is the Scala keyword for inheriting from another class, which will probably be familiar if you have worked in another object-oriented language). In our case, we want to create an object that extends `App`, which will turn whatever we put in that object into an application that can be run. Make an object in "Demo.scala", after the package line:

```scala
object Demo extends App {
  println("Hello, World!")
}
```

Now you can go to the command line in the directory of your project (the one that contains the `src` folder) and type `sbt run` to see it compile and then print "Hello, World!" Yay! Throughout the rest of the tutorial, run either `sbt compile` or `sbt run` to compile and/or run your code. The compiler will print error messages when there's something wrong--these are really confusing occasionally, but they'll provide direction, or at least an error message to search for help. Once you get familiar with them, they actually help you write your application.

Inside of the `Demo` object, you can use the `sys.env.get` function to read an environment variable. Use this to get the home directory, and then add the path to where the data file is checked out. To build the string mentioned previously, this would look like:

```scala
val home = sys.env.get("HOME")
val dataFilePath = s"$home/scala-boost/data/basics_data.tsv"
```

The `s` that appears before the string we assign to `dataFilePath` is known as a [string interpolator](https://docs.scala-lang.org/overviews/core/string-interpolation.html), which replaces variables in a string with their values. In this case, the interpolator expands `$home` into the value assigned to `home`.

For a quick reference on objects, functions, variables, etc, Twitter provides a great [primer on core structures and concepts](http://twitter.github.io/effectivescala/) as well, with a topical index so you can go straight to the topic that is throwing an error.

Now that we've created the path, we need to actually read the contents of the file. The `scala.io.Source` package contains functions for reading from files. Check out [the documentation for io.Source](http://www.scala-lang.org/api/2.11.11/index.html#scala.io.Source$) for a simple function that will open a file if you give it a file path (hint: you want the one that accepts a String as the argument). This function has a return type (which appears after the colon) that you can click to find the function that you can use to actually read (or _get_) the lines. Following this call, you should have an `Iterator[String]`. An `Iterator` is like a list, except you can only go through the data once. We may want to use it a few times, so go ahead and convert it from an `Iterator` to a `Seq`.

Once you have the the data in a sequence, this is a great time to print it and see what it looks like. The column headers are:

```
Title    Author    PublishedAt    Description    URL
```

They are not included in the data itself, but you can see that each line follows this pattern.

_If you have had difficulty with the previous step and wish to see/use an example implementation thus far, check out [the step0 directory](../src/main/scala/tutorials/basics/step0) in this repo._

## Formatting the data

TSV-formatted data is good for storing on disk, but this is not very easy to work with directly. "case classes" are data structures in Scala that are useful for structuring this kind of object. In a relational database (one with defined columns where each row has the same number of items in the same order, and each item has a set type), each row corresponds well to an instance of a class. Create a new file in the source directory named "Article.scala", and add the package statement and a case class named Article, with a field for each column in the table.

Now, create a function that takes a string as input. If the string fits the pattern of the columns in our data table, the function should return an instance of the class. If not, it should fail (take a look at [this blog](http://danielwestheide.com/blog/2012/12/19/the-neophytes-guide-to-scala-part-5-the-option-type.html) for an introduction to the `Option` type, which makes this success-or-failure checking work well). The function should have the following signature:

```scala
def deserialize(input: String): Option[Article]
```

As an additional note about design, there are several places where you could put this function. Because its purpose is to get an Article out of something else, one common place to put it is in what's called the "companion object" to the case class. This is an `object` with the exact same name as the `case class`. This is what a companion object with a deserialize function would look like on a simplified version of an Article:

```scala
case class Article(name: String)

object Article {
  def deserialize(input: String): Option[Article] = Some( Article( input ) )
}
```

You can choose your own name for the function and/or the input parameter, adjusting the signature accordingly. Once you've written the function, you can test it by adding this:

```scala
val testArticle = Article.deserialize( "Title\tAuthor\tPublishedAt\tDescription\tURL" ).get
assert( 
  testArticle.title == "Title" && 
  testArticle.author == "Author" && 
  testArticle.publishedAt == "PublishedAt" &&
  testArticle.description == "Description" &&
  testArticle.url == "URL"
)
println("Passed!")
```

If the funtion worked correctly, it will print "Passed!". If not, it will throw an exception. An exception on the first line (probably saying "None.get") indicates that the expected format did not match what was in the TSV file. An exception on the assert means that maybe the fields were out of order. Print the `testArticle` object to see what's out of place. Once we have a function like this, we can use it to change each line of data from a string to an `Article`. There are several functions that can do this. One very common example is `map` which takes a function as its argument and applies that function to each member of a sequence. The function has to have the same input type as the objects in the sequence, but the output type could be anything. Try using both of them to see how they behave (more background info [on this syntax tutorial from Twitter](https://twitter.github.io/scala_school/collections.html) under the "Functional Combinators" header if you get stuck or just want a more complete explanation of their behavior and other similar options).

Before we move on, if we have a `deserialize` function, it makes sense to have a `serialize` function that does the opposite. This can just use string interpolation to unpack the `Article` into a string containing all of the info (in the format specified by the test for `deserialize`). Like `deserialize`, this function could go in several places. One good place to put it is onto the case class itself. Using a simplified version of `Article`, with only a name, we would add a `serialize` function like this:

```scala
case class Article(name: String) {
  def serialize: String = s"Article:$name"
}

object Article {
  def deserialize(input: String): Option[Article] = Some( Article( input.split(":").last ) )
}
```

If you have written these two functions correctly, you should be able to do the following:

```scala
val inputArticle = Article("Title", "Author", "Date", "Description", "URL")
val articleRecord = inputArticle.serialize
val outputArticle = Article.deserialize( articleRecord )
assert( outputArticle == inputArticle )
```

## Using the formatted data

_If you have had difficulty with the previous step and wish to see/use an example implementation thus far, check out [the step1 directory](../src/main/scala/tutorials/basics/step1) in this repo._

Use the functions from the last section to get a `Seq[Article]` with the data from the TSV file. Now, we can do other types of analysis and filtering, also using `map` and/or `flatMap`, and a similar function named `filter`. If you have a list of strings, and you want a list of integers, you could use either `map` or `flatMap`. If you definitely want to keep every value in the list, use `map`, and write a function to use with it that takes a String as its input type, and has Int as its output type, which we sometimes write as `String => Int`. If the funciton might fail sometimes (let's say you're converting a string to an integer, but the string is "a"), you can write a function that takes a `String` and returns an `Option[Int]` (also written `String => Option[Int]`), and use `flatMap` instead of `map`. This will go over the list, apply the function to each item, and create a list of results that only includes the successful items.

First, take a look at the articles, and choose a word that appears in some but not all of the records. Think about what fields it does and does not occur in, and which fields you care about for matching records.

`map` and `flatMap` are good for taking each record, and picking out which sub-set of the data you care about. Maybe you only want to keep the title--this is pretty stright-forward. Write a function that takes an Article as the input, and returns the title from that Article. Then, you can call `map` on the input list of Articles, and get a list of Strings that only contains the titles. If you're writing a function that could conceivably fail, you might want to make the output an Option, and use `flatMap`. This will simply discard any failed cases, giving you only a list of the successes. Or you can use the function that returns the output inside of an Option with `map` instead, and be able to see which cases were successful and which were not.

`filter` is used with a function that takes an item of the input type (in this case probably either Article or String), and returns either true or false. This function is applied to each member of the list, and members that resulted in "true" are kept, while those that resulted in "false" are discarded.

You can filter and extract the data in many ways. One thing you might try is to take a list of Articles, and use `map`, `flatMap`, and/or `filter` to return a list of HTML hyperlinks in the format `<a href=[the url of the article]>[the title of the article]</a>`.

_If you have had difficulty with the previous step and wish to see/use an example implementation thus far, check out [the step2 directory](../src/main/scala/tutorials/basics/step2) in this repo._

## Saving and sharing data

Applications do a lot of processing in memory, but most eventually need to save, shared, and read data from some kind of storage. In modern applications, this is generally done with a database. There are many different kinds of database, each of which provides different pros and cons for saving and searching different kinds of data. The most common traditional kind is called a relational database (or RDBMS), and under the hood it works very similarly to the TSV file on disk that we read from earlier.

We have already created a bit of the functionality needed to build a database to save `Article`s. Now, let's add a few more functions to implement a very basic relational database from scratch. Copy this skeleton into a new file named "Database.scala" in the source directory:

```scala
package demo

import java.nio.file._

case class Database(rootDir: String)(name: String) {
  // If a database by this name exists, open it, else create an empty db with this name
  val databaseDir: Path = ???

  def useTable(tableName: String): Table = ???
}

case class Table(db: Database)(name: String) {
  // If a table by this name exists, open it, else create an empty table with this name
  private val tableDir: Path = ???

  private var uuids: Set[Int] = ???
  private var maxUUID = ???

  // not required, but recommended to separate this out into its own function for use by multiple methods later
  private def saveToFile(path: Path)(contents: String): Boolean = ???

  // Add an article, assigning a new UUID
  def insert(article: Article): Int = ???

  // If an article exists with the specified UUID, get it
  def get(uuid: Int): Option[Article] = ???
}

```

Everywhere that there are `???`, some kind of implementation is needed to make the database work correctly (you're welcome to add any other variables and functions that are helpful for solving the problem). [Hint: use the "serialize" and "deserialize" functions written earlier for writing and reading respectively]

Some of these functions will need to interact with the file system. The function used earlier to read a file (from `scala.io.Source`) will work for actually reading the contents of a file, but for creating directories, listing folder contents, and checking if things already exists, you'll want to look at the functions in "NIO", especially `java.nio.file.Files` and `java.nio.file.Path`. `Files` provides functions such as `createDirectories` (most functions in `Files` return a `Path`). A `Path` can be converted to a `java.nio.file.File` using the `toFile` function, and the resulting `File` provides `listFiles` for the contents of a directory, `getName` for the filename, etc. Check out the [this tutorial on Path](http://tutorials.jenkov.com/java-nio/path.html) and [this one on Files](http://tutorials.jenkov.com/java-nio/files.html) for info and examples. There's also good info in [the NIO javadocs](https://docs.oracle.com/javase/7/docs/api/java/nio/file/Files.html).

Start with filling in Database. The directory where the data should be stored is passed as `rootDir`, and the db also has a `name`. The most straight-forward way to store this info in the filesystem is to create a directory named by the `name` variable inside of the directory specified by `rootDir`. Check out the documentation for `java.nio.files.Files` for helpful methods.

Wait on `useTable`, and start filling in Table. Table has a variable that acts just like `databaseDir`, so you can do the same thing here that you did in Database.

Next are `uuids` and `maxUUID`. `uuids` should keep track of all of the IDs that have been used for records already, and maxUUID should point to the highest number UUID at the time that the class is loaded.

`saveFile` should take a Path and contents to write, and write the contents to the specified file.

`insert` should take an Article, assign it an unused UUID, and then write it to a file with the UUID as the filename (feel free to include other info in the filename, such as a file extension, as long as it doesn't impede making `get` work correctly).

`get` should open the file corresponding to a specified UUID, and return the contents, or None if there is no record for that UUID.

Now, go back and finish `useTable` in Database. This should create a new instance of Table with the Database instance (a.k.a.: `this`) and the specified table name.

_If you have had difficulty with the previous step and wish to see/use an example implementation thus far, check out [the step3 directory](../src/main/scala/tutorials/basics/step3) in this repo._

### Use the database

Now it's time to make a User Interface. Create a file named "Repl.scala", and paste this in:

```scala
object Repl extends App {
  var exitFlag = false
  val dbRootDir = ???

  val databases: Map[String, Database] = Map.empty
  var currentDb: Option[Database] = None
  var currentTable: Option[Table] = None

  println("Welcome to NewbieDB! Type 'help' for a list of commands, or 'exit' to quit (commands are not case-sensitive).")
  while( !exitFlag ) {
    print("> ")
    val input: Seq[String] = ???

    input.head match {
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

      case "usedb" => ???

      case "usetable" => ???

      case "insert" => ???

      case "get" => ???

      case "exit" => exitFlag = true

      case other => println( s"Invalid input: $other" )
    }
  }
}
```

I've included a couple of guiding details that are a lot of work and not much learning, so that you can get right to the more interesting stuff. Do take a look though, because the text and whatnot may provide hints about the commands you need to implement. Note that there's plenty here you can modify, such as the character(s) used to separate fields in a record input. For the example, I picked ";;", because it won't occur accidentally in an article, whereas splitting on spaces, commas, etc could easily result in the string getting split up the wrong way and failing to write to the database.

The first thing this app needs is to know the path in the filesystem where the database will be stored. You're welcome to make whatever design decision makes sense to you--you can add this as an additional parameter to the Database class, hard-code it to the current directory, or specify some set path based on an environment variable that is set on all operating systems, such as `HOME` (in the example implementation, I made this a parameter, and passed in `sys.env("HOME") + "/.newbiedb"` at the callsite).

Now, let's build the REPL (read-evaluate-print-loop). As the name suggests, the first thing a REPL does is read input from stdin (usually with a prompt). This is another task where `scala.io` comes in handy. Check out [the scaladocs](http://www.scala-lang.org/api/2.11.11/#scala.io.package). Once you get the input, you may have to clean it (remove end-lines, etc), and should figure out a way to split the command portion (the first word from the input) from the rest. No matter how you do it, you should end up with a Seq[String], where the first element (a.k.a. the `head`) is the command. You could choose to make your commands case-sensitive (including changing the case statements below accordingly if you want to force all-caps, for instance), or you could include a command to turn the command lowercase.

The next step is "evaluate". In our case, this is just identifying which command has been entered, and selecting what to do based on this. The case statement already does the choosing if the input is formatted correctly, but you're welcome to modify it if you want it to behave differently. Now you just need to replace the `???` for each command with an implementation.

`usedb` should "open" a database. The Database class already handles the details, so we just need to pass along the name that the user specifies, and keep track of which database has been "opened" most recently. You can get as simple or fancy as you like. The second item in the input array will be the word after "usedb". You can do more fancy stuff, like making sure that they only enter one word, check for invalid characters (ones that would break the file system, like "/"), etc. For a first version this isn't strictly necessary though.

`usetable` should "open" a table inside the current database. This should require that a database already be "open" and fail otherwise (hopefully with a useful error message...). When this command is successful, the app should keep track of the most recently opened table.

`insert` should turn the remainder of the input and attempt to turn it into an instance of Article. If this is unsuccessful, print an error message, otherwise use the insert method of the current table.

`get` should turn the remainder of the input into an integer. If unsuccessful, print an error message, otherwise use the get method of the current table.

_If you have had difficulty with the previous step and wish to see/use an example implementation thus far, check out [the step4 directory](../src/main/scala/tutorials/basics/step4) in this repo._

### Database extra credit

The database we've created so far is an immutable database, with no built-in ability to change or delete records. This is actually all that is needed for some ways of designing applications, so it is a complete MVP product. Most databases also support commands to update existing records, delete records, delete tables or whole databases, and some kinds of search functionality. Each extra credit assignment is independent of the others, so you don't have to do all of them, and you can do them in any order.

Extra credit assignment #1: Add `update` and `delete` commands to Table.

```scala
def update(uuid: Int)(article: Article): Boolean = ???
def delete(uuid: Int): Boolean = ???
```

The implementation of the `update` case statement in the REPL is very similar to that of `insert`. The main difference is extracting the UUID from the input separately from the rest of the tail. Convert that piece to an integer and pass it to the update command--the rest should be the same. The implementaton of `update` in Table should overwrite an existing record if it exists.

The implementation of the `delete` case statement in the REPL is basically identical to that of `get`. Just make it call the `delete` method of Table instead of `get`. The implementation of Table should check that the target UUID does actually correspond to a record in the table, and delete it ([probably using `java.nio.file.Files`](https://docs.oracle.com/javase/tutorial/essential/io/delete.html)).

Extra credit assignment #2: Add `dropTable` and `dropDb` commands to Database.

Unlike all prior functions, `droptable` and `dropdb` can be implemented completely in the REPL app, as this is the only place where table or database state are kept. Delete the corresponding directory from the file system, set the relevant state variable to None, and remove from a record-keeping Map if one is used. (Note that if you drop the current database, you'll want to set both the current table and current database variables to None--not just the database).

Extra credit assignment #3: Add one or more `find` commands to Table (your choice, exact match or "contains").

[Add FIND _ = _ to Table and REPL]

_If you have had difficulty with the extra credit and wish to see/use example implementations, check out [the step5 directory](../src/main/scala/tutorials/basics/step5) in this repo._

Extra extra extra credit: Modify the Database class so that it works with any data type (maybe some restrictions), rather than just Article. [no example provided yet--under construction]

## Notes

If you're curious, I used this snippet to create data that was used for this tutorial, using articles from [NewsAPI](https://newsapi.org/), which is similar to an implementation of `serialze` used in `demo.Database.Table.insert`:

```scala
  var nextUUID = 0
  
  val articleToTSV: (Article => String) = a => {
    val result = s"${a.title}\t${a.author}\t${a.publishedAt}\t${a.description}\t${a.url}\n"
    nextUUID = nextUUID + 1
    result
  }
```

Further Reading:

- For an extremely detailed discussion of database implementations and challenges, check out ["Designing Data Intensive Applications](http://shop.oreilly.com/product/0636920032175.do), especially Chapter 3
