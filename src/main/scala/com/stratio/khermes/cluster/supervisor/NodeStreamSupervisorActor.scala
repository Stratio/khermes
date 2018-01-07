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
import java.util.concurrent.TimeUnit

import akka.{Done, NotUsed}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.{DistributedPubSub, DistributedPubSubMediator}
import akka.stream._
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import com.stratio.khermes.cluster.supervisor.StreamFileOperations.FileOperations
import com.stratio.khermes.cluster.supervisor.StreamKafkaOperations.KafkaOperations
import com.stratio.khermes.cluster.supervisor.StreamGenericOperations.{createSource, executeBatchIO}
import com.stratio.khermes.cluster.supervisor.NodeSupervisorActor.Result
import com.stratio.khermes.commons.config.AppConfig
import com.stratio.khermes.commons.exceptions.KhermesException
import com.stratio.khermes.helpers.faker.Faker
import com.stratio.khermes.helpers.twirl.{TwirlActorCache, TwirlHelper}
import com.stratio.khermes.metrics.KhermesMetrics
import com.stratio.khermes.persistence.file.FileClient
import com.stratio.khermes.persistence.kafka.KafkaClient
import com.typesafe.config.Config
import org.apache.kafka.clients.producer.RecordMetadata
import play.twirl.api.Txt

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.concurrent.duration._
import scala.io.Codec
import cats.data.State
import com.stratio.khermes.helpers.twirl.TwirlActorCache.{FakeEvent, NextEvent, Stop}
import org.reactivestreams.Publisher


object StreamKafkaOperations {
  case class KafkaOperations(topic: String, counter: Int, suspend: List[Future[RecordMetadata]])

  def executeStateKafka(k: KafkaClient[String])(implicit ec: ExecutionContext): (String) => (KafkaOperations) => KafkaOperations =
    (event: String) =>
      (st: KafkaOperations) => {
        st.copy(counter = st.counter + 1)
      }

  def kafkaFlow[A](config: AppConfig)
                  (implicit codec: Codec,
                   client: KafkaClient[String],
                   ec: ExecutionContext): Flow[List[String], State[KafkaOperations, List[String]], NotUsed] = {
    Flow[List[String]].map { events =>
      executeBatchIO(events)(executeStateKafka(client))
    }
  }
}

object StreamFileOperations {
  case class FileOperations(counter: Int)

  def executeStateFile(fc: FileClient[String])(implicit ec: ExecutionContext) : String => FileOperations => FileOperations =
    (_: String) =>
      (st: FileOperations) => {
        st.copy(counter = st.counter + 1)
    }

  def fileFlow(config: AppConfig)(implicit codec: Codec,
                                  client: FileClient[String], ec: ExecutionContext): Flow[List[String], State[FileOperations, List[String]], NotUsed] = {
    Flow[List[String]].map { events =>
      executeBatchIO(events)(executeStateFile(client))
    }
  }
}

object StreamGenericOperations {

  import State._

  final class EventPublisher(hc: AppConfig, twirlActorCache: ActorRef)(implicit val config: Config) extends ActorPublisher[String] with ActorLogging {

    private[this] var count = 0

    override def receive : Receive = {
      case FakeEvent(ev) =>
        if (isActive && !isCompleted && totalDemand > 0) {
          count = count + 1
          if(count <= hc.stopNumberOfEventsOption.get) {
            onNext(ev.replace("\n", ""))
          }
          else {
            twirlActorCache ! Stop
          }
        }
      case Request(cnt) =>
        log.debug("Received Request ({}) from Subscriber", cnt)
        for(_ <- 0 to cnt.toInt) twirlActorCache ! NextEvent
      case Cancel =>
        log.info("Cancel Message Received -- Stopping")
        context.stop(self)
      case _ => // Do nothing!!
    }
  }

  // Function to put a runtime value inside the Monad.
  def init[S, A](a: A): State[S, A] = State(s => (s, a))
  /**
    * Using Cats State Monad to perform each bulk of operations. It allows to abstract state operations via stateFunction,
    * State monad can be helpful in the future, i.e, dealing with temporal sequences or using and external
    * enriching services. In this case a transformer should be used. Cats Monad is heap safe.
    * @param in         Events collection
    * @param stateFunction Function with perform one single io operation. TODO: In thise case Task could be used
    * @tparam S
    * @return
    */
  def executeBatchIO[S](in: List[String])(stateFunction: String => S => S): State[S, List[String]] = {
    in.foldLeft(init[S, List[String]](List()))((state, event) => {
     for {
        xs <- state
        _ <-  modify[S]{ s => stateFunction(event)(s) }
        s1 <- get[S]
        _  <- set(s1)
      } yield event :: xs
    })
  }

  /**
    * It gives a single event
    * @param hc     User configuration
    * @param config App configuration. It is required for twirl execution
    * @return
    */
  private[supervisor] def getEvent(hc: AppConfig)(implicit config: Config): String = {
    val template =
      TwirlHelper.template[(Faker) => Txt](hc.templateContent, hc.templateName)
    val khermes = Faker(hc.khermesI18n, hc.strategy)
    template.static(khermes).toString()
  }

  /**
    * Twirl execution each time the Source tries to emit data
    * @param config App configuration. It is required for twirl execution
    * @return function which creates a "n" bunch of events
    */
  def getTwirlStringCollection(implicit config: Config): (AppConfig) => List[String] =
    (hc: AppConfig) => {
      hc.timeoutNumberOfEventsOption match {
        case (Some(nevents)) =>
          List.tabulate(nevents) { _ => getEvent(hc)}
        case None => List(getEvent(hc))
      }
    }

  /**
    * Create an Akka Stream Source. This source does not materialize, it executes indefinitely
    * @param config generator configuration
    * @return Akka Streams Source which emits a collection of events
    */
  def createSource(hc: AppConfig, ref: Publisher[String])(implicit config: Config): Source[List[String], NotUsed] = {
    val timeout = {
      hc.timeoutNumberOfEventsDurationOption match {
        case Some(duration) =>
          new FiniteDuration(duration.getSeconds, TimeUnit.SECONDS)
        case None => FiniteDuration(1, TimeUnit.SECONDS)
      }
    }
    /**
      * GroupWithin accumulates events depending on the timeOutNumberOfEvents property or emit if this limit has not been
      * reached in one second.
      */
    Source.fromPublisher(ref).take(hc.stopNumberOfEventsOption.get).groupedWithin(hc.timeoutNumberOfEventsOption.get, 1 seconds).map(_.toList)
  }

  /**
    * @param source Akka stream source to be started
    * @param am     Actor materializer to create the stream
    * @tparam A Type parameter to abstract over the IO model
    * @return Return handlerd to abort stream and a future if the stream is aborted or has crashed.
    */
  def startStream[A](source: Source[A, NotUsed])(
    implicit am: ActorMaterializer): (UniqueKillSwitch, Future[Done]) = {
    source
    .viaMat(KillSwitches.single)(Keep.right)
      .toMat(Sink.ignore)(Keep.both)
      .run
  }

  def checkStatus(streamStatus: Option[Done], streamHandler: Option[UniqueKillSwitch]) : String = {
    (streamStatus, streamHandler) match {
      case (_, None)    => "Stopped"
      case (None, _)    => "Running"
      case (Some(_), _) => "Exited"
    }
  }

  /**
    * Implicit class to extend Akka sources to provide this method to start "in line"
    */
  implicit class SourceOps[A](in: Source[A, NotUsed])(implicit am: ActorMaterializer) {
    def start: (UniqueKillSwitch, Future[Done]) =
      startStream(in)
  }

  implicit class commonStartStream[A](source: Source[A, NotUsed])(implicit am: ActorMaterializer) {
    def commonStart(hc: AppConfig) = source.take(hc.stopNumberOfEventsOption.get).start
  }
}

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
  import StreamGenericOperations._

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
      log.info(s"Received configuration ${hc}")
      twirlActorCacheProps  = Some(Props(new TwirlActorCache(hc)))
      twitlActorCacheRef    = Some(as.actorOf(twirlActorCacheProps.get))
      dataPublisherProps    = Some(Props(new EventPublisher(hc, twitlActorCacheRef.get)))
      dataPublisherRef      = Some(as.actorOf(dataPublisherProps.get))

      // Depending on the config it starts File or Kafka flow
      (hc.kafkaConfig, hc.fileConfig) match {
        case (Some(kafka), None) =>
          implicit val client = new KafkaClient[String](kafka)
          val (streamHandler, streamStatus) = SourceImplementations(hc, dataPublisherRef.get).createKafkaSource.commonStart(hc)
          this.streamHandler = Some(streamHandler)
          streamStatus.onComplete(_ => this.streamStatus = Some(Done) )

        case (None, Some(file)) =>
          implicit val client = new FileClient[String](hc.filePath)
          val (streamHandler, streamStatus) = SourceImplementations(hc, dataPublisherRef.get).createFileSource.commonStart(hc)
          this.streamHandler = Some(streamHandler)
          streamStatus.onComplete(_ => this.streamStatus = Some(Done) )

        case _ =>
          throw new KhermesException("Invalid Sink Data Configuration")
      }

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

