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
import com.stratio.khermes.clients.http.protocols.WsProtocolCommand.WsProtocolCommandValue
import com.stratio.khermes.clients.http.protocols.{WSProtocolMessage, WsProtocolCommand}
import com.stratio.khermes.cluster.collector.CommandCollectorActor.CheckCommandHasEnd
import com.stratio.khermes.cluster.supervisor.NodeSupervisorActor
import com.stratio.khermes.cluster.supervisor.NodeSupervisorActor.Result
import com.stratio.khermes.commons.config.AppConfig
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
    context.system.scheduler.schedule(0 milliseconds, CheckCommandStateTimeout, self, CheckCommandHasEnd)
  }

  //scalastyle:off
  override def receive: Receive = {
    case WSProtocolMessage(WsProtocolCommand.Ls, _) =>
      ls

    case WSProtocolMessage(WsProtocolCommand.Start, args) =>
      start(args)

    case WSProtocolMessage(WsProtocolCommand.Stop, args) =>
      stop(args)

    case WSProtocolMessage(WsProtocolCommand.CreateTwirlTemplate, args) =>
      createConfig(args, WsProtocolCommand.CreateTwirlTemplate, AppConstants.TwirlTemplatePath)

    case WSProtocolMessage(WsProtocolCommand.CreateGeneratorConfig, args) =>
      createConfig(args, WsProtocolCommand.CreateGeneratorConfig, AppConstants.GeneratorConfigPath)

    case WSProtocolMessage(WsProtocolCommand.CreateKafkaConfig, args) =>
      createConfig(args, WsProtocolCommand.CreateKafkaConfig, AppConstants.KafkaConfigPath)

    case WSProtocolMessage(WsProtocolCommand.CreateAvroConfig, args) =>
      createConfig(args, WsProtocolCommand.CreateAvroConfig, AppConstants.AvroConfigPath)

    case WSProtocolMessage(WsProtocolCommand.ShowTwirlTemplate, _) =>
      showConfig(AppConstants.TwirlTemplatePath)

    case WSProtocolMessage(WsProtocolCommand.ShowGeneratorConfig, _) =>
      showConfig(AppConstants.GeneratorConfigPath)

    case WSProtocolMessage(WsProtocolCommand.ShowKafkaConfig, _) =>
      showConfig(AppConstants.KafkaConfigPath)

    case WSProtocolMessage(WsProtocolCommand.ShowAvroConfig, _) =>
      showConfig(AppConstants.AvroConfigPath)

    case result: NodeSupervisorActor.Result =>
      collectResult(result)

    case CheckCommandHasEnd =>
      checkCommandHasEnd

    case message: CommandCollectorActor.Result =>
      performOnNext(message)
  }

  def collectResult(result: NodeSupervisorActor.Result): Unit = {
    Try(commands(result.commandId)).toOption
      .map(x => commands += (result.commandId -> (x._1, (x._2 ::: List(result)))))
      .getOrElse(commands += result.commandId -> (System.currentTimeMillis(), List(result)))
  }

  def ls(): Unit = {
    val commandId = UUID.randomUUID().toString
    mediator ! Publish("content", NodeSupervisorActor.List(Seq.empty, commandId))
  }

  def start(args: Map[String, String]): Unit = {
    val argsTwirlTemplate = args.get(WsProtocolCommand.ArgsTwirlTemplate).getOrElse(
      throw new IllegalArgumentException("a twirl-template must be supplied when you send a Start signal"))
    val argsKafkaConfig = args.get(WsProtocolCommand.ArgsKafkaConfig).getOrElse(
      throw new IllegalArgumentException("a kafka-config must be supplied when you send a Start signal"))
    val argsGeneratorConfig = args.get(WsProtocolCommand.ArgsGeneratorConfig).getOrElse(
      throw new IllegalArgumentException("a generator-config must be supplied when you send a Start signal"))
    val argsAvroConfigOption = args.get(WsProtocolCommand.ArgsAvroConfig)
    val nodeIds = args.get(WsProtocolCommand.ArgsNodeIds).map(value => value.split(" ")).toSeq.flatten

    val twirlTemplate = configDAO.read(s"${AppConstants.TwirlTemplatePath}/$argsTwirlTemplate")
    val kafkaConfig = configDAO.read(s"${AppConstants.KafkaConfigPath}/$argsKafkaConfig")
    val generatorConfig = configDAO.read(s"${AppConstants.GeneratorConfigPath}/$argsGeneratorConfig")
    val avroConfig = argsAvroConfigOption.map(
      argsAvroConfig => configDAO.read(s"${AppConstants.AvroConfigPath}/$argsAvroConfig"))

    mediator ! Publish("content",
      NodeSupervisorActor.Start(nodeIds, AppConfig(generatorConfig, kafkaConfig, twirlTemplate, avroConfig)))
    self ! Result("OK", s"Sending Start signal to nodes ${nodeIds.mkString(" ")}")
  }

  def stop(args: Map[String, String]): Unit = {
    val nodeIds = args.get(WsProtocolCommand.ArgsNodeIds).map(value => value.split(" ")).toSeq.flatten
    mediator ! Publish("content", NodeSupervisorActor.Stop(nodeIds))
    self ! Result("OK", s"Sending Stop signal to nodes ${nodeIds.mkString(" ")}")
  }

  def createConfig(args: Map[String,String], protocolCommand: WsProtocolCommandValue, basePath: String): Unit = {
    val name = args.get(WsProtocolCommand.ArgsName).getOrElse(
      throw new IllegalArgumentException(s"Not found name for ${protocolCommand.toString}"))
    val content = args.get(WsProtocolCommand.ArgsContent).getOrElse(
      throw new IllegalArgumentException(s"not found content for ${protocolCommand.toString}"))

    configDAO.create(s"$basePath/$name", content)
    self ! Result("OK", s"Created node in ZK: $basePath/$name")
  }

  def showConfig(basePath: String): Unit ={
    val list = configDAO.list(s"$basePath")
    self ! Result(s"OK \n$list", s"Show config of $basePath")
  }

  def checkCommandHasEnd(): Unit = {
    val currentMembersInCluster = membersInCluster
    commands.filter(element => {
      (currentMembersInCluster == element._2._2.size || System.currentTimeMillis() - element._2._1 > MaxCommandTimeout)
    }).map(element => {
      val result = element._2._2.map(_.value).mkString("\n")
      commands.remove(element._1)
      context.system.eventStream.publish(CommandCollectorActor.Result(result))
    })
  }

  def performOnNext(message: CommandCollectorActor.Result): Unit = {
    if(totalDemand > 0 && isActive) {
      onNext(message)
    }
  }

  def membersInCluster: Int =
    akka.cluster.Cluster(context.system).state.members.filter(_.status == MemberStatus.Up).size
}

object CommandCollectorActor {
  case object CheckCommandHasEnd
  case class Result(value: String)
  def props: Props = Props[CommandCollectorActor]
}
