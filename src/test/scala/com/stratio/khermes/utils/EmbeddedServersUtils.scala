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

package com.stratio.khermes.utils

import java.io.File
import java.util.Properties

import com.stratio.khermes.kafka.KafkaClient
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import kafka.server.{KafkaConfig, KafkaServer}
import kafka.utils.{SystemTime, TestUtils}
import org.apache.curator.test.TestingServer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.protocol.SecurityProtocol
import org.junit.rules.TemporaryFolder

import scala.util.Try

trait EmbeddedServersUtils extends LazyLogging {
  type TopicName = String
  val zookeeperConnectString = "127.0.0.1:2181"
  val tmpFolder = new TemporaryFolder()
  tmpFolder.create()
  val logDir = tmpFolder.newFolder("kafkatest")
  val loggingEnabled = true

  def withEmbeddedKafkaServer(topicsToBeCreated: Seq[TopicName])(function: KafkaServer => Any): Unit = {
    withEmbeddedZookeeper() { zookeeperServer =>
      zookeeperServer.start
      val kafkaConfig = new KafkaConfig(kafkaConfiguration(logDir, zookeeperServer.getConnectString), loggingEnabled)

      logger.debug("Starting embedded Kafka broker (with log.dirs={} and ZK ensemble at {}) ...",
        logDir, zookeeperConnectString)

      val kafkaServer = TestUtils.createServer(kafkaConfig, SystemTime)
      Try {
        kafkaServer.startup
        val brokerList =
          s"""${kafkaServer.config.hostName}:${
            Integer.toString(kafkaServer.boundPort(SecurityProtocol.PLAINTEXT))
          }"""

        logger.debug("Startup of embedded Kafka broker at {} completed (with ZK ensemble at {}) ...",
          brokerList, zookeeperConnectString)

        function(kafkaServer)
      }
      kafkaServer.shutdown
      zookeeperServer.stop
    }
  }


  def withEmbeddedZookeeper()(function: TestingServer => Any): Unit = {
    function(new TestingServer())
  }

  def withKafkaProducer[V](kafkaServer: KafkaServer)(testFunction: KafkaProducer[String, V] => Any): Unit = {
    val props = kafkaServer.config.originals
    val producer: KafkaProducer[String, V] = new KafkaProducer(props)
    testFunction(producer)
  }

  def withKafkaClient[V](kafkaServer: KafkaServer)(function: KafkaClient[V] => Any): Unit = {
    val kafkaClient = new KafkaClient[V](ConfigFactory.parseMap(kafkaServer.config.originals))
    function(kafkaClient)
  }

  //TODO: Accept initial config parameter (specific traits)
  private def kafkaConfiguration(logDir: File, zkConnectString: String) = {
    val kafkaConfig = new Properties()
    kafkaConfig.put(KafkaConfig.ZkConnectProp, zkConnectString)
    kafkaConfig.put(KafkaConfig.BrokerIdProp, "0")
    kafkaConfig.put(KafkaConfig.HostNameProp, "127.0.0.1")
    kafkaConfig.put(KafkaConfig.PortProp, "9092")
    kafkaConfig.put(KafkaConfig.NumPartitionsProp, "1")
    kafkaConfig.put(KafkaConfig.AutoCreateTopicsEnableProp, "true")
    kafkaConfig.put(KafkaConfig.MessageMaxBytesProp, "1000000")
    kafkaConfig.put(KafkaConfig.ControlledShutdownEnableProp, "true")
    kafkaConfig.put("kafka.bootstrap.servers", "127.0.0.1:9092")
    kafkaConfig.put("kafka.key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    kafkaConfig.put("kafka.value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    kafkaConfig.setProperty(KafkaConfig.LogDirProp, logDir.getAbsolutePath)
    //effectiveConfig.putAll(initialConfig);
    kafkaConfig
  }

}
