package tutorials.basics
package step3

object Demo extends App {
  Database(sys.env("HOME") + "/dbroot")("testdb").createTable("testtable")
}
