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

package com.stratio.khermes.helpers

import java.io.File
import java.util.Date

import akka.actor.{ActorRef, ActorSystem, Props}
import com.stratio.khermes.actors.{KhermesClientActor, KhermesSupervisorActor}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContextExecutor

/**
 * Common operations used when Khermes starts.
 */
object KhermesRunnerHelper extends LazyLogging {

  /**
   * Prints a welcome message with some information about the system and creates necessary paths.
   * @param system
   */
  def welcome(implicit system: ActorSystem, config: Config): Unit = {
    logger.info(
      s"""
         |╦╔═┬ ┬┌─┐┬─┐┌┬┐┌─┐┌─┐
         |╠╩╗├─┤├┤ ├┬┘│││├┤ └─┐
         |╩ ╩┴ ┴└─┘┴└─┴ ┴└─┘└─┘ Powered by Stratio (www.stratio.com)
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

  val khermesConfigContent =
    """
      |khermes {
      |  templates-path = "/tmp/khermes/templates"
      |  topic = "alicia"
      |  template-name = "chustasTemplate"
      |  i18n = "ES"
      |
      |  timeout-rules {
      |    number-of-events: 1000
      |    duration: 2 seconds
      |  }
      |
      |  stop-rules {
      |    number-of-events: 5000
      |  }
      |}
    """.stripMargin

  val templateContent =
    """
      |@import com.stratio.khermes.utils.Khermes
      |
      |@(khermes: Khermes)
      |{
      |  "name" : "alicia"
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
    val templatesFile = new File(config.getString("khermes.templates-path"))
    if(!templatesFile.exists()) {
      logger.info(s"Creating templates path: ${templatesFile.getAbsolutePath}")
      templatesFile.mkdirs()
    }
  }

  //scalastyle:off

  def workerSupervisor(implicit config: Config,
                       system: ActorSystem,
                       executionContext: ExecutionContextExecutor): ActorRef =
    system.actorOf(Props(new KhermesSupervisorActor()), "khermes-supervisor")

  def clientActor(khermesSupervisor: ActorRef)(implicit config: Config,
                                               system: ActorSystem,
                                               executionContext: ExecutionContextExecutor): Unit = {

    val clientActor = system.actorOf(Props(new KhermesClientActor()), "khermes-client")
    clientActor ! KhermesClientActor.Start
  }
}
