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

package com.stratio.khermes.kafka
import java.util.UUID

import com.stratio.khermes.utils.EmbeddedServersUtils
import com.typesafe.scalalogging.LazyLogging
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class KafkaClientTest extends FlatSpec
  with Matchers
  with LazyLogging
  with EmbeddedServersUtils{

  val Message = "testMessage"
  val Topic = s"topic-${UUID.randomUUID().toString}"
  val NumberOfMessages = 3

  "A KafkaClient" should "parse the configuration" in {
    withEmbeddedKafkaServer(Seq(Topic)) { kafkaServer =>
      withKafkaClient[Object](kafkaServer) { kafkaClient =>
        Option(kafkaClient.parseProperties().getProperty("bootstrap.servers")) should not be (None)
      }
    }
  }

  it should "produce messages in a topic" in {
    withEmbeddedKafkaServer(Seq(Topic)) { kafkaServer =>
      withKafkaClient[Object](kafkaServer) { kafkaClient =>
        (1 to NumberOfMessages).foreach(_ => {
          kafkaClient.send(Topic, Message)
        })
        kafkaClient.producer.flush()
        kafkaClient.producer.close()
      }
    }
  }
}