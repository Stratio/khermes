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

import akka.actor.Props
import com.stratio.khermes.cluster.BaseActorTest
import com.stratio.khermes.cluster.supervisor.NodeSupervisorActor.{Start, WorkerStatus}
import com.stratio.khermes.commons.config.AppConfig

import scala.concurrent.duration._

//@RunWith(classOf[JUnitRunner])
class NodeSupervisorActorTest extends BaseActorTest {

  val nodeSupervisor = system.actorOf(Props(new NodeSupervisorActor()), "node-supervisor")

  val kafkaConfigContent =
    """
      |kafka {
      |  bootstrap.servers = "localhost:9092"
      |  acks = "-1"
      |  key.serializer = "org.apache.kafka.common.serialization.StringSerializer"
      |  value.serializer = "org.apache.kafka.common.serialization.StringSerializer"
      |}
    """.stripMargin

  val khermesConfigContent =
    """
      |khermes {
      |  templates-path = "/tmp/khermes/templates"
      |  topic = "chustas"
      |  template-name = "chustasTemplate"
      |  i18n = "ES"
      |}
    """.stripMargin

  val templateContent =
    """
      |@import com.stratio.khermes.helpers.faker.Faker
      |
      |@(faker: Faker)
      |{
      |  "name" : "@(faker.Name.firstName)"
      |}
    """.stripMargin

  val avroContent =
    """
      |{
      |  "type": "record",
      |  "name": "myrecord",
      |  "fields":
      |    [
      |      {"name": "name", "type":"int"}
      |    ]
      |}
    """.stripMargin

  "An WorkerSupervisorActor" should {
    "Start n threads of working kafka producers" in {
      within(10 seconds) {
        nodeSupervisor ! Start(Seq.empty, AppConfig(khermesConfigContent, Some(kafkaConfigContent), None, templateContent))
        expectMsg(WorkerStatus.Started)
      }
    }
  }
}
