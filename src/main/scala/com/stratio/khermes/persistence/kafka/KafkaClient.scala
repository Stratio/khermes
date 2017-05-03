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

package com.stratio.khermes.persistence.kafka

import java.util.Properties
import java.util.concurrent.Future

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}

/**
 * Simple client used to send messages to a Kafka broker.
 * @param config with all Kafka configuration.
 */
class KafkaClient[K](config: Config) extends LazyLogging {

  lazy val producer: KafkaProducer[String, K] = new KafkaProducer(parseProperties())

  /**
   * Parses Kafka's configuration to a properties object.
   * @param path that could be the configuration of a kafkaProducer or a kafkaConsumer. (kafkaProducer by default).
   * @return a parsed properties object.
   */
  def parseProperties(path: String = "kafka"): Properties = {
    assert(config.hasPath(path), s"Not existing $path path in application.conf")
    import scala.collection.JavaConversions._
    val props = new Properties()
    val map: Map[String, Object] = config.getConfig(path).entrySet().map({ entry =>
      entry.getKey -> entry.getValue.unwrapped()
    })(collection.breakOut)
    props.putAll(map)
    props
  }

  /**
   * Sends a message to a topic.
   * @param topic with the Kafka's topic.
   * @param message with the message to send.
   * @return a future with the result of the operation.
   */
  def send(topic: String, message: K): Future[RecordMetadata] = {
    val a = new ProducerRecord[String, K](topic, message)
    producer.send(a)
  }
}
