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
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}

object KafkaProducer extends HermesLogging {

  def getProperties(config: Config): Properties = {
    val props: Properties = new Properties()
    props.put("metadata.broker.list", config.getString("metadata.broker.list"))
    props.put("key.serializer", config.getString("key.serializer"))
    props.put("bootstrap.servers", config.getString("bootstrap.servers"))
    props.put("value.serializer", config.getString("value.serializer"))
    props.put("request.requieres.acks", config.getString("request.requieres.acks"))
    props
  }

  def getInstance(config: Config): KafkaProducer[AnyRef, AnyRef] = getInstance(getProperties(config))

  def getInstance(props: Properties): KafkaProducer[AnyRef, AnyRef] = new KafkaProducer(props)

  def send(producer: KafkaProducer[AnyRef, AnyRef], topic: String, message: String): Future[RecordMetadata] = {
      log.info(s"Sending message: [$message] to the topic: $topic")
      producer.send(new ProducerRecord[AnyRef, AnyRef](topic, message))
    }

  def close(producer: KafkaProducer[AnyRef, AnyRef]): Unit = {
      log.info("Producer closed.")
      producer.close()
  }
}
