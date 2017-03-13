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

package com.stratio.khermes.actors

import akka.actor.Props
import com.stratio.khermes.actors.KHermesSupervisorActor.{Start, WorkerStatus}
import com.stratio.khermes.helpers.KHermesConfig
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class WorkerSupervisorActor extends KHermesActorTest {

  val khermesSupervisor = system.actorOf(Props(new KHermesSupervisorActor()), "khermes-supervisor")

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
      |@import com.stratio.khermes.utils.KHermes
      |
      |@(khermes: KHermes)
      |{
      |  "name" : "@(khermes.Name.firstName)"
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
      within(5 seconds) {
        khermesSupervisor ! Start(Seq.empty, KHermesConfig(khermesConfigContent, kafkaConfigContent, templateContent))
        expectMsg(WorkerStatus.Started)
      }
    }
  }
}
