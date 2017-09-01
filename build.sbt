name := "example-kafka-streams-word-count"

version := "1.0"

scalaVersion := "2.12.3"

libraryDependencies ++= {
  val kafkaVersion = "0.11.0.0"
  Seq(
    "org.apache.kafka" % "kafka-streams" % kafkaVersion,
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "org.codehaus.groovy" % "groovy" % "2.4.12",
    "com.github.scopt" %% "scopt" % "3.6.0",
    "org.scalatest" %% "scalatest" % "3.0.4" % "test",
    "com.madewithtea" %% "mockedstreams" % "1.3.0" % "test" exclude("org.slf4j", "slf4j-log4j12")
  )
}

assemblyMergeStrategy in assembly := {
  x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

assemblyJarName := "example-kafka-streams-word-count.jar"

mainClass in assembly := Some("io.saagie.example.KafkaStreamsExample")