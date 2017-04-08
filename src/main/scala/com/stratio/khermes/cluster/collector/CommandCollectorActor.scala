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

package com.stratio.khermes.cluster.collector

import java.util.UUID

import akka.actor.{ActorLogging, Props}
import akka.cluster.MemberStatus
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import akka.stream.actor.ActorPublisher
import com.stratio.khermes.clients.http.protocols.{WSProtocolMessage, WsProtocolCommand}
import com.stratio.khermes.cluster.collector.CommandCollectorActor.Check
import com.stratio.khermes.cluster.supervisor.NodeSupervisorActor
import com.stratio.khermes.cluster.supervisor.NodeSupervisorActor.Result
import com.stratio.khermes.commons.constants.AppConstants
import com.stratio.khermes.commons.implicits.AppImplicits._

import scala.concurrent.duration._
import scala.util.Try

class CommandCollectorActor extends ActorPublisher[CommandCollectorActor.Result] with ActorLogging {

  val MaxCommandTimeout = 10L
  val CheckCommandStateTimeout = 100 milliseconds
  val mediator = DistributedPubSub(context.system).mediator

  var commands = scala.collection.mutable.HashMap.empty[String, (Long, List[Result])]

  override def preStart: Unit = {
    implicit val executionContext = context.system.dispatcher
    context.system.eventStream.subscribe(self, classOf[CommandCollectorActor.Result])
    context.system.scheduler.schedule(0 milliseconds, CheckCommandStateTimeout, self, Check)
  }

  //scalastyle:off
  override def receive: Receive = {
    case WSProtocolMessage(command, args)=>
      command match {
        case WsProtocolCommand.Ls =>
          val commandId = UUID.randomUUID().toString
          mediator ! Publish("content", NodeSupervisorActor.List(Seq.empty, commandId))

        case WsProtocolCommand.CreateTwirlTemplate =>
          val name = args.get("name").getOrElse(throw new IllegalArgumentException("not found a name for a twirl-template"))
          val content =args.get("content").getOrElse(throw new IllegalArgumentException("not found a content for a twirl-template"))
          configDAO.create(s"${AppConstants.TwirlTemplatePath}/${name}", content)
          self ! Result("OK", "")

        case WsProtocolCommand.CreateGeneratorConfig =>
          val name = args.get("name").getOrElse(throw new IllegalArgumentException("not found a name for a generator-config"))
          val content =args.get("content").getOrElse(throw new IllegalArgumentException("not found a content for a generator-config"))
          configDAO.create(s"${AppConstants.GeneratorConfigPath}/${name}", content)
          self ! Result("OK", "")

        case WsProtocolCommand.CreateKafkaConfig =>
          val name = args.get("name").getOrElse(throw new IllegalArgumentException("not found a name for a kafka-config"))
          val content =args.get("content").getOrElse(throw new IllegalArgumentException("not found a content for a kafka-config"))
          configDAO.create(s"${AppConstants.KafkaConfigPath}/${name}", content)
          self ! Result("OK", "")

        case WsProtocolCommand.CreateAvroConfig =>
          val name = args.get("name").getOrElse(throw new IllegalArgumentException("not found a name for a avro-config"))
          val content =args.get("content").getOrElse(throw new IllegalArgumentException("not found a content for a avro-config"))
          configDAO.create(s"${AppConstants.AvroConfigPath}/${name}", content)
          self ! Result("OK", "")
      }

    case result: NodeSupervisorActor.Result =>
      Try(commands(result.commandId)).toOption
        .map(x => commands += (result.commandId -> (x._1, (x._2 ::: List(result)))))
        .getOrElse(commands += result.commandId -> (System.currentTimeMillis(), List(result)))

    case Check =>
      val currentMembersInCluster = membersInCluster

      commands.filter(element => {
        (currentMembersInCluster == element._2._2.size || System.currentTimeMillis() - element._2._1 > MaxCommandTimeout)
      }).map(element => {
        val result = element._2._2.map(_.value).mkString("\n")
        commands.remove(element._1)
        context.system.eventStream.publish(CommandCollectorActor.Result(result))
      })

    case message: CommandCollectorActor.Result =>
      if(totalDemand > 0 && isActive) {
        onNext(message)
      }
  }
  //scalastyle:on

  def membersInCluster: Int =
    akka.cluster.Cluster(context.system).state.members.filter(_.status == MemberStatus.Up).size
}

object CommandCollectorActor {
  case object Check
  case class Result(value: String)
  def props: Props = Props[CommandCollectorActor]
}
