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

import java.util.Properties
import java.util.concurrent.Future

import com.stratio.hermes.utils.HermesLogging
import com.typesafe.config.Config
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.{ProducerRecord, RecordMetadata, KafkaProducer}

/**
 * Simple client used to send messages to a Kafka broker.
 * @param config with all Kafka configuration.
 */
class KafkaClient()(implicit config: Config) extends HermesLogging {

  lazy val properties: Properties = {
    assert(config.hasPath("kafka"), "There is not a kafka configuration in your application.conf")
    import scala.collection.JavaConversions._
    val props = new Properties()
    val map: Map[String, Object] = config.getConfig("kafka").entrySet().map({ entry =>
      entry.getKey -> entry.getValue.unwrapped()
    })(collection.breakOut)
    props.putAll(map)
    props
  }

  lazy val producer: KafkaProducer[AnyRef, AnyRef] = new KafkaProducer(properties)
  lazy val consumer: KafkaConsumer[AnyRef, AnyRef] = new KafkaConsumer(properties)

  /**
   * Sends a message to a topic.
   * @param topic with the Kafka's topic.
   * @param message with the message to send.
   * @return a future with the result of the operation.
   */
  def send(topic: String, message: String): Future[RecordMetadata] = {
    producer.send(new ProducerRecord[AnyRef, AnyRef](topic, message))
  }
}
