# Do something interesting with Scala!

[under construction]

Background: I am a professional Scala developer, and have also worked in C, C++, and Python. I decided to work with Scala because of the types of problems it is being used to solve in industry, and because it has some language features that I like, such as a relatively robust type system and better support for functional programming (I'm not an FP fanatic, it's just a preference). When I started learning Scala, I found the written materials extremely focused on syntax rather than how to use Scala to solve a problem. 

In my experience, syntax is something you can look up, and learning to design the solution at a higher level is much more important. Many tutorials though, focus on getting practice with syntax. There is a reason for this, though I think we can improve it. In an interpreted language like Python, you can write a program with many kinds of bugs in it, and the program will execute right up until it tries to run the buggy line. In a compiled language like C or Scala, if there's something wrong with your syntax the program won't compile and won't even run the first statement, so getting the syntax wrong has a higher penalty up front. This is frustrating at first, but once you get a bit more familiar with it, it actually helps you figure out the correct solution to the problem sometimes! Having a primer on syntax is valuable, but too many tutorials are 90% syntax. If we don't do something more interesting than saving a string to a variable and finding the length of it, we get bored and stop learning.

The goal of this tutorial is to get the reader as quickly as possible to actually solve an interesting problem, and have a complete app that they can deploy and show off that they have actually used some of the tools that are gaining a lot of attention in industry right now.

This tutorial assumes that you have at least basic experience programming in some language. Pretty much any one will do: Python, C, Java, R, Ruby, etc. It will touch on some advanced topics, including distributed systems, but does not require previous knowledge or experience with those topics. Working through the tutorial should take between _ and _ hours of work (estimate, based on the wonderful folks who have helped test and refine it, and learned Scala in the process).

## Data Source

First, we want to know where we can get useful info, and how to read/use it. For any web service that allows you to consume or produce data, you'll generally need an API key--this is like your login code. Depending on which source you wish to use for you app, how you get the key will vary.

