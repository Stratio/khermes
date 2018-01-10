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

import java.util.UUID

import akka.{Done, NotUsed}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.{DistributedPubSub, DistributedPubSubMediator}
import akka.stream._
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.Source
import com.stratio.khermes.cluster.supervisor.stream.fileimpl.StreamFileOperations.FileOperations
import com.stratio.khermes.cluster.supervisor.stream.kafkaimpl.StreamKafkaOperations.KafkaOperations
import com.stratio.khermes.cluster.supervisor.stream.StreamGenericOperations.createSource
import com.stratio.khermes.cluster.supervisor.NodeSupervisorActor.Result
import com.stratio.khermes.cluster.supervisor.stream.fileimpl.StreamFileOperations
import com.stratio.khermes.cluster.supervisor.stream.kafkaimpl.StreamKafkaOperations
import com.stratio.khermes.commons.config.AppConfig
import com.stratio.khermes.commons.exceptions.KhermesException
import com.stratio.khermes.helpers.faker.Faker
import com.stratio.khermes.helpers.twirl.TwirlActorCache
import com.stratio.khermes.metrics.KhermesMetrics
import com.stratio.khermes.persistence.file.FileClient
import com.stratio.khermes.persistence.kafka.KafkaClient
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext
import scala.util.Try


case class SourceImplementations(hc: AppConfig, publisher: ActorRef)(implicit am: ActorMaterializer, config: Config, ec: ExecutionContext) {
  def createKafkaSource(implicit client: KafkaClient[String]): Source[KafkaOperations, NotUsed] = {
    createSource(hc, ActorPublisher(publisher))
      .via(StreamKafkaOperations.kafkaFlow(hc))
      .map(_.run(KafkaOperations(hc.topic, 0, Nil)).value)
      .map{ case (ops, events) => events.foreach(str => client.send(hc.topic, str)); ops}
  }

  def createFileSource(implicit file: FileClient[String]): Source[FileOperations, NotUsed] =
    createSource(hc, ActorPublisher(publisher))
      .via(StreamFileOperations.fileFlow(hc))
      .map(_.run(FileOperations(0)).value)
      .map{ case (ops, events) => events.foreach(str => file.send(str)); ops}
}


final class NodeStreamSupervisorActor(implicit config: Config) extends Actor with ActorLogging with KhermesMetrics {

  import DistributedPubSubMediator.Subscribe
  import com.stratio.khermes.cluster.supervisor.stream.StreamGenericOperations._

  val id = UUID.randomUUID.toString

  private[this] def isAMessageForMe(ids: Seq[String]): Boolean = ids.contains(id)
  private[this] def execute(ids: Seq[String], callback: () => Unit): Unit =
    if (ids.nonEmpty && !isAMessageForMe(ids)) log.debug("Is not a message for me!") else callback()

  //Put in scope the ActorSystem execution context.
  implicit val ec = context.dispatcher
  implicit val as = context.system
  implicit val am = ActorMaterializer()

  var dataPublisherProps    : Option[Props]    = None
  var dataPublisherRef      : Option[ActorRef] = None
  var twirlActorCacheProps  : Option[Props] = None
  var twitlActorCacheRef    : Option[ActorRef] = None

  val mediator = DistributedPubSub(context.system).mediator
  mediator ! Subscribe("content", self)

  // These variables show the stream status
  var streamHandler : Option[UniqueKillSwitch] = None
  var streamStatus  : Option[Done] = None

  val khermes = Faker(
    Try(config.getString("khermes.i18n")).toOption.getOrElse("EN"),
    Try(config.getString("khermes.strategy")).toOption)

  override def receive : Receive = {
    case NodeSupervisorActor.Start(ids, hc) =>
      execute(ids, () => {
        log.info(s"Received configuration ${hc}")
        twirlActorCacheProps = Some(Props(new TwirlActorCache(hc)))
        twitlActorCacheRef = Some(as.actorOf(twirlActorCacheProps.get))
        dataPublisherProps = Some(Props(new EventPublisher(hc, twitlActorCacheRef.get)))
        dataPublisherRef = Some(as.actorOf(dataPublisherProps.get))

        // Depending on the config it starts File or Kafka flow
        (hc.kafkaConfig, hc.fileConfig) match {
          case (Some(kafka), None) =>
            implicit val client = new KafkaClient[String](kafka)
            val (streamHandler, streamStatus) = SourceImplementations(hc, dataPublisherRef.get).createKafkaSource.commonStart(hc)
            this.streamHandler = Some(streamHandler)
            streamStatus.onComplete(_ => this.streamStatus = Some(Done))

          case (None, Some(file)) =>
            implicit val client = new FileClient[String](hc.filePath)
            val (streamHandler, streamStatus) = SourceImplementations(hc, dataPublisherRef.get).createFileSource.commonStart(hc)
            this.streamHandler = Some(streamHandler)
            streamStatus.onComplete(_ => this.streamStatus = Some(Done))

          case _ =>
            throw new KhermesException("Invalid Sink Data Configuration")
        }
        log.info(s"Node started ${hc}")
      })
      sender ! (id, checkStatus(streamStatus, streamHandler))

    case NodeSupervisorActor.Stop(ids) =>
      log.debug("Received stop message")
      execute(ids, () => {
        streamHandler match {
          case Some((handler)) =>
            handler.shutdown()
            this.streamHandler = None
          case _ => // Do nothing?!
        }
      })
      sender ! (id)

    case NodeSupervisorActor.List(ids, commandId) =>
      execute( ids, () =>
        sender ! Result(s"$id | ${checkStatus(streamStatus, streamHandler)}", commandId))

  }
}

