name := "google-news"

version := "0.1"

scalaVersion := "2.13.3"

val akkaVersion = "2.6.8"
val akkaHttpVersion = "10.2.0"

scalacOptions ++= Seq(
  "-deprecation",
  "-language:higherKinds"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.1.0" % Test,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "de.heikoseeberger" %% "akka-http-circe" % "1.34.0",
  "io.circe" %% "circe-parser" % "0.13.0",
  "io.circe" %% "circe-core" % "0.13.0",
  "com.typesafe.akka" %% "akka-stream" % "2.6.8",
  "io.circe" %% "circe-generic" % "0.13.0",
  "com.typesafe.akka" %% "akka-actor-typed" % "2.6.8",
)
