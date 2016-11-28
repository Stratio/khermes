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

import com.typesafe.config.Config
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.LoggerFactory


object KafkaProducer {

  val log = LoggerFactory.getLogger(getClass)

  def getInstance(config: Config): KafkaProducer[AnyRef, AnyRef] = {
    val props: Properties = new Properties()
    props.put("metadata.broker.list", config.getString("brokerList"))
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("bootstrap.servers", config.getString("servers"))
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("request.requieres.acks", "1")
    new KafkaProducer(props)
  }

  def send(producer: KafkaProducer[AnyRef, AnyRef], topic: String, message: String): Unit = {
    try {
      log.info("Sending message : [" + message + "] to the topic: " + topic)
      producer.send(new ProducerRecord[AnyRef, AnyRef](topic, message))

    } catch {
      case e: Exception =>
        log.error("Error sending message: [" + message + "]")
        log.error("Exception: " + e.getMessage)
    }
  }

  def close(producer: KafkaProducer[AnyRef, AnyRef]): Unit = {
    log.info("Producer closed.")
    producer.close()
  }
}