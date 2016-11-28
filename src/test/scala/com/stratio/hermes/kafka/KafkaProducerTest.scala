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
import java.util.Properties

import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source
import scala.util.Try


@RunWith(classOf[JUnitRunner])
class KafkaProducerTest extends FlatSpec with Matchers {

  val poll_time = 1000
  val kafka_host = System.getProperty("KAFKA_HOST", "localhost")
  val kafka_port = System.getProperty("KAFKA_PORT", "9092")
  val topicName = "test"

  val props = new Properties
  props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  props.put("bootstrap.servers", kafka_host + ":" + kafka_port)
  props.put("enable.auto.commit", "true")
  props.put("group.id", "0")
  props.put("auto.commit.interval.ms", "1000")
  props.put("session.timeout.ms", "30000")

  val producer_test = Try(getClass.getResourceAsStream(s"/producer_test.json"))
    .getOrElse(throw new IllegalStateException(s"Error loading locale: /producer_test.json"))


  "A KafkaProducer" should "Produce a message and be consumed" in {

    val kafkaProducer = KafkaProducer.getInstance(ConfigFactory.parseResources("kafka.conf"))

    val consumer = new KafkaConsumer(props)
    consumer.subscribe(util.Arrays.asList(topicName))

    val records1 = consumer.poll(poll_time)
    records1.count() shouldEqual 0

    KafkaProducer.send(kafkaProducer, topicName, Source.fromInputStream(producer_test).mkString)

    val records2 = consumer.poll(poll_time)
    records2.count() shouldEqual 1

    KafkaProducer.close(kafkaProducer)
    consumer.close()
  }
}