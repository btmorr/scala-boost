# Do something interesting with Scala!

[under construction]

Background: I am a professional Scala developer, and have also worked in C, C++, and Python. I decided to work with Scala because of the types of problems it is being used to solve in industry, and because it has some language features that I like, such as a relatively robust type system and better support for functional programming (I'm not an FP fanatic, it's just a preference). When I started learning Scala, I found the written materials extremely focused on syntax rather than how to use Scala to solve a problem. 

In my experience, [syntax is something you can look up](http://docs.scala-lang.org/cheatsheets/), and learning to design the solution at a higher level is much more important. Many tutorials though, focus on getting practice with syntax. There is a reason for this, though I think we can improve it. In an interpreted language like Python, you can write a program with many kinds of bugs in it, and the program will execute right up until it tries to run the buggy line. In a compiled language like C or Scala, if there's something wrong with your syntax the program won't compile and won't even run the first statement, so getting the syntax wrong has a higher penalty up front. This is frustrating at first, but once you get a bit more familiar with it, it actually helps you figure out the correct solution to the problem sometimes! Having a primer on syntax is valuable, but too many tutorials are 90% syntax. If we don't do something more interesting than saving a string to a variable and finding the length of it, we get bored and stop learning.

The goal of this tutorial is to get the reader as quickly as possible to actually solve an interesting problem, and have a complete app that they can deploy. This tutorial will aim to use only the tools that are available in Scala itself, without external dependencies.

This tutorial assumes that you have at least basic experience programming in some language. Pretty much any one will do: Python, C, Java, R, Ruby, etc. It will touch on some advanced topics, including distributed systems, but does not require previous knowledge or experience with those topics. Working through the tutorial should take between _?_ and _?_ hours of work (estimate, based on the wonderful folks who have helped test and refine it, and learned Scala in the process). After the first few lines of starter code, instructions are provided on what to accomplish but little or no more code is given. Links will be provided to documentation, blog posts, etc where hints and solutions can be found, and if you get extremely stuck, this repo also contains [complete solutions to the tutorial](../src/main/scala/tutorials/basics) broken down into steps that roughly correspond to stages of the instructions, so you can look there if the links do not provide clear help.

## Let's get started

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

In Scala, all functionality has to be housed in an object. These can stand alone, or can "extend" other objects to add functionality ("extends" is the Scala keyword for inheriting from another class, which will probably be familiar if you have worked in another object-oriented language). In our case, we want to create an object that extends `App`, which will turn whatever we put in that object into an application that can be run. Make an object in "Demo.scala", after the package line:

```scala
object Demo extends App {

}
```

Inside of that object, you can use the `sys.env.get` function to read an environment variable. Use this to get the home directory, and then add the path to where the data file is checked out. To build the string mentioned previously, this would look like:

```scala
val home = sys.env.get("HOME")
val dataFilePath = s"$home/scala-boost/data/basics_data.tsv"
```

This has the path, now we need to actually read the contents of the file. The `scala.io.Source` package contains functions for reading from files. Check out [the documentation for io.Source](http://www.scala-lang.org/api/2.11.11/index.html#scala.io.Source$) for a function that will open a file (hint: you want the one that accepts a String as the argument). This funtion has a return type that you can click to find the function that you can use to actually read the lines. Following this call, you should have an `Iterator[String]`. An Iterator is like a list, except you can only go through the data once. We may want to use it a few times, so go ahead and convert it from an Iterator to a Seq.

Once you have the the data in a sequence, this is a great time to print it and see what it looks like. The column headers are:

```
UniqueID    Title    Author    PublishedAt    Description    URL
```

They are not included in the data itself, but you can see that each line follows this pattern.


## Formatting the data

TSV-formatted data is good for storing on disk, but this is not very easy to work with directly. "case classes" are data structures in Scala that are useful for structuring this kind of object. In a relational database (one with defined columns where each row has the same number of items in the same order, and each item has a set type), each row corresponds well to an instance of a class. Create a new file in the source directory named "Article.scala", and add the package statement and a case class named Article, with a field for each column in the table.

Now, create a function that takes a string as input. If the string fits the pattern of the columns in our data table, the function should return an instance of the class. If not, it should fail (take a look at [this blog](http://danielwestheide.com/blog/2012/12/19/the-neophytes-guide-to-scala-part-5-the-option-type.html) for an introduction to the Option type, which makes this success-or-failure checking work well). The function should have the following signature:

```scala
def deserialize(input: String): Option[Article]
```

You can choose your own name for the function and/or the input parameter, adjusting the signature accordingly. Once you've written the function, you can test it by adding this line:

```scala
println( deserialize( "0\tTitle\tAuthor\tPublishedAt\tDescription\tURL" ).get )
```

If the funtion worked correclty, it will print `0    Title    Author    PublishedAt    Description    URL`. If not, it will throw an exception. Once we have a function like this, we can use it to change each line of data from a string to an Article. There are several functions that can do this. The most common ones are `map` and `flatMap`, each of which takes a function as its argument. The function has to have the same input type as the objects in the sequence, but the output type could be anything. Try using both of them to see how they behave (more background info [on this syntax tutorial from Twitter](https://twitter.github.io/scala_school/collections.html) under the "Functional Combinators" header if you get stuck or just want a more complete explanation of their behavior and other similar options). 

## Using the formatted data

Use the functions from the last section to get a `Seq[Article]` with the data from the TSV file. Now, we can do other types of analysis and filtering, also using `map` and/or `flatMap`, and a similar function named `filter`.

[examples of use of map, flatMap, and filter to do basic analysis]

## Saving and sharing data

Applications do a lot of processing in memory, but most eventually need to save, shared, and read data from some kind of storage. In modern applications, this is generally done with a database. There are many different kinds of database, each of which provides different pros and cons for saving and searching different kinds of data. The most common traditional kind is called a relational database (or RDBMS), and under the hood it works very similarly to the TSV file on disk that we read from earlier.

We have already created a bit of the functionality needed to build a database to save `Article`s. Now, let's add a few more functions to implement a very basic relational database from scratch. Copy this skeleton into a new file named "Database.scala" in the source directory:

```scala
package demo

case class Database(name: String) {
  // If a database by this name exists, open it, else create an empty db with this name
  ???
  
  case class Table(name: String) {
    // If a table by this name exists, open it, else create an empty table with this name
    ???
    
    def insert(article: Article): Boolean = ???
    def update(article: Article): Boolean = ???
    def delete(article: Article): Boolean = ???
    def get(uuid: Int): Option[Article] = ???
  }
}
```

Everywhere that there are `???`, some kind of implementation is needed to make the database work correctly.

[Explain each function, and walk through implementing and testing them]

## Notes

If you're curious, I used this snippet to create data that was used for this tutorial, using articles from [NewsAPI](https://newsapi.org/), which is similar to an implementation of `demo.Database.Table.insert`:

```scala
  var nextUUID = 0
  
  val articleToTSV: (Article => String) = a => {
    val result = s"$nextUUID\t${a.title}\t${a.author}\t${a.publishedAt}\t${a.description}\t${a.url}\n"
    nextUUID = nextUUID + 1
    result
  }
```

Further Reading:

- For an extremely detailed discussion of database implementations and challenges, check out ["Designing Data Intensive Applications](http://shop.oreilly.com/product/0636920032175.do), especially Chapter 3

