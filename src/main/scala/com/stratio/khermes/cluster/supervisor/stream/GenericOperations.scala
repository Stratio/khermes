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
package com.stratio.khermes.cluster.supervisor.stream

import java.util.concurrent.TimeUnit

import akka.{Done, NotUsed}
import akka.actor.{ActorLogging, ActorRef}
import akka.stream.{ActorMaterializer, KillSwitches, UniqueKillSwitch}
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}
import akka.stream.scaladsl.{Keep, Sink, Source}
import cats.data.State
import com.stratio.khermes.commons.config.AppConfig
import com.stratio.khermes.helpers.faker.Faker
import com.stratio.khermes.helpers.twirl.TwirlActorCache.{FakeEvent, NextEvent, Stop}
import com.stratio.khermes.helpers.twirl.TwirlHelper
import com.typesafe.config.Config
import org.reactivestreams.Publisher
import play.twirl.api.Txt

import scala.concurrent.Future
import scala.concurrent.duration._

object StreamGenericOperations {

  import State._

  /**
    * Publisher
    * @param hc generator configuration
    * @param twirlActorCache actor that holds the twirl compiled template
    * @param config application config
    */
  final class EventPublisher(hc: AppConfig, twirlActorCache: ActorRef)(implicit val config: Config) extends ActorPublisher[String] with ActorLogging {
    private[this] var count = 0
    override def receive : Receive = {
      case FakeEvent(ev) =>
        if (isActive && !isCompleted && totalDemand > 0) {
          count = count + 1
          if(hc.stopNumberOfEventsOption.isDefined) {
            if (count <= hc.stopNumberOfEventsOption.get) {
              //onNext(ev.replace("\n", ""))
              onNext(ev)
            }
            else {
              twirlActorCache ! Stop
            }
          }
          else {
            //onNext(ev.replace("\n", ""))
            onNext(ev)
          }
        }

      case Request(cnt) =>
        log.debug("Received Request ({}) from Subscriber", cnt)
        for(_ <- 0 to cnt.toInt) twirlActorCache ! NextEvent

      case Cancel =>
        log.info("Cancel Message Received -- Stopping")
        context.stop(self)
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
  def executeBatchState[S](in: List[String])(stateFunction: String => S => S): State[S, List[String]] = {
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
    * Depending whether there is a limit emitting events or not
    * @param ref actor publisher reference
    * @param limit number of events to emit
    * @return source instance
    */
  def createSourceLimited(ref: Publisher[String], limit: Option[Int]): Source[String, NotUsed] = {
    if(limit.isDefined) {
      Source.fromPublisher(ref).take(limit.get)
    }
    else {
      Source.fromPublisher(ref)
    }
  }

  /**
    * Depending on the configuration the source behaves different
    * @param hc generator config
    * @param ref actor publisher
    * @param config application config
    * @return source instance
    */
  def createSource(hc: AppConfig, ref: Publisher[String])(implicit config: Config): Source[List[String], NotUsed] = {
    (hc.timeoutNumberOfEventsDurationOption, hc.timeoutNumberOfEventsOption) match {
      // Emit a bulk of events each n seconds
      case (Some(time), Some(number)) =>
        val s = Source.tick(0 milliseconds, FiniteDuration(time.toNanos, TimeUnit.NANOSECONDS), ())
        createSourceLimited(ref, hc.stopNumberOfEventsOption).groupedWithin(number, 10 second)
          .zip(s)
          .map(_._1)
          .map(_.toList)
      // Emit one event each n seconds
      case (Some(time), None) =>
        val s = Source.tick(0 milliseconds, FiniteDuration(time.toNanos, TimeUnit.NANOSECONDS), ())
        createSourceLimited(ref, hc.stopNumberOfEventsOption).groupedWithin(1, 10 second)
          .zip(s)
          .map(_._1)
          .map(_.toList)
      // Events are emitted in sets of n
      case (None, Some(number)) =>
        createSourceLimited(ref, hc.stopNumberOfEventsOption).groupedWithin(number, 10 second)
          .map(_.toList)
      // Events are emitted one at time
      case _ =>
        createSourceLimited(ref, hc.stopNumberOfEventsOption).groupedWithin(1, 10 second)
          .map(_.toList)
    }
  }

  /**
    * @param source Akka stream source to be started
    * @param am Actor materializer to create the stream
    * @tparam A Type parameter to abstract over the IO model
    * @return Return handler to abort stream and a future if the stream is aborted or has crashed.
    */
  def startStream[A](source: Source[A, NotUsed])(
    implicit am: ActorMaterializer): (UniqueKillSwitch, Future[Done]) = {
    source
      .viaMat(KillSwitches.single)(Keep.right)
      .toMat(Sink.ignore)(Keep.both)
      .run
  }

  /**
    * Function to check the stream status
    * @param streamStatus If the stream is done
    * @param streamHandler Stream handler
    * @return
    */
  def checkStatus(streamStatus: Option[Done], streamHandler: Option[UniqueKillSwitch]) : String = {
    (streamStatus, streamHandler) match {
      case (_, None)    => "Stopped"
      case (None, _)    => "Running"
      case (Some(_), _) => "Exited"
    }
  }

  implicit class SourceOps[A](in: Source[A, NotUsed])(implicit am: ActorMaterializer) {
    def start: (UniqueKillSwitch, Future[Done]) =
      startStream(in)
  }

  implicit class commonStartStream[A](source: Source[A, NotUsed])(implicit am: ActorMaterializer) {
    def commonStart(hc: AppConfig) = source.start
  }
}