- [Change.org](https://www.change.org/developers/api-key) is a site for managing petitions
- [NewsAPI](https://newsapi.org/) makes news stream metadata available from many sources, including NYT, Al Jazeera, BBC, the Economist, TechCrunch, etc.
- Twitter provides a [REST API](https://dev.twitter.com/rest/public) (single search at a time) and a [streaming API](https://dev.twitter.com/streaming/overview)(continuous feed)

## Processing Framework

A lot of systems will get a snapshot of the incoming data, and then do something with all of it together. More recently, streaming data processing systems--ones where you get a continuous incoming flow of data--are proving to be very powerful, so we'll build an app on this design. The basics of getting a stream and keeping it open can be extremely tricky, so several technologies have been built for handling streams of data, including [Spark Streaming](https://spark.apache.org/docs/latest/streaming-programming-guide.html#overview), [Flink](https://flink.apache.org/), [Kafka Streaming](https://kafka.apache.org/documentation/streams/), [Samza](http://samza.apache.org/), [Storm](http://storm.apache.org/) (all projects are Apache Foundation projects). Spark is one of the more developed of these solutions and is currently a popular choice in industry (e.g.: something that's really good for a resume), so we'll use Spark Streaming for building this application.

## Database

Most applications need to save some kind of data--at least records of what they have done. These records could be stored in many places, including to local disk or stdout stream. Disk and stdout work well for applications that run on a single computer, but most applications now need to run in parallel on different servers, so in addition to providing basic benefits like easy data sort/query/etc, databases also allow any number of instances of an application to easily collect their records into a single place. 

Most databases broadly fall into two categories: relational and non-relational. Relational databases are stores where every record fits a pre-defined schema--each record has a fixed number of pieces in a definite order, and each element has a predefined type such as integer or string. Non-relational databases store records that may or may not have requirements on the structure of each record--each one can be different from others and the application has to figure out how to structure the information.

Common examples of relational databases are MySQL, Postgres, and Hive. Common examples of non-relational databases are MongoDB and Cassandra.

The other common criteria to take into account when choosing a database is whether or not it needs to support distributed mode of operation (a database running on multiple servers, but looking like a single copy to users/apps). MongoDB, Cassandra, Hive, and Postgres all provide built-in support for distributed deployment.

## Choices for this tutorial

In future evolutions of this tutorial, we'll try to show how to use almost any combination of the above options. The first version of will use:

- Data Source: *NewsAPI* - NewsAPI provides free API access 
- Processing Framework: *Spark* - Spark is one of the more developed of these solutions and is currently a popular choice in industry (e.g.: something that's really good for a resume). Specifically this tutorial will use a Databricks account to get free access to an existing Spark cluster to avoid having to deal with setup of computing hardware--there are great resources for learning how to do this, but it is outside of the scope of learning to do something interesting with Scala
- Database: *Hive* - Hive is supported out of the box by Databricks, so we'll use this for starters so we can get straight to working with data, rather than paying for compute resources and spending a lot of time and headache installing and running a database.

A future version of this will probably also include creating and using a dashboard to allow a user to change what kinds of filters are applied and visualize the result.

# Time to actually do something!

## Getting access

The first thing to set up is access accounts for [NewsAPI](https://newsapi.org/) and [Databricks Community Edition](https://databricks.com/try-databricks). Both accounts are free.

On Databricks, click the link that says "Start Today" under "Community Edition". This is the account tier that remains free (note: the data you load into their platform is not private--this is the gotcha for this particular service, but is not an issue for this tutorial since this app will be processing a stream of public data and won't include any private data from the developer or anyone else). After completing the registration form, Databricks will send you an email to confirm your email address. 

On NewsAPI, click the button that says "Get API Key" and fill out the registration. Once you finish registration, you will see a page with a field titled "API key". Save this value to use later (you can always log back in and get it). The terms for free use of NewsAPI is to add a line to the site you create saying "Powered by NewsAPI" with a link back to NewsAPI.org--pretty reasonable request.

## Setting up the app

A Spark cluster is a set of one or more computers running Spark and whatever else is needed for Spark to run (really interesting, but off-topic for this tutorial). If you’ve worked in another language, you’ve probably used the language runtime to execute your script (`python my_script.py`, `java -jar myApp.jar`, `npm start`, etc.), or executed compiled code directly (hi, C devs!). Instead of this, Spark acts like a pre-existing runtime and you submit your code to it as a task to execute. This is great for getting started, because you don’t have to worry about whether or not everything is installed correctly on your laptop, or whether your laptop and current network connection is fast enough.

Once you log into Databricks, the entry page has links to most of the things you’d want to do, including the documentation where you can explore the platform’s functionality beyond what we’ll cover here. Click "Cluster" under "New". On the next page, there will be a name field (you can name the cluster whatever you like--I named mine Hufflepuff). From the Databricks Runtime Edition pulldown, select "Spark 2.1 (auto-updating, Scala 2.11)". Then click "Create Cluster" at the top.

Now, we’ll create a notebook and attach it to the cluster (you could have multiple clusters, and decide which one will actually run this job). Click the icon on the top left that says "databricks" to go back to the entry page. Then click "Notebook" under "New". Give the notebook a name (I used "news-stream"), select Scala from the language pulldown, and your cluster will probably already be pre-filled in the cluster pulldown (if not, select it). Now you’ll be in an empty notebook. This is like a Scala command line terminal, except that you can go back and edit blocks of code and re-run them, and nothing runs when you hit Enter--it waits until you click "Run Cell" on the right-hand side of the cell, or "Run All" at the top of the notebook.

[do you have to do anything special to use streaming on databricks?]

## Connect the source

First, you’ll want to add your API key. Normally, you’d have some kind of sophisticated secret-management system, but we’ll put the API key into a variable. In the first cell, type:

```
val newsApiKey = "d3b5c<your-key-here>46f411f16e13"
```

We get the data from NewsAPI by making HTTP GET requests to an endpoint provided by their site. In the first cell, we’ll add:

```
val source = "bbc-news"
val requestString = s"https://newsapi.org/v1/articles?source=$source&sortBy=top&apiKey=$newsApiKey"
```

The `requestString` variable now contains the whole address we need to make the request. As a side-note, this uses string interpolation, which you might already be familiar with from another language. In Scala, if you put an `s` before a string, then wherever it finds a `$` in the string, it will insert the value of the named variable there, which is why `$source` gets replaced with `bbc-news` and `$newsApiKey` gets replaced with your key. Go ahead and run the cell to see the what’s stored in this variable.

After running that cell, hover your mouse at the bottom-center of the cell and click the + sign that pops up. This gives you a new cell to work with. Paste this into that cell and run it:

```
val rdd = sc.parallelize(requestString :: Nil)
val contents = rdd.map(url => {
  val client = new org.apache.http.impl.client.DefaultHttpClient()
  val request = new org.apache.http.client.methods.HttpGet(url)
  val response = client.execute(request)
  val handler = new org.apache.http.impl.client.BasicResponseHandler()
  handler.handleResponse(response).trim.toString
}).collect()
```

Ok, cool! It got back some data! But it’s completely unformatted and unreadable...  First, let's understand what all of that code does, and then we'll make the output something we can work with.

```
val rdd = sc.parallelize( Seq( requestString ) )
```

The basic data structure in Spark is an "RDD" or Resilient Distributed Document. Basically, the data is split out and pieces of it are copied to every server in the cluster. This makes it possible to work on a dataset that is multiple terabytes or petabytes, even though any one server only has a handful of gigabytes of memory. The `parallelize` function takes a sequence (a.k.a. list) of items of data, and turns it into an RDD. 

```
val contents = rdd.map(url => {
```

This means "for every element in rdd, call it `url` and then do whatever is in the following curly-braces to it--once you're done, put the resulting sequence into a variable named `contents`". It's slightly silly to do this with a list of only one item, but it becomes powerful quickly when we want to process a whole bunch of things.

```
val client = new org.apache.http.impl.client.DefaultHttpClient()
```

This creates an HTTP client, which is the thing that will actually communicate to the NewsAPI server. There are other libraries that provide HTTP clients, and each has its own quirks and benefits. For anything more than a simple request, I'd recommend looking into [http4s](https://github.com/http4s/http4s).

```
val request = new org.apache.http.client.methods.HttpGet(url)
val response = client.execute(request)
```

The Apache DefaultHttpClient uses a set of data structures to provide some additional information that it needs to know what exactly to send to the NewsAPI server. The first line tells it that the request a GET request (as opposed to [POST or the several other options](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods)). The second line is the one that actually sends the request to the server.

```
val handler = new org.apache.http.impl.client.BasicResponseHandler()
handler.handleResponse(response).trim.toString
```

These two lines set up a parser that is able to decode the HTTP response, and turns the result into a string.

```
}).collect()
```

So, this is where Spark makes things a little weird. Everything above where we've said "this line does X, and this line does Y" wasn't exactly true. Those statements are like writing a recipe. They tell Spark what to do whenever it actually does run. These instructions (which are called "Transformations", and include `map`, `flatMap`, `groupByKey`, and others) are actually computed when other kinds of instructions called "Actions" are used, including `collect`, `reduce`, `first`, and `count` amongst others.

Why does it work this way? When you're handling data sets that are broken apart and spread over several servers, you have to have a plan for what to do with each piece, to make sure the output is the same as it would be if the program were run on a single giant server. For our purposes, we'll be able to use `map` and `flatMap` the same way we do in plain Scala, and `collect` will act as the equivalent of `run`.

## Reformatting the output

Now that we've reviewed the code that gets the news articles, we can make the results easier to read, display, and use.

The response data from NewsAPI is in a format called JSON. If you've done web programming before, you've probably already dealt with this extensively. The strategies for reading and writing JSON are different, depending on what language you're working in. In JavaScript, it is very straightforward to read and write JSON (JavaScript Object Notation), because it was create to exactly represent a JavaScript Object.

Scala models objects differently, which means we have to do a small amount of processing to transform it into a more useable format. 

[make case classes for the data, and a function that takes a JSON packet and returns a typed instance]

## Filter incoming data

[process the rdd into a dataframe]
[add filter functions]
[add a way to configure which filters get applied using flags]

## Use a database to store state

The cluster created earlier will already have a Hive database available on it. You don't have to do anything special to connect to it. Just add the following command to your application:

```
dataFrame.write.saveAsTable("newsRecords")
```

There's a snag though: Hive is an immutable data store, which means that records cannot be re-written or updated. We can only write new records. This also makes sense for our application, because when we make a request to NewsAPI for more news, we will probably get copies of things we've already seen before. We don't need or want to process them again, so we'll have to do some checking early in the pipeline to discard records we've already processed (e.g.: ones that are already in Hive).

[check db for records and drop matches]

There's another thing that databases are really valuable for: storing settings that are used to determine the behavior of our app. Settings, such as the name of our data source, could be hard-coded, but this means that in order to check Ars Technica instead of the BBC, we have to make a change to code and re-deploy the app, which is not the preferred way to do it (some sites used to have problems with this, where if for instance a blogger wanted to post an article, they had to have a programmer update the code of the website--much better to just write the new option to the database and have the app respond).

[move the state of the flags into the db]

Now that the settings are stored in the database, they can be changed by updating the database records, but without a UI, this means you have to have access to Hive and know how to write a records with command-line tools. Still a pain! We want a simple way to update that setting, such as a pull-down menu in a UI.

[create a way to change the state of the flags while the application is running]

## How to display the output?

[probably start with live update field in the notebook]
[webapp? combined with the one that changes the db settings?]

# Notes

Some snippets of code borrowed from [the Databricks docs](https://docs.databricks.com/) including use of the Apache DefaultHttpClient and functions for writing to Hive.