package tutorials.basics
package step3

import tutorials.basics.step2.Database

object Demo extends App {
  Database(sys.env("HOME") + "/dbroot")("testdb").useTable("testtable")
}
