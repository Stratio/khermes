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