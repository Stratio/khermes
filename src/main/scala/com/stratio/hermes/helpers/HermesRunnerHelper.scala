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

package com.stratio.hermes.helpers

import java.io.File
import java.util.Date

import akka.actor.{ActorRef, ActorSystem, Props}
import com.stratio.hermes.actors.HermesSupervisorActor
import com.stratio.hermes.actors.HermesSupervisorActor.Start
import com.stratio.hermes.constants.HermesConstants
import com.stratio.hermes.utils.HermesLogging
import com.typesafe.config.Config

import scala.concurrent.ExecutionContextExecutor

/**
 * Common operations used when Hermes starts.
 */
object HermesRunnerHelper extends HermesLogging {

  /**
   * Prints a welcome message with some information about the system and creates necessary paths.
   * @param system
   */
  def welcome(implicit system: ActorSystem, config: Config): Unit = {
    log.info(
      s"""
         |╦ ╦┌─┐┬─┐┌┬┐┌─┐┌─┐
         |╠═╣├┤ ├┬┘│││├┤ └─┐
         |╩ ╩└─┘┴└─┴ ┴└─┘└─┘ Powered by Stratio (www.stratio.com)
         |
         |> System Name   : ${system.name}
         |> Start time    : ${new Date(system.startTime)}
         |> Number of CPUs: ${Runtime.getRuntime.availableProcessors}
         |> Total memory  : ${Runtime.getRuntime.totalMemory}
         |> Free memory   : ${Runtime.getRuntime.freeMemory}
    """.stripMargin)
  }

  val kafkaConfigContent =
    """
      |kafka {
      |  bootstrap.servers = "localhost:9092"
      |  acks = "-1"
      |  key.serializer = "org.apache.kafka.common.serialization.StringSerializer"
      |  value.serializer = "org.apache.kafka.common.serialization.StringSerializer"
      |}
    """.stripMargin

  val hermesConfigContent =
    """
      |hermes {
      |  templates-path = "/tmp/hermes/templates"
      |  topic = "template"
      |  template-name = "tmpTemplate"
      |  i18n = "ES"
      |}
    """.stripMargin

  val templateContent =
    """
      |@import com.stratio.hermes.utils.Hermes
      |
      |@(hermes: Hermes)
      |{
      |  "name" : "@(hermes.Name.firstName)"
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

  def createPaths(implicit config: Config): Unit = {
    val templatesFile = new File(config.getString("hermes.templates-path"))
    if(!templatesFile.exists()) {
      log.info(s"Creating templates path: ${templatesFile.getAbsolutePath}")
      templatesFile.mkdirs()
    }
  }

  def workerSupervisor(implicit config: Config,
                       system: ActorSystem,
                       executionContext: ExecutionContextExecutor): ActorRef =
    system.actorOf(Props(new HermesSupervisorActor()), "hermes-supervisor")

  def clientActor(hermesSupervisor: ActorRef)(implicit config: Config,
                                              system: ActorSystem,
                                              executionContext: ExecutionContextExecutor): Unit = {
    import scala.concurrent.duration._
    system.scheduler.scheduleOnce(HermesConstants.WorkerSupervisorTimeout seconds) {
      hermesSupervisor ! Start(Seq.empty, HermesConfig(hermesConfigContent, kafkaConfigContent, templateContent))
    }

  }
}
