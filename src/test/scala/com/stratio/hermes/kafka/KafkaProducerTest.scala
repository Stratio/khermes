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

import com.typesafe.config.{ConfigException, ConfigFactory}
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConverters._
import scala.io.Source
import scala.util.Try


@RunWith(classOf[JUnitRunner])
class KafkaProducerTest extends FlatSpec with Matchers {

  val PollTime = Integer.parseInt(System.getProperty("POLL_TIME", "100"))
  val KafkaHost = System.getProperty("KAFKA_HOST", "localhost")
  val KafkaPort = System.getProperty("KAFKA_PORT", "9092")
  val TopicName = System.getProperty("TOPIC_NAME", "test")
  val props = new Properties
  props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  props.put("bootstrap.servers", KafkaHost + ":" + KafkaPort)
  props.put("enable.auto.commit", "true")
  props.put("group.id", "0")
  props.put("auto.commit.interval.ms", "1000")
  props.put("session.timeout.ms", "30000")
  val ProducerTest = Try(getClass.getResourceAsStream("/fixtures/kafka/producer-fixture.json"))
    .getOrElse(throw new IllegalStateException("Error loading locale: /fixtures/kafka/producer-fixture.json"))

  "A KafkaProducer" should "Produce a message and be consumed" in {

    val kafkaProducer = KafkaProducer.getInstance(ConfigFactory.parseResources("kafka.conf"))
    val consumer = new KafkaConsumer(props)
    consumer.subscribe(util.Arrays.asList(TopicName))
    val records1 = consumer.poll(PollTime)
    records1.count() shouldEqual 0
    KafkaProducer.send(kafkaProducer, TopicName, Source.fromInputStream(ProducerTest).mkString)
    kafkaProducer.flush()
    val records2 = consumer.poll(PollTime)
    for (r <- records2.iterator().asScala) {
      r.value.toString.shouldEqual("{\"name\":\"amparo\"}")
    }
    records2.count() shouldEqual 1
    KafkaProducer.close(kafkaProducer)
    consumer.close()
  }

  "A KafkaProducer" should "fail when do not read correctly the configuration" in {
    a[ConfigException] should be thrownBy KafkaProducer.getInstance(ConfigFactory.parseResources(""))
  }

}
