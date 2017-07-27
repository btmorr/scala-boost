# Scala syntax cheatsheet

In an interpreted language like Python, if you have a syntax bug in your software (such as trying to inject a String-typed variable into a string using `%f`), the program will run until it hits the bug and then crash, allowing the developer to partially run even badly broken software.

In compiled languages like Scala, syntax errors have to be fixed before the program can finish compiling, which is mandatory before any part of the program can run. This is initially very annoying if you've worked with an interpreted language before, but actually makes many things easier once you're familiar with compiler errors.

This is why most Scala tutorials focus so much on syntax--if you make a syntax error, you can't run your app at all. For this tutorial, we're going to try to focus on app design and data processing, and provide enough snippets of code that the student can create a working application without being a syntax expert. We also don't want it to be a copy-paste exercise, though, so the tutorial will provide code, but expects the user to modify this heavily and play around with it. This cheatsheet provides notes on various elements of Scala syntax to help as you modify and debug your application.

## Working with Strings

Creating a basic string (note that the `: String` after the variable name is optional, but included for completeness):

```scala
val userAddress: String = "1234 Example Way, Bastion, WA 54321"
```

---

There are two common ways to remove things from strings. If you're comfortable with regular expressions, you can use `replaceAll`, otherwise you can use `filter`. Either way, `trim` removes leading and trailing whitespace and newlines. 

```scala
val input = " messy_string-\n"

val output1 = input.replaceAll("[_-]", " ")
  .trim
  .replaceFirst("messy", "clean")  
// output1 = "clean string"

val output2 = input.filterNot( "_-".contains(_) )
  .trim
  .replaceFirst("messy", "clean")
// output2 = "cleanstring"
```

Things to notice: 
1. a statement may span multiple lines if one line ends with a `.` or the next line begins with one
1. `filter` removes the characters, while `replaceAll` can put something back in the place of removed elements.

---

Injecting values from other variables into a string (a.k.a. "string interpolation") is done by putting an `s` before the string and then using the `$` with variable names in the string. Also, you can use `${<code>}` to add any amount of processing. If you do, the code is run and the result is injected:

```scala
val simpleNumber: Int = 123
val complexObject: Option[String] = Some("Example")

val result = s"For and object named ${complexObject.getOrElse("")}, we're going to assign the number $simpleNumber"
```

## What is an Option?

In Scala, we avoid assigning `null` to a variable. Instead, if it is possible that a variable is unassigned, you can use an `Option`. This is a data type that can either contain something, or contain nothing. If it contains nothing, it will have the value `None`, and if it contains something, that thing will be inside of a `Some`. You can get the value out of an Option by calling the `get` method, but if it turns out to be `None` it will throw a `NullPointerException`, exactly as if you had used `null`, so we haven't gained anything. Instead, we almost always use a different function, `getOrElse`. The difference is that you have to provide a default value that will be used if the variable turns out to be `None`. You can also call `map` on an Option and if the Option is a Some, you will get back a Some with the result, and if it's a None you get a None. Some examples:

```scala
val username: String = "Testy"
val emailAddr: Option[String] = Some("testy@example.com")
val phoneNum: Option[String] = Some(" 123.456.7890\n")

/* if emailAddr is a Some, this wraps the string in <>
 * if it's a None, it is replaced with an empty string
 */
val formattedEmail = emailAddr
  .map{ email => s"<$email>" }
  .getOrElse("")  
  
/* First, this strips out any special characters. Then, it 
 * splits the phone number into three parts, and interpolates 
 * it into a formatter.
 */
val cleanedPhone = phoneNum
  .map{ _.replaceAll("[()-_.]", "").trim }

val formattedPhone = for {
  rawnum <- phoneNum
  num = rawnum.replaceAll("[.()_-]", "").trim
  if num.length == 10
  first = num.take(3)
  second = num.drop(3).take(3)
  last = num.takeRight(4)
} yield s"#: ($first) $second-$last"

val result = s"User: $username${formattedEmail} ${formattedPhone}"
// result = "User: Testy<testy@example.com> #: (123) 456-7890"
```

## Making a type

For some tasks, the data types that are already part of Scala (such as `String`, `Option`, `List`, `Int`, etc) will be plenty. Some of them I use in every application I write (especially `Seq`, which is similar to `List`), but often you will want to create a type for your data. This is generally done by creating case classes and case objects, and using traits. 

Traits are abstract classes. If you define a trait named `Sphere`, you cannot create something that is just a `Sphere`. It only exists in its subclasses, and is used to create categories of things, and to require everything in the category to have certain things in common, such as they all have to define a val named `radius` of type `Double`, perhaps. Traits can also extend other traits (e.g.: there can be a larger category containing several traits). This would look like:

```scala
trait Shape

trait Sphere {
  val radius: Double
} extends Shape
```

Case classes can stand alone, or they can extend traits or other classes:

```scala
case class BeachBall(radius: Double) extends Sphere
case class Globe(radius: Double) extends Sphere
```

This is useful where we want to write some functions that can take any kind of Sphere and perhaps calculate something from their radius, and it doesn't matter what exactly the Sphere is. In other places, we are also able to use the type system to tell whether a thing is a BeachBall or a Globe, even though they are both just objects that have a radius.

```scala
import math.Pi

def area(s: Sphere): Double = {
  s.radius * s.radius * Pi
}
```

Case objects represent single objects (called "singletons"). These are often used as part of a limited set of values, similar to an enumeration, but uses the type system to determine which one is being handled rather than checking a value.

```scala
trait Browser

case object Opera extends Browser
case object Chrome extends Browser
case object Safari extends Browser
case object Firefox extends Browser
case class OtherBrowser(name: String) extends Browser
case object InvalidBrowser extends Browser
```

The trait can be used for both case objects and case classes, and case classes can define members that are not required by the trait.

Once we have defined some case objects and case classes, we can create instances of them.

```scala
val validBrowsers: Seq[Browser] = Seq( 
  Chrome, 
  Safari, 
  Firefox, 
  OtherBrowser("Vivaldi")
)

val thisBrowser: Option[Browser] = getUserInput() // not defined, assume it is a function that takes Unit and returns an Option[Browser]

println(s"Is the browser supported? ${validBrowsers.contains(thisBrowser.getOrElse(InvalidBrowser))}")
```
