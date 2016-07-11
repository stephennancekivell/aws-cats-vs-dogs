scalaVersion := "2.11.8"

name := "aws-cats-vs-dogs"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
	"com.typesafe.akka" %% "akka-actor" % "2.4.8",
	"com.typesafe.akka" %% "akka-http-core" % "2.4.8",
	"com.typesafe.akka" %% "akka-http-experimental" % "2.4.8",
	"com.typesafe.akka" %% "akka-http-jackson-experimental" % "2.4.8",
	"com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.8",
	"mysql" % "mysql-connector-java" % "6.0.3",
	"com.typesafe" % "config" % "1.3.0",
	"com.h2database" % "h2" % "1.4.192"
)

enablePlugins(JavaAppPackaging)
