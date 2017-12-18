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

/**
  * Created by Emiliano Martinez on 30/10/17.
  */

package com.stratio.khermes.cluster.supervisor

import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.{Done, NotUsed}
import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, PoisonPill, Props}
import akka.cluster.pubsub.{DistributedPubSub, DistributedPubSubMediator}
import akka.stream._
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.util.Timeout
import com.stratio.khermes.cluster.supervisor.StreamFileOperations.FileOperations
import com.stratio.khermes.cluster.supervisor.StreamKafkaOperations.KafkaOperations
import com.stratio.khermes.cluster.supervisor.StreamGenericOperations.{executeBatchIO, startStream}
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

import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.util.{Success, Try}
import scala.concurrent.duration._
import scala.io.Codec
import cats.data.State
import com.stratio.khermes.helpers.twirl.TwirlActorCache.{FakeEvent, NextEvent}
import org.reactivestreams.Publisher

object StreamKafkaOperations {
  case class KafkaOperations(topic: String, counter: Int, suspend: List[Future[RecordMetadata]])

  def executeIOKafka(k: KafkaClient[String])(implicit ec: ExecutionContext) =
    (event: String) =>
      (st: KafkaOperations) => {
        st.copy(suspend = Future { k.send(st.topic, event).get } :: st.suspend, counter = st.counter + 1)
      }

  def kafkaFlow[A](config: AppConfig)(implicit codec: Codec, client: KafkaClient[String], ec: ExecutionContext) = {
    Flow[List[String]].map { events =>
      executeBatchIO(events)(executeIOKafka(client))
    }
  }
}

object StreamFileOperations {
  case class FileOperations(counter: Int)
  def executeIOFile(fc: FileClient[String])(implicit ec: ExecutionContext) =
    (event: String) =>
      (st: FileOperations) => {
        // Sending event
        fc.send(event)
        st.copy(counter = st.counter + 1)
    }
  def fileFlow(config: AppConfig)(implicit codec: Codec, client: FileClient[String], ec: ExecutionContext) = {
    Flow[List[String]].map { events =>
      executeBatchIO(events)(executeIOFile(client))
    }
  }
}

object StreamGenericOperations {

  import State._

  final class EventPublisher(hc: AppConfig, twirlActorCache: ActorRef)(implicit val config: Config) extends ActorPublisher[String] with ActorLogging {

    private[this] var count = 0

    def receive = {
      case FakeEvent(ev) =>
        if (isActive && !isCompleted && totalDemand > 0) {
          count = count + 1
          if(count < hc.stopNumberOfEventsOption.get) {
            //carry return explicitly dropped
            onNext(ev.replace("\n", ""))
          }
          else
            context.stop(self)
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
  //scalastyle:off
  /**
    * Using Cats State Monad to perform each bulk of operations. It allows to abstract of IO operation via ioFunction,
    * State monad can be helpful in the future, i.e, dealing with temporal sequences or using and external
    * enriching services. Cats Monad is heap safe.
    * @param in         Events collection
    * @param ioFunction Function with perform one single io operation. TODO: In thise case Task could be used
    * @tparam S
    * @return
    */
  def executeBatchIO[S](in: List[String])(ioFunction: String => S => S) = {
    in.foldLeft(init[S, String](""))((state, event) => {
      val result = for {
        _ <-  modify[S]{s => ioFunction(event)(s)}
        s1 <- get[S]
        s2 <- set(s1)
      } yield event
      result
    })
  } //scalastyle:on

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
    Source.fromPublisher(ref).groupedWithin(hc.timeoutNumberOfEventsOption.get, 5 seconds).map(_.toList)
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

  /**
    * Implicit class to extend Akka sources to provide this method to start "in line"
    */
  implicit class SourceOps[A, B](in: Source[(A, B), NotUsed])(implicit am: ActorMaterializer) {
    def start: (UniqueKillSwitch, Future[Done]) =
      startStream(in)
  }

  implicit class commonStartStream[A, B](source: Source[(A, B), NotUsed])(implicit am: ActorMaterializer) {
    def commonStart(hc: AppConfig) = source.take(hc.stopNumberOfEventsOption.get).start
  }
}

final class NodeStreamSupervisorActor(implicit config: Config) extends Actor with ActorLogging with KhermesMetrics {

  import DistributedPubSubMediator.Subscribe
  import StreamGenericOperations._

  val id = UUID.randomUUID.toString

  private[this] def isAMessageForMe(ids: Seq[String]): Boolean = ids.contains(id)
  private[this] def execute(ids: Seq[String], callback: () => Unit): Unit =
    if (ids.nonEmpty && !isAMessageForMe(ids)) log.debug("Is not a message for me!") else callback()

  private[this] def checkStatus : String = {
    (streamStatus, streamHandler) match {
      case (_, None)    => "Stopped"
      case (None, _)    => "Running"
      case (Some(_), _) => "Exited"
    }
  }

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

  override def receive: Receive = {

    // TODO: Create one type class with IOperations. This actor will need one instance of this typeclass
    case NodeSupervisorActor.Start(ids, hc) =>
      log.info(s"Received configuration ${hc}")

      twirlActorCacheProps  = Some(Props(new TwirlActorCache(hc)))
      twitlActorCacheRef    = Some(as.actorOf(twirlActorCacheProps.get))
      dataPublisherProps    = Some(Props(new EventPublisher(hc, twitlActorCacheRef.get)))
      dataPublisherRef      = Some(as.actorOf(dataPublisherProps.get))

      // Depending on the config it starts File or Kafka flow
      (hc.kafkaConfig, hc.fileConfig) match {
        // Kafka config is included
        case (Some(kafka), None) =>
          log.info(s"Using Kafka implementation")
          implicit val client = new KafkaClient[String](kafka)
          log.info(s"Starting stream . . .")

          val (streamHandler, streamStatus) = createSource(hc, ActorPublisher(dataPublisherRef.get))
            .via(StreamKafkaOperations.kafkaFlow(hc))
            .map(_.run(KafkaOperations(hc.topic, 0, Nil)).value)
            .commonStart(hc)

          this.streamHandler = Some(streamHandler)

          streamStatus andThen { case _ => {
            this.streamStatus = Some(Done)
          } }

        // Local file config is included
        case (None, Some(file)) =>
          implicit val client = new FileClient[String](hc.filePath)
          val (streamHandler, streamStatus) = createSource(hc, ActorPublisher(dataPublisherRef.get))
            .via(StreamFileOperations.fileFlow(hc))
            .map(_.run(FileOperations(0)).value)
            .commonStart(hc)

          this.streamHandler = Some(streamHandler)

          streamStatus andThen { case _ => {
            this.streamStatus = Some(Done)
          } }

        case _ =>
          throw new KhermesException("Invalid Sink Data Configuration")
      }

      sender ! (id, checkStatus)

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
        sender ! Result(s"${id} | ${checkStatus}", commandId))
  }
}

