# Sample Kafka Streams streaming job (Word count)

Read records' keys as strings from an input Kafka topic, count them, log them and send the result into an output topic.

## Packaging

Execute ```sbt clean assembly``` at the project root directory to generate
`./target/scala-2.12/example-kafka-streams-word-count.jar`.

## Usage

To use this app, execute this command :
```java -jar {file} --kafka-bootstrap-server {kafkaServer} --kafka-input-topic {producerTopic} --kafka-output-topic {outputTopic}```,
with the correct values for `{file}`, `{kafkaServer}`, `{producerTopic}` and `{outputTopic}`.

There is an embedded word producer in this project. You can start it by typing :
```java -cp {file} io.saagie.example.KafkaWordProducer --kafka-bootstrap-server {kafkaServer} --kafka-output-topic {producerTopic}```.

*NB : if you use this sample project with __Saagie Data Fabric__, you can leave `{file}` as it is.*

## Scaling

To easily scale up or down your job, you can start and stop as many instances of the Streams App on any machine with an access to the same Kafka Server.

If you use this sample project with __Saagie Data Fabric__, you just have to click on *"Create from..."* on one the instances of your job, and start it.
