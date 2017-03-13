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

package com.stratio.hermes.actors

import akka.actor.{Actor, ActorLogging}
import akka.cluster.pubsub.{DistributedPubSub, DistributedPubSubMediator}
import com.stratio.hermes.helpers.{HermesConfig, HermesConsoleHelper, HermesClientActorHelper}
import com.typesafe.config.Config

import scala.concurrent.Future

/**
 * This actor starts a client with an interactive shell to throw commands through the cluster.
 * @param config with passed configuration
 */
class HermesClientActor(implicit config: Config) extends Actor with ActorLogging {

  import DistributedPubSubMediator.Publish
  val mediator = DistributedPubSub(context.system).mediator

  override def receive: Receive = {
    case HermesClientActor.Start =>
      import scala.concurrent.ExecutionContext.Implicits.global
      Future(new HermesConsoleHelper(this).parseLines())

    case message =>
      //scalastyle:off
      println(s"$message")
    //scalastyle:on
  }

  /**
   * Sends to the cluster a list message.
   */
  def ls: Unit = {
    mediator ! Publish("content", HermesSupervisorActor.List(Seq.empty))
  }

  /**
   * Starts event generation in N nodes.
   * @param hermesConfigOption with Hermes' configuration.
   * @param kafkaConfigOption with Kafka's configuration.
   * @param templateOption with the template
   * @param nodeIds with the ids that should be start the generation.
   *                If this Seq is empty it will try to start all of them.
   */
  def start(hermesConfigOption: Option[String],
            kafkaConfigOption: Option[String],
            templateOption: Option[String],
            avroConfigOption: Option[String],
            nodeIds: Seq[String]): Unit = {
    (for {
      hermesConfig <- hermesConfigOption
      kafkaConfig <- kafkaConfigOption
      template <- templateOption
    } yield {
      mediator ! Publish("content",
        HermesSupervisorActor.Start(nodeIds, HermesConfig(hermesConfig, kafkaConfig, template, avroConfigOption)))
    }).getOrElse({
      //scalastyle:off
      println(HermesClientActorHelper.messageFeedback(hermesConfigOption,kafkaConfigOption,templateOption))
      //scalastyle:on
    })
  }


  /**
   * Stops event generation in N nodes.
   * @param nodeIds with the ids that should be stop the generation.
   *                If this Seq is empty it will try to start all of them.
   */
  def stop(nodeIds: Seq[String]): Unit =
    mediator ! Publish("content", HermesSupervisorActor.Stop(nodeIds))

}


object HermesClientActor {

  case object Start
}
