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

package com.stratio.khermes.cluster.supervisor

import java.util.UUID

import akka.actor.{ActorLogging, Props}
import akka.cluster.pubsub.{DistributedPubSub, DistributedPubSubMediator}
import akka.stream.actor.ActorPublisher
import com.stratio.khermes.clients.shell.KhermesConsoleHelper
import com.stratio.khermes.commons.config.AppConfig

import scala.concurrent.Future

/**
 * This actor starts a client with an interactive shell to throw commands through the cluster.
 * TODO: (Alvaro Nistal) This class must be deleted and refactored to another one that will use WebSockets with
 * the command collector.
 */
class KhermesClientActor extends ActorPublisher[String] with ActorLogging {

  import DistributedPubSubMediator.Publish
  val mediator = DistributedPubSub(context.system).mediator

  override def preStart: Unit = {
    context.system.eventStream.subscribe(self, classOf[String])
  }

  override def receive: Receive = {
    case KhermesClientActor.Start =>
      import scala.concurrent.ExecutionContext.Implicits.global
      Future(new KhermesConsoleHelper(this).parseLines)

    case result: NodeSupervisorActor.Result  =>
      //scalastyle:off
      println(result.value)
      //scalastyle:on
  }

  /**
   * Sends to the cluster a list message.
   */
  def ls: Unit = {
    mediator ! Publish("content", NodeSupervisorActor.List(Seq.empty, UUID.randomUUID().toString))
  }

  /**
    * Starts event generation in N nodes.
    * @param khermesConfig with Khermes' configuration.
    * @param nodeIds       with the ids that should be start the generation.
    *                      If this Seq is empty it will try to start all of them.
    */
  def start(khermesConfig: AppConfig,
            nodeIds: Seq[String]): Unit = {
    mediator ! Publish("content",
      NodeSupervisorActor.Start(nodeIds, khermesConfig))
  }


  /**
   * Stops event generation in N nodes.
   * @param nodeIds with the ids that should be stop the generation.
   *                If this Seq is empty it will try to start all of them.
   */
  def stop(nodeIds: Seq[String]): Unit =
    mediator ! Publish("content", NodeSupervisorActor.Stop(nodeIds))
}


object KhermesClientActor {

  case object Start

  def props: Props = Props(new KhermesClientActor())

  // TODO (Alvaro Nistal) this method should be refactored.
  def messageFeedback(khermesConfigOption: Option[String],
                      kafkaConfigOption: Option[String],
                      templateOption: Option[String]): String = {
    var m = List[String]()
    if (khermesConfigOption.isEmpty) m = "khermes" :: m
    if (kafkaConfigOption.isEmpty) m = "kafka" :: m
    if (templateOption.isEmpty) m = "template" :: m
    if (m.isEmpty) "Your configuration is OK" else s"Error: To start nodes is necessary to set ${m.mkString(" and ")} configuration."
  }
}

