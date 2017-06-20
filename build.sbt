name := "domain-security-scanner"
 
version := "1.0"
 
scalaVersion := "2.12.2"

resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/")

mainClass in assembly := Some("ar.com.falberca.security.agent.MainApp")

libraryDependencies ++= {
  val AkkaVersion = "2.5.1"
  val ScalaTestVersion = "3.0.1"
  val AkkaHttpVersion = "10.0.4"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
    "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % AkkaVersion,
    "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion,
    "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
    "ch.qos.logback" % "logback-classic" % "1.1.7",
    "org.scalatest"     %% "scalatest" % ScalaTestVersion % "test")
}