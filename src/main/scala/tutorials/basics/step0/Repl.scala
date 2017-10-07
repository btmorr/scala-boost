package tutorials.basics
package step0

object Repl extends App {
	var done = false
	// read
	while(!done) {
		val splitInput = scala.io.StdIn.readLine("> ").stripLineEnd.split(" ", 2)
		val command = splitInput.head
		val tail = splitInput.tail.headOption.getOrElse("")
		// evaluate
		val result = command match {
			case "usedb" => s"USEDB $tail"
			case "usetable" => s"USETABLE $tail"
			case "insert" => s"INSERT $tail"
			case "get" => s"GET $tail"
			case "exit" =>
        done = true
				s"EXIT"
			case _ => s"Invalid command: $command"
		}
		// print
		println( result )
	} // loop
}
