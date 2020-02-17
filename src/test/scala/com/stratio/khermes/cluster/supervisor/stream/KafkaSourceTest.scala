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
package com.stratio.khermes.cluster.supervisor.stream

import akka.Done
import akka.actor.Props
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.{ActorMaterializer, UniqueKillSwitch}
import akka.stream.scaladsl.{Keep, Sink}
import com.stratio.khermes.cluster.BaseActorTest
import com.stratio.khermes.cluster.supervisor.SourceImplementations
import com.stratio.khermes.commons.config.{AppConfig, AppConfigTest}
import com.stratio.khermes.helpers.twirl.TwirlActorCache
import com.stratio.khermes.persistence.kafka.KafkaClient
import com.stratio.khermes.utils.EmbeddedServersUtils
import kafka.server.KafkaServer
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

@RunWith(classOf[JUnitRunner])
class KafkaSourceTest extends BaseActorTest with EmbeddedServersUtils {

  import CommonsConfig._
  import StreamGenericOperations._

  implicit val am = ActorMaterializer()
  def kafkaConfig(server: KafkaServer): String = {
    val kafkaConfig: String = s"""
                         |kafka {
                         |  bootstrap.servers = "localhost:${server.config.port}"
                         |  acks = "-1"
                         |  key.serializer = "org.apache.kafka.common.serialization.StringSerializer"
                         |  value.serializer = "org.apache.kafka.common.serialization.StringSerializer"
                         |}
                              """.stripMargin

    kafkaConfig
  }

  def kafkaSource(hc: AppConfig): (UniqueKillSwitch, Future[Done]) = {
    val twirlActorCacheProps  = Props(new TwirlActorCache(hc))
    val twitlActorCacheRef    = system.actorOf(twirlActorCacheProps)
    val dataPublisherProps    = Props(new EventPublisher(hc, twitlActorCacheRef))
    val dataPublisherRef      = system.actorOf(dataPublisherProps)

    implicit val client = new KafkaClient[String](hc.kafkaConfig.get)

    val source = SourceImplementations(hc, dataPublisherRef).createKafkaSource.commonStart(hc)
    source
  }

  "A Kafka Source implementation" should {
    "Create a which publishes to kafka n generated events" in {

      withEmbeddedKafkaServer(List("khermes")) { server: KafkaServer =>

        val hc = AppConfigTest.testConfig.copy(kafkaConfigContent = Some(kafkaConfig(server)),
          localFileConfigContent = None, khermesConfigContent = khermesConfigContent, template = templateContent)

        kafkaSource(hc)

        val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
          .withBootstrapServers(s"localhost:${server.config.port}")
          .withGroupId("group1")

        val future = Consumer.committableSource(consumerSettings, Subscriptions.topics("khermes"))
          .map(_.record.value())
          .takeWithin(10 seconds)
          .toMat(Sink.fold(List[String]())((a, b) => {
            b :: a
          }))(Keep.right)

        Await.result(future.run(), 60 seconds).length shouldBe 60

      }
    }

    "Write n generated events to Kafka without timeout rules" in {

      withEmbeddedKafkaServer(List("khermes")) { server: KafkaServer =>
        val khermesConfigContent =
          """
            |khermes {
            |  templates-path = "/tmp/khermes/templates"
            |  topic = "khermes"
            |  template-name = "khermestemplate"
            |  i18n = "EN"
            |
            |  stop-rules {
            |    number-of-events: 5
            |  }
            |}
          """.
            stripMargin

        val hc = AppConfigTest.testConfig.copy(kafkaConfigContent = Some(kafkaConfig(server)),
          localFileConfigContent = None, khermesConfigContent = khermesConfigContent, template = templateContent)

        kafkaSource(hc)

        val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
          .
            withBootstrapServers(s"localhost:${server.config.port}")
          .withGroupId("group1")

        val future = Consumer.committableSource(consumerSettings, Subscriptions.topics("khermes"))
          .map(_.record.value())
          .takeWithin(10 seconds)
          .toMat(Sink.fold(List[String]())((a, b) => {
            b :: a
          }))(Keep.right)

        Await.result(future.run(), 60 seconds).length shouldBe 5
      }
    }

    "Write n generated events to a file with a timeout rule" in {
      withEmbeddedKafkaServer(List("khermes")) { server: KafkaServer =>
        val khermesConfigContent =
          """
            |khermes {
            |  templates-path = "/tmp/khermes/templates"
            |  topic = "khermes"
            |  template-name = "khermestemplate"
            |  i18n = "EN"
            |
            |  stop-rules {
            |    number-of-events: 5
            |  }
            |
            |  timeout-rules {
            |    number-of-events: 1
            |    duration: 5
            |  }
            |}
          """.stripMargin

        val hc = AppConfigTest.testConfig.copy(kafkaConfigContent = Some(kafkaConfig(server)),
          localFileConfigContent = None, khermesConfigContent = khermesConfigContent, template = templateContent)

        kafkaSource(hc)

        val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
          .withBootstrapServers(s"localhost:${server.config.port}")
          .withGroupId("group1")

        val future = Consumer.committableSource(consumerSettings, Subscriptions.topics("khermes"))
          .map(_.record.value())
          .takeWithin(10 seconds)
          .toMat(Sink.fold(List[String]())((a, b) => {
            b :: a
          }))(Keep.right)

        Await.result(future.run(), 60 seconds).length shouldBe 5
      }
    }
  }
}
