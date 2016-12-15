/*
 * Copyright (C) 2016 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.hermes.kafka

import java.util

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class KafkaClientIT extends FlatSpec with Matchers {

  val Message = "testMessage"
  val Topic = "testTopic"
  val PollTime = 100

  implicit val config = com.stratio.hermes.implicits.HermesImplicits.config
  val kafkaClient = new KafkaClient

  "A KafkaProducer" should "parse the configuration correctly" in {
    val expectedResult = Map(
      "metadata.broker.list" -> "localhost:9092",
      "bootstrap.servers" -> "localhost:9092",
      "key.serializer" -> "org.apache.kafka.common.serialization.StringSerializer",
      "value.serializer" -> "org.apache.kafka.common.serialization.StringSerializer",
      "key.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer",
      "value.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer",
      "group.id" -> "0"
    )
    import collection.JavaConversions._
    mapAsScalaMap(kafkaClient.properties) should be (expectedResult)
  }

  it should "produce and consume messages" in {
    val kafkaClient = new KafkaClient

    kafkaClient.consumer.subscribe(util.Arrays.asList(Topic))
    kafkaClient.consumer.poll(PollTime).count should be (0)

    kafkaClient.send(Topic, Message)
    kafkaClient.producer.flush()
    kafkaClient.consumer.poll(PollTime).iterator().next().value().toString should be (Message)

    kafkaClient.producer.close
    kafkaClient.consumer.close
  }
}
