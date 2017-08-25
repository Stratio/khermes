/**
 * © 2017 Stratio Big Data Inc., Sucursal en España.
 *
 * This software is licensed under the Apache 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the terms of the License for more details.
 *
 * SPDX-License-Identifier:  Apache-2.0.
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
