package io.saagie.example

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import scopt.OptionParser

import scala.util.Random

/**
  * Basic random word generator. Produces [String, String] records with words as key into a Kafka topic.
  */
object KafkaWordProducer extends App {
  /**
    * Parameters for this app.
    */
  case class Params(kafkaBootstrapServer: String = "", kafkaOutputTopic: String = "")

  lazy val logger = LoggerFactory.getLogger(getClass)

  /**
    * Parses command-line arguments with scopt.
    */
  val parser = new OptionParser[Params]("") {
    head("kafka word producer", "1.0")

    help("help") text "print this usage text"

    opt[String]("kafka-bootstrap-server") required() action { (data, conf) =>
      conf.copy(kafkaBootstrapServer = data)
    } text "URI of the kafka server. Example: http://IP:9091"

    opt[String]("kafka-output-topic") valueName "topic" required() action { (data, conf) =>
      conf.copy(kafkaOutputTopic = data)
    } text "Topic where the words are sent."
  }

  /**
    * Parse and start producing records.
    */
  parser.parse(args, Params()) match {
    case Some(params) =>
      logger.info(s"Params: $params")
      startProducer(params)
    case None =>
      logger.error("Error while parsing params.")
  }

  /**
    * Configure and start producing words into provided Kafka topic.
    *
    * @param params containing the output topic and kafka's bootstrap server URI.
    */
  def startProducer(params: Params): Unit = {
    val stringSerializer = new StringSerializer
    val rnd = new Random
    val wordLength = 5

    // Producer configuration.
    val producerConfig = new Properties()
    producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, params.kafkaBootstrapServer)
    producerConfig.put(ProducerConfig.CLIENT_ID_CONFIG, "kafka-streams-example-producer")

    val producer = new KafkaProducer[String, String](producerConfig, stringSerializer, stringSerializer)

    // Send random words to a kafka topic.
    val sender = new Thread(() => {
      while (!Thread.currentThread.isInterrupted) {
        val data = new ProducerRecord[String, String](
          params.kafkaOutputTopic,
          rnd.nextString(wordLength).map(a => (a % 26 + 'a').toChar),
          "value"
        )
        producer.send(data)
        logger.debug("word sent: " + data.key)
      }
    })

    // Shut down the job gracefully when we stop it.
    sys.addShutdownHook({
      logger.info("Stopping producer...")
      sender.interrupt()
      sender.join()
      producer.close()
    })

    logger.info("Sending words into " + params.kafkaOutputTopic + "...")
    sender.start()
  }
}