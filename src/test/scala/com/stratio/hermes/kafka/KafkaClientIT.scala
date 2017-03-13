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

import java.util.UUID

import com.stratio.hermes.utils.HermesLogging
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
  val ConnectionString = "localhost:2181"

  val config = com.stratio.hermes.implicits.HermesImplicits.config

  "A KafkaClient" should "parse the configuration" in {
    val kafka = new KafkaClient[Object](config)
    Option(kafka.parseProperties().getProperty("bootstrap.servers")) should not be (None)
  }

  it should "produce messages in a topic" in {
    val kafka = new KafkaClient[Object](config)
    (1 to NumberOfMessages).foreach(_ => kafka.send(Topic, Message))
    kafka.producer.flush()
    kafka.producer.close()
  }
}
