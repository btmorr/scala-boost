lazy val sparkVersion = "2.1.1"

lazy val boost = (project in file("."))
  .settings(
    name := "scala-boost",
    scalaVersion := "2.11.11",
    resolvers += Resolver.bintrayRepo("commercetools", "maven"),
    libraryDependencies ++= Seq(
    	"io.sphere" %% "sphere-json" % "0.8.2",
      "org.apache.httpcomponents" % "httpclient" % "4.5.3",
    	"org.apache.spark" %% "spark-core" % sparkVersion /* % "provided" */,
      "org.apache.spark" %% "spark-streaming" % sparkVersion /* % "provided" */,
      "org.apache.spark" %% "spark-sql" % sparkVersion /* % "provided" */
    ),
    /* Spark 2.x requires an exact version of Jackson for json handling
     * For more info, see:
     *   - https://stackoverflow.com/questions/41464444/play-and-spark-incompatible-jackson-versions
     *   - https://mvnrepository.com/artifact/org.apache.spark/spark-core_2.11/2.2.0
     */
    dependencyOverrides ++= Set(
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.5",
      "org.json4s" %% "json4s-jackson" % "3.2.11"
    )
  )
