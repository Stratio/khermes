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
import java.util.{Properties, UUID}

import com.stratio.hermes.utils.HermesLogging
import com.typesafe.config.Config
import kafka.admin.AdminUtils
import kafka.utils.ZkUtils
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class KafkaClientIT extends FlatSpec with Matchers with HermesLogging {

  val Message = "testMessage"
  val Topic = s"topic-${UUID.randomUUID().toString}"
  val PollTime = 200
  val SessionTimeout = 1000
  val ConnectionTimeout = 1000
  val IsZkSecurityEnabled = false
  val Partitions = 1
  val ReplicationFactor = 1
  val TimeoutPoll = 100
  val NumberOfMessages = 3
  implicit val config = com.stratio.hermes.implicits.HermesImplicits.config

  "A KafkaProducer" should "parse the configuration of the consumer correctly" in {
    val kafka = new KafkaClient()
    Option(kafka.parseProperties("kafkaConsumer").getProperty("bootstrap.servers")) should not be (None)
  }

  it should "parse the configuration of the producer correctly" in {
    val kafka = new KafkaClient()
    kafka.parseProperties("kafkaProducer").getProperty("bootstrap.servers") should not be (None)
    kafka.parseProperties().getProperty("bootstrap.servers") should not be (None)
  }

  it should "produce and consume messages" in {
    createTopic(Topic)
    val kafka = new KafkaClient()
    kafka.consumer.subscribe(util.Arrays.asList(Topic))
    kafka.consumer.poll(TimeoutPoll).count() should be (0)
    (1 to NumberOfMessages).foreach(_ => kafka.send(Topic, Message))
    kafka.producer.flush()
    kafka.producer.close()
    kafka.consumer.poll(TimeoutPoll).count should be (NumberOfMessages)
    kafka.consumer.commitSync()
    kafka.consumer.close()
  }

  def createTopic(topicName: String)(implicit config: Config): Unit = {
    val connectionString = config.getString("zk.connectionString")
    val zkUtils = ZkUtils.apply(
      connectionString,
      SessionTimeout,
      ConnectionTimeout,
      IsZkSecurityEnabled)
    AdminUtils.createTopic(zkUtils, topicName, Partitions, ReplicationFactor, new Properties())
  }
}
