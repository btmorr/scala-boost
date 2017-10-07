# Do something interesting with Scala!

Background: I am a professional Scala developer, and have also worked in C, C++, and Python. I decided to work with Scala because of the types of problems it is being used to solve in industry, and because it has some language features that I like, such as a relatively robust type system and better support for functional programming (I'm not an FP fanatic, it's just a preference). When I started learning Scala, I found the written materials extremely focused on syntax rather than how to use Scala to solve a problem. 

### Motivation

In my experience, [syntax is something you can look up](http://docs.scala-lang.org/cheatsheets/), and learning to design the solution at a higher level is much more important. Many tutorials though, focus on getting practice with syntax. There is a reason for this, though I think we can improve it. In an interpreted language like Python, you can write a program with many kinds of bugs in it, and the program will execute right up until it tries to run the buggy line. In a compiled language like C or Scala, if there's something wrong with your syntax the program won't compile and won't even run the first statement, so getting the syntax wrong has a higher penalty up front. This is frustrating at first, but once you get a bit more familiar with it, it actually helps you figure out the correct solution to the problem sometimes! Having a primer on syntax is valuable, but too many tutorials are 90% syntax. If we don't do something more interesting than saving a string to a variable and finding the length of it, we get bored and stop learning.

### About this tutorial

The goal of this tutorial is to get the reader as quickly as possible to actually solve an interesting problem, and have a complete app that they can deploy. We'll build a very basic relational database, along the lines of a minimum-viable-product for something similar to [sqlite](https://www.sqlite.org/). This kind of database stores records where every record has the same set of named fields of data. The pattern that each record has to follow is called the schema. We will build the software that stores the records, and also a REPL for using the database. The biggest difference between this and a real database is that we'll keep the data in memory only, rather than writing it to disk. Reading and writing from disk are extremely unusual tasks for Scala applications and that is not the real point of this exercise, so we won't bother with it.

We will accomplish this using only the tools that are available in Scala itself, without external dependencies. This tutorial assumes that you have at least basic experience programming in some language. Pretty much any one will do: Python, C, Java, R, Ruby, etc. It will touch on some advanced topics, including distributed systems, but does not require previous knowledge or experience with those topics. Working through the tutorial should take between 4 and 6 hours of work (estimate, based on the wonderful folks who have helped test and refine it, and learned Scala in the process--if it takes you much longer than this, please shoot me a message and tell me about what was really tough to work through so I can improve it). After the first few lines of starter code, instructions are provided on what to accomplish but little or no more code is given. Links will be provided to documentation, blog posts, etc where hints and solutions can be found, and if you get extremely stuck, this repo also contains [complete solutions to the tutorial](../src/main/scala/tutorials/basics) broken down into steps that roughly correspond to stages of the instructions, so you can look there if the links do not provide clear help. 

### Why bother?

Why build a simple database? You never build your own database when building another app--you use something like Mongo or MySQL, so why practice something you will probably not actually do?

Two reasons: First, you will be a lot more adept at using databases correctly if you understand the basics of how they work under the hood. Second, it's something that you can add to your portfolio of example work, that is related to the field of work that commonly uses Scala--data engineering. This is a solid resume-builder, just like building example web-sites for a web-designer.

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

You can use this repository, and put your source into "src/main/scala", or follow the instructions from [this blog post](https://alvinalexander.com/scala/how-to-create-sbt-project-directory-structure-scala) to set up your own project from scratch. If you follow the blog post, the only thing to change is the Scala version. Replace "2.10.0" with "2.11.11".

In the source directory, create a new file, named "Repl.scala". Open that file, and add this line to the top:

```scala
package database
```

This creates a namespace. You can create multiple source files in the same project, and if they have the same package statement, they act as if they are all in the same file. We'll see more soon on how to use this for your advantage.

### The User Interface

_In this section, we'll build a very basic app that reads from stdin (the user input prompt), changes the input to lower-case, and prints the result. Then, it should check whether the input was "exit", and if not then do it all again._

The first piece of the database to build is the user interface. This doesn't have to be the first component, but by building this first it will be possible to use it and see what works and what doesn't, right from the start. The standard UI for a database is a read-evauluate-print-loop, a.k.a. the REPL. 

### A quick Hello, as a starter

In Scala, all functionality has to be housed in an object. These can stand alone, or can "extend" other classes/traits to add functionality ("extends" is the Scala keyword for inheriting from another class, which will probably be familiar if you have worked in another object-oriented language). In our case, we want to create an object that extends `App`, which will turn whatever we put in that object into an application that can be run. Make an object in "Repl.scala", after the package line:

```scala
object Repl extends App {
  println("Hello, World!")
}
```

Now you can go to the command line in the directory of your project (the one that contains the `src` folder) and type `sbt run` to see it compile and then print "Hello, World!" Yay! Throughout the rest of the tutorial, run either `sbt compile` or `sbt run` to compile and/or run your code. The compile will print error messages when there's something wrong--these are really confusing occasionally, but they'll provide direction, or at least an error message to search for help. Once you get familiar with them, they actually help you write your application.

As we build the app, we'll separate the components out into objects and functions to group and/or separate them by what part of the work they perform. You're probably already familiar with variables, objects, and functions, but if not or if you're unfamiliar with how these are represented in Java-like languages, Twitter provides a great [primer on core structures and concepts](http://twitter.github.io/effectivescala/), with a topical index.

### The REPL

As the name suggests, the first thing a REPL does is read input from stdin (usually with a prompt). A package built into the language, `scala.io`, comes in handy. Check out [the scaladocs](http://www.scala-lang.org/api/2.11.11/#scala.io.package). Once you get the input, you may have to clean it (remove end-lines, etc). We'll worry about how the commands are formatted later, but for now just write a function that takes the whole input as a single string, and outputs a Seq[String], with no new-line characters. Now, replace the "Hello, World" line with a mutable variable named `command` (you can initialize it with an empty string, e.g.: `var command = ""`), and then a while loop that checks if the variable is equal to "exit". Inside the loop, make an immutable variable (a `val`) and assign it the return value of the function from `scala.io` that you use to get a line of input from the user. (From this point on unless specifically directed otherwise, all the code we add will be inside the while-loop, and all variables should be `val`s).

The next step is "evaluate". In our case, this will be identifying which command has been entered, and selecting what to do based on this. First, write a function that takes a string, makes it lower-case, and splits it into two pieces: the first word before a space, and the rest. Return the resulting list. Use this function on the input from the user, and store the result in another varable (pro-tip: you could return a "tuple" instead of a list, and then use multiple assignment to name variables for the first word and the rest--without using a function, this would look like this: `val (head, tail) = ("insert", "the_rest_of_the_input")`).

There is a basic set of commands that a database management system ("DBMS") should support. These vary slightly from system to system, but we'll start with this set, roughly correlating to the minimum set for an immutable database with multiple databases and multiple tables in each database:

- `usedb` should "open" a database. The Database class already handles the details, so we just need to pass along the name that the user specifies, and keep track of which database has been "opened" most recently. You can get as simple or fancy as you like. The second item in the input array will be the word after "usedb". You can do more fancy stuff, like making sure that they only enter one word, check for invalid characters (ones that would break the file system, like "/"), etc. For a first version this isn't strictly necessary though.
- `usetable` should "open" a table inside the current database. This should require that a database already be "open" and fail otherwise (hopefully with a useful error message...). When this command is successful, the app should keep track of the most recently opened table.
- `insert` should turn the remainder of the input and attempt to turn it into an instance of Article. If this is unsuccessful, print an error message, otherwise use the insert method of the current table.
- `get` should turn the remainder of the input into an integer. If unsuccessful, print an error message, otherwise use the get method of the current table.

Don't worry about the implementation yet. Use a "match" clause to check the command, with a "case" for each command listed above, and for now just print a message so you can verify that it selected the case you intended.

_If you have had difficulty with the previous step and wish to see/use an example implementation thus far, check out [the step0 directory](../src/main/scala/tutorials/basics/step0) in this repo._

## Formatting the data

_One very common task in data processing and saving things in a database is how to establish a consistent format, so that the data still makes sense when you try to read what you wrote earlier. This section will cover data serialization and deserialization--technical terms for writing and reading application data._

Data comes in from standard input as a string, but this is not very easy to work with directly. "case classes" are data structures in Scala that are useful for structuring this kind of object. In a relational database (one with defined columns where each row has the same number of items in the same order, and each item has a set type), each row corresponds well to an instance of a class. Create a new file in the source directory named "Article.scala", and add the package statement and a case class named Article, with a field for each column in the table.

Now, create a function that takes a string as input. If the string fits the pattern of the columns in our data table, the function should return an instance of the class. If not, it should fail (take a look at [this blog](http://danielwestheide.com/blog/2012/12/19/the-neophytes-guide-to-scala-part-5-the-option-type.html) for an introduction to the Option type, which makes this success-or-failure checking work well). The function should have the following signature:

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

The most difficult portion of this is deciding exactly how a plain string gets broken apart so you can turn it into an instance of the Article case class. There isn't always a completely simple solution. One common pattern is called "comma-separated value" format, a.k.a. CSV. In a CSV record, there are commas in between each field, and you just split the string at the commas. This works very well for numerical data, but doesn't always work well for text, where the content of a field might include a comma as well and get split incorrectly. On the upside, when designing this app you get to choose what the input format looks like, so you can pick a separator that works better for text. You can choose whatever you like, but for the tutorial I'll use a semicolon. Write a deserialize function that splits the string and then tries to turn the result into an Article. If it is successful, return the resulting Article inside of a `Some`. If it fails, return a `None`. Once you've written the body of the function, you can test it by adding this:

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

If the function worked correctly, it will print "Passed!". If not, it will throw an exception. An exception on the first line (probably saying "None.get") indicates that the expected format did not match the input. An exception on the assert means that maybe the fields were out of order. Print the `testArticle` object to see what's out of place. Once we have a function like this, we can use it to change each line of data from a string to an Article. There are several functions that can do this. One very common example is `map` which takes a function as its argument and applies that function to each member of a sequence. The function has to have the same input type as the objects in the sequence, but the output type could be anything. Try using both of them to see how they behave (more background info [on this syntax tutorial from Twitter](https://twitter.github.io/scala_school/collections.html) under the "Functional Combinators" header if you get stuck or just want a more complete explanation of their behavior and other similar options). 

Before we move on, if we have a `deserialize` function, it makes sense to have a `serialize` function that does the opposite. This can just use string interpolation to unpack the Article into a string containing all of the info (in the format specified by the test for `deserialize`). Like `deserialize`, this function could go in several places. One good place to put it is onto the case class itself. Using a simplified version of Article, with only a name, we would add a `serialize` function like this:

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

_If you have had difficulty with the previous step and wish to see/use an example implementation thus far, check out [the step1 directory](../src/main/scala/tutorials/basics/step1) in this repo._

## Saving and sharing data

_In a real database, saved data would be stored on the hard drive, in some kind of files so that when the application is restarted the data will not be lost. For our example, we will not actually write to disk, but we will still "save" the data to an in-memory dictionary that will serve as a standing for file I/O operations._

Applications do a lot of processing in memory, but most eventually need to save, shared, and read data from some kind of storage. In modern applications, this is generally done with a database. There are many different kinds of database, each of which provides different pros and cons for saving and searching different kinds of data. The most common traditional kind is called a relational database (or RDBMS), and under the hood it saves data to normal files very similar to CSV files.

We have already created a bit of the functionality needed to build a database to save `Article`s. Now, let's add a few more functions to implement a very basic relational database from scratch. Copy this skeleton into a new file named "Database.scala" in the source directory:

```scala
package database

import scala.util.Try

case class Database(name: String) {
  var tables: Set[Table] = Set.empty
  var currentTable: Option[Table] = None
  def useTable(tableName: String): Table = {
    ???
  }
  
  // functions to make it possible for the app using the Database to call methods from the Table, even though 
  // it is a private case class
  def insert(article: Article): Option[Int] = currentTable.get.insert( article )
  def get(id: Int): Option[Article] = currentTable.flatMap( _.get( id ) )
}

private case class Table(name: String) {
  // Notice that this is a map of Int -> String. While a purely in-memory database _could_ contain actual Article
  // objects, we are replicating the behavior of a database that would store objects in files, so it has to save
  // them as strings and then turn them from strings back into objects
  private var records: Map[Int, String] = Map.empty

  def insert(article: Article): Option[Int] = {
    ???
  }

  def get(id: Int): Option[Article] =
    ???

}

```

Everywhere that there are `???`, some kind of implementation is needed to make the database work correctly (you're welcome to add any other variables and functions that are helpful for solving the problem). [Hint: use the "serialize" and "deserialize" functions written earlier for writing and reading respectively]

Start with filling in Database. The database needs to keep track of what Tables have been created and which one is currently active. You can fill out the `useTable` function without having to know exactly how Table will work yet. The key thing to remember in implementing this function is that if a table with the specified name has already been opened earlier, it should find that Table rather than creating a new one.

Inside Table, `insert` should take an Article, assign it an unused ID integer, and then save it to a Map[Int, String], where the key is the ID number, and the value is the serialized version of the Article. Even though it can't actually fail in this version of the database, return the result inside of a Some, since in a real database there are several ways that an insert could fail.

`get` should retrieve the record corresponding to the specified ID, convert it back into an Article and return it, or return None if there is no record for that ID.

_If you have had difficulty with the previous step and wish to see/use an example implementation thus far, check out [the step3 directory](../src/main/scala/tutorials/basics/step2) in this repo._

## Using the formatted data

[=----------------=]

Use the functions from the last section to get a `Seq[Article]` with the data from the TSV file. Now, we can do other types of analysis and filtering, also using `map` and/or `flatMap`, and a similar function named `filter`. If you have a list of strings, and you want a list of integers, you could use either `map` or `flatMap`. If you definitely want to keep every value in the list, use `map`, and write a function to use with it that takes a String as its input type, and has Int as its output type, which we sometimes write as `String => Int`. If the funciton might fail sometimes (let's say you're converting a string to an integer, but the string is "a"), you can write a function that takes a `String` and returns an `Option[Int]` (also written `String => Option[Int]`), and use `flatMap` instead of `map`. This will go over the list, apply the function to each item, and create a list of results that only includes the successful items.

First, take a look at the articles, and choose a word that appears in some but not all of the records. Think about what fields it does and does not occur in, and which fields you care about for matching records.

`map` and `flatMap` are good for taking each record, and picking out which sub-set of the data you care about. Maybe you only want to keep the title--this is pretty stright-forward. Write a function that takes an Article as the input, and returns the title from that Article. Then, you can call `map` on the input list of Articles, and get a list of Strings that only contains the titles. If you're writing a function that could conceivably fail, you might want to make the output an Option, and use `flatMap`. This will simply discard any failed cases, giving you only a list of the successes. Or you can use the function that returns the output inside of an Option with `map` instead, and be able to see which cases were successful and which were not.

`filter` is used with a function that takes an item of the input type (in this case probably either Article or String), and returns either true or false. This function is applied to each member of the list, and members that resulted in "true" are kept, while those that resulted in "false" are discarded.

You can filter and extract the data in many ways. One thing you might try is to take a list of Articles, and use `map`, `flatMap`, and/or `filter` to return a list of HTML hyperlinks in the format `<a href=[the url of the article]>[the title of the article]</a>`.

_If you have had difficulty with the previous step and wish to see/use an example implementation thus far, check out [the step2 directory](../src/main/scala/tutorials/basics/step2) in this repo._

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

If you're curious, I used this snippet to create data that was used for this tutorial, using articles from [NewsAPI](https://newsapi.org/), which is similar to an implementation of `serialze` used in `database.Database.Table.insert`:

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

# Thanks!

Thank you so much to all of the people who have reviewed this and provided feedback to make it better. Notably:

- [Bonnie Eisenman](http://blog.bonnieeisenman.com)
- [Miguel Iglesias](https://github.com/caente)
- [Ratan Sebastian](https://github.com/rjsvaljean)
