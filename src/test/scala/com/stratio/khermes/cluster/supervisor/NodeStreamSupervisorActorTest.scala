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
package com.stratio.khermes.cluster.supervisor

import java.io.File
import java.util.Properties

import akka.actor.{Props}
import com.stratio.khermes.cluster.BaseActorTest
import com.stratio.khermes.cluster.supervisor.NodeSupervisorActor.{Result, Start}
import com.stratio.khermes.cluster.supervisor.StreamGenericOperations.EventPublisher
import com.stratio.khermes.commons.config.{AppConfig, AppConfigTest}
import com.stratio.khermes.helpers.twirl.TwirlActorCache
import com.stratio.khermes.persistence.file.FileClient
import com.stratio.khermes.persistence.kafka.KafkaClient
import com.stratio.khermes.utils.EmbeddedServersUtils
import kafka.consumer.ConsumerConfig
import kafka.server.KafkaServer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.{Deserializer, StringDeserializer}
import org.junit.runner.RunWith
import org.scalatest.AsyncFlatSpec
import org.scalatest.junit.JUnitRunner

import scala.language.implicitConversions
import scala.concurrent.duration._
import scala.language.implicitConversions
import scala.collection.JavaConversions._

object kafkaOps {

  def consumerConfig[Key, Msg](kafkaBrokerUri: String,
                               zkUri: String,
                               consumerGroup: String,
                               keyDeserializer: Deserializer[Key],
                               valueDeserializer: Deserializer[Msg]): Map[String, String] = {
    val props = new Properties()
    props.put("bootstrap.servers", kafkaBrokerUri)
    props.put("key.deserializer", keyDeserializer.getClass.getName)
    props.put("value.deserializer", valueDeserializer.getClass.getName)
    props.put("enable.auto.commit", "true")
    props.put("auto.offset.reset", "earliest")
    props.put("group.id", consumerGroup)
    props.put("delete.topic.enable", "true")
    props.put("auto.commit.interval.ms", "1000")
    props.put("session.timeout.ms", "30000")
    props.toMap
  }

}

object CommonsConfig {
  val fileConfigContent =
    """
      |file {
      | path = "/tmp/file2.json"
      |}
    """.stripMargin

  val khermesConfigContent =
    """
      |khermes {
      |  templates-path = "/tmp/khermes/templates"
      |  topic = "khermes"
      |  template-name = "khermestemplate"
      |  i18n = "EN"
      |
      |  timeout-rules {
      |    number-of-events: 10
      |    duration: 5 seconds
      |  }
      |
      |  stop-rules {
      |    number-of-events: 60
      |  }
      |}
    """.stripMargin

  val templateContent =
    """|@import com.stratio.khermes.helpers.faker.generators._
       |@import scala.util.Random
       |@import com.stratio.khermes.helpers.faker.Faker
       |@import com.stratio.khermes.helpers.faker.generators.Positive
       |@import org.joda.time.DateTime
       |@(faker: Faker)
       |@defining(faker, List(CategoryFormat("MASTERCARD", "0.5"),CategoryFormat("VISA", "0.5")),List(CategoryFormat("MOVISTAR", "0.5"),CategoryFormat("IUSACELL", "0.5"))){ case (f,s,s2) =>
       |@f.Name.fullName,@f.Categoric.runNext(s),@f.Number.numberInRange(10000,50000),@f.Geo.geolocation.city,@f.Number.numberInRange(1000,10000),@f.Categoric.runNext(s2),@f.Number.numberInRange(1,5000),@f.Datetime.datetime(new DateTime("2000-01-01"), new DateTime("2016-01-01"), Option("yyyy-MM-dd")) }
    """.stripMargin
}

@RunWith(classOf[JUnitRunner])
class NodeStreamSupervisorActorTest extends BaseActorTest with EmbeddedServersUtils {

  import CommonsConfig._

  val nodeSupervisor = system.actorOf(Props(new NodeStreamSupervisorActor()), "node-supervisor")

  "An NodeStreamSupervisorActor" should {
    "Start a Akka Stream fo event generation" in {
      within(100 seconds) {
        nodeSupervisor ! Start(Seq.empty, AppConfig(khermesConfigContent, None, Some(fileConfigContent), templateContent))
        expectMsgPF(100 seconds) {
          case (id, status) =>
            status shouldBe "Running"
        }
      }
    }
  }

  "An NodeStreamSupervisorActor" should {
    "Stop the Stream when an 'Stop' message is received " in {
      within(50 seconds) {
        var streamId: Seq[String] = Nil
        nodeSupervisor ! Start(Seq.empty, AppConfig(khermesConfigContent, None, Some(fileConfigContent), templateContent))
        expectMsgPF(10 seconds) {
          case (id: String, status) =>
            status shouldBe "Running"
            streamId = Seq(id)
        }

        Thread.sleep(5000)

        nodeSupervisor ! NodeSupervisorActor.Stop(streamId)
        expectMsgPF(10 seconds) {
          case (id: String) =>
            streamId = Seq(id)
        }

        nodeSupervisor ! NodeSupervisorActor.List(streamId, "")
        expectMsgPF(10 seconds) {
          case (Result(status, _)) =>
            status.split(" | ")(2) shouldBe "Stopped"
        }
      }
    }
  }
}
