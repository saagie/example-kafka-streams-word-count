package io.saagie.example

import java.util.Properties

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream.KStreamBuilder
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import org.slf4j.LoggerFactory
import scopt.OptionParser

/**
  * Kafka Streams sample transformation. Does a word count from an input topic, logs the result,
  * and send them into an output topic.
  */
object KafkaStreamsExample extends App {

  /**
    * Parameters for this app.
    */
  case class Params(kafkaBootstrapServer: String = "", kafkaInputTopic: String = "", kafkaOutputTopic: String = "")

  lazy val logger = LoggerFactory.getLogger(getClass)

  /**
    * Parses command-line arguments with scopt.
    */
  val parser = new OptionParser[Params]("") {
    head("kafka streams example", "1.0")

    help("help") text "print this usage text"

    opt[String]("kafka-bootstrap-server") required() action { (data, conf) =>
      conf.copy(kafkaBootstrapServer = data)
    } text "URI of the kafka server. Example: http://IP:9091"

    opt[String]("kafka-input-topic") valueName "topic" required() action { (data, conf) =>
      conf.copy(kafkaInputTopic = data)
    } text "Input topic where words are sent."

    opt[String]("kafka-output-topic") valueName "topic" required() action { (data, conf) =>
      conf.copy(kafkaOutputTopic = data)
    } text "Output topic."
  }

  /**
    * Parse and start the streaming job.
    */
  parser.parse(args, Params()) match {
    case Some(params) =>
      logger.info(s"Params: $params")
      startStreaming(params)
    case None =>
      logger.error("Error while parsing params.")
  }

  /**
    * Configure and start a streaming job counting words from an input topic.
    *
    * @param params containing input /output topics and kafka's bootstrap server URI.
    */
  def startStreaming(params: Params): Unit = {
    // Local state store name, used for aggregations.
    val storeName = "word-count-state-store-name"

    // Kafka Streams configuration.
    val streamingConfig = new Properties
    streamingConfig.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-streams-example")
    streamingConfig.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, params.kafkaBootstrapServer)

    val builder = new KStreamBuilder
    streamingJob(builder, params.kafkaInputTopic, params.kafkaOutputTopic, storeName)

    // Start streaming.
    val kafkaStreams = new KafkaStreams(builder, streamingConfig)
    sys.addShutdownHook(kafkaStreams.close())
    kafkaStreams.start()
  }

  /**
    * Takes a KStreamBuilder to creates streaming transformations needed.
    *
    * @param builder     input KStreamBuilder.
    * @param inputTopic  input topic containing words.
    * @param outputTopic where to output the word count.
    * @param storeName   name of the state store used for aggregations.
    */
  def streamingJob(builder: KStreamBuilder, inputTopic: String, outputTopic: String, storeName: String): Unit = {
    val stringSerde = Serdes.String
    val longSerde = Serdes.Long
    val stream = builder.stream[String, String](stringSerde, stringSerde, inputTopic)

    val wordCount = stream
      .groupByKey(stringSerde, stringSerde)
      .count(storeName)

    // Log the result.
    wordCount
      .toStream()
      .foreach((word, count) => logger.info(s"word: $word - count: $count"))

    wordCount.to(stringSerde, longSerde, outputTopic)
  }
}
