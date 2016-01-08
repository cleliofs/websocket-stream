name := "websocket-stream"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= {
  val akkaVersion = "2.3.12"
//  val streamsVersion = "1.0"
  val streamsVersion = "2.0.1"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" % "akka-http-core-experimental_2.11" % streamsVersion,
    "com.typesafe.akka" % "akka-http-experimental_2.11" % streamsVersion,
    "com.typesafe.akka" % "akka-stream-experimental_2.11" % streamsVersion,

    // websocket client
    "javax.websocket" % "javax.websocket-api" % "1.1",
    "org.glassfish.tyrus" % "tyrus-client" % "1.11",
    "org.glassfish.tyrus" % "tyrus-server" % "1.11"
  )
}