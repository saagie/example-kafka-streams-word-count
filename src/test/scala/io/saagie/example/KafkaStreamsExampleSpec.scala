package io.saagie.example

import java.lang.Long

import com.madewithtea.mockedstreams.MockedStreams
import com.madewithtea.mockedstreams.MockedStreams.Builder
import org.apache.kafka.common.serialization.{Serde, Serdes}
import org.scalatest._

/**
  * Test cases for the Kafka Streams sample transformation.
  */
class KafkaStreamsExampleSpec extends FlatSpec with Matchers {
  val stringSerde: Serde[String] = Serdes.String
  val longSerde: Serde[Long] = Serdes.Long
  val inputTopic: String = "input"
  val outputTopic: String = "output"
  val storeName: String = "store"

  val input: Seq[(String, String)] = Seq(
    "atest", "btest", "ctest", "atest", "dtest", "dtest", "etest", "btest", "atest"
  ).map(word => (word, ""))

  val mockedStreams: Builder = MockedStreams()
    .topology(builder => KafkaStreamsExample.streamingJob(builder, inputTopic, outputTopic, storeName))
    .input(inputTopic, stringSerde, stringSerde, input)

  info("Starting...")

  "Kafka Streams Example" should "send to the output topic the number of occurrences of each words" in {
    val expected = Seq(
      ("atest", 1L),
      ("btest", 1L),
      ("ctest", 1L),
      ("atest", 2L),
      ("dtest", 1L),
      ("dtest", 2L),
      ("etest", 1L),
      ("btest", 2L),
      ("atest", 3L)
    )

    mockedStreams.output(outputTopic, stringSerde, longSerde, expected.size) shouldEqual expected
  }

  it should "store into the state store the number of occurrences of each words" in {
    val expected = Map(
      "atest" -> 3L,
      "btest" -> 2L,
      "ctest" -> 1L,
      "dtest" -> 2L,
      "etest" -> 1L
    )

    mockedStreams.stateTable(storeName) shouldEqual expected
  }
}
