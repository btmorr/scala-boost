lazy val boost = (project in file("."))
  .settings(
    name := "scala-boost",
    scalaVersion := "2.11.11",
    resolvers += Resolver.bintrayRepo("commercetools", "maven"),
    libraryDependencies ++= Set(
    	"io.sphere" %% "sphere-json" % "0.8.2",
    	"org.apache.spark" % "spark-core_2.11" % "2.1.1" // % "provided"
  )
