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

package com.stratio.khermes

import java.io.File
import java.net.InetAddress
import java.util.Date

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import com.stratio.khermes.clients.http.flows.WSFlow
import com.stratio.khermes.cluster.collector.CommandCollectorActor
import com.stratio.khermes.cluster.supervisor.{KhermesClientActor, NodeSupervisorActor}
import com.stratio.khermes.commons.constants.AppConstants
import com.stratio.khermes.commons.implicits.AppImplicits
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import scala.util.{Failure, Success, Try}

/**
 * Entry point of the application.
 */
object Khermes extends App with LazyLogging {

  import AppImplicits._
  welcome
  createPaths

  val khermesSupervisor = workerSupervisor

  if(config.getString("khermes.client") == "true") {
    clientActor(khermesSupervisor)
  }

  if(config.getString("khermes.ws") == "true") {
    wsHttp()
  }

  /**
   * Prints a welcome message with some information about the system.
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

  /**
   * Creates necessary paths used mainly to generate and compile Twirl templates.
   * @param config with all necessary configuration.
   */
  def createPaths(implicit config: Config): Unit = {
    val templatesFile = new File(config.getString("khermes.templates-path"))
    if(!templatesFile.exists()) {
      logger.info(s"Creating templates path: ${templatesFile.getAbsolutePath}")
      templatesFile.mkdirs()
    }
  }


  def workerSupervisor(implicit config: Config,
                       system: ActorSystem,
                       executionContext: ExecutionContextExecutor): ActorRef =
    system.actorOf(Props(new NodeSupervisorActor()), "khermes-supervisor")

  def clientActor(khermesSupervisor: ActorRef)(implicit config: Config,
                                               system: ActorSystem,
                                               executionContext: ExecutionContextExecutor): Unit = {

    val clientActor = system.actorOf(Props(new KhermesClientActor()), "khermes-client")
    clientActor ! KhermesClientActor.Start
  }

  def wsHttp()(implicit config: Config,
               system: ActorSystem,
               executionContext: ExecutionContextExecutor): Unit = {
    val commandCollector = system.actorOf(CommandCollectorActor.props)

    val routes =
      get {
        pathPrefix("css") {
          getFromResourceDirectory("web/css")
        } ~
          pathPrefix("js") {
            getFromResourceDirectory("web/js")
          } ~
          pathSingleSlash {
              getFromResource("web/index.html")
          } ~
          path("console") {
            getFromResource("web/console.html")
          } ~
          path("input") {
            handleWebSocketMessages(WSFlow.inputFlow(commandCollector))
          } ~
          path("output") {
            handleWebSocketMessages(WSFlow.outputFlow)
          }
      }

    val host = Try(config.getString("khermes.ws.host")).getOrElse({
      logger.info("khermes.ws.host is not defined. Setting default: localhost")
      AppConstants.DefaultWSHost
    })

    val port = Try(config.getInt("khermes.ws.port")).getOrElse({
      logger.info("khermes.ws.port is not defined. Setting default: 8081")
      AppConstants.DefaultWSPort
    })

    logger.info("Binding routes......")
    val binding = Http().bindAndHandle(routes, "0.0.0.0", port)

    binding.onComplete {
      case Success(b) ⇒ logger.info(s"Started WebSocket Command Server online at ${b.localAddress}")
      case Failure(t) ⇒ logger.error("Failed to start HTTP server")
    }
    while(true){}
    binding.flatMap(_.unbind()).onComplete(_ ⇒ system.terminate())
  }
}
