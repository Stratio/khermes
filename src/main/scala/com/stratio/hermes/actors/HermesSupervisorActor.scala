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

import java.util.UUID

import akka.actor.{Actor, ActorLogging}
import akka.cluster.pubsub.{DistributedPubSub, DistributedPubSubMediator}
import com.stratio.hermes.helpers.{HermesConfig, TwirlHelper}
import com.stratio.hermes.kafka.KafkaClient
import com.stratio.hermes.utils.{Hermes, HermesLogging}
import com.typesafe.config.Config
import org.apache.avro.Schema.Parser
import play.twirl.api.Txt
import tech.allegro.schema.json2avro.converter.JsonAvroConverter

import scala.annotation.tailrec
import scala.util.Try

/**
 * Supervisor that will manage a thread that will generate data along the cluster.
 * @param config with all needed configuration.
 */
class HermesSupervisorActor(implicit config: Config) extends Actor with ActorLogging {

  import DistributedPubSubMediator.Subscribe
  val mediator = DistributedPubSub(context.system).mediator
  mediator ! Subscribe("content", self)

  var hermesExecutor: Option[HermesExecutor] = None
  val id = UUID.randomUUID.toString

  val hermes = Hermes(Try(config.getString("hermes.i18n")).toOption.getOrElse("EN"))

  override def receive: Receive = {
    case HermesSupervisorActor.Start(ids, hc) =>
      log.debug("Received start message")

      execute(ids, () => {
        if(hermesExecutor.isEmpty) {
          hermesExecutor = Option(new HermesExecutor(hc))
        } else {
          hermesExecutor.foreach(_.stopExecutor)
          hermesExecutor = Option(new HermesExecutor(hc))
        }
        hermesExecutor.foreach(_.start())
      })

    case HermesSupervisorActor.Stop(ids) =>
      log.debug("Received stop message")
      execute(ids, () => {
        hermesExecutor.foreach(_.stopExecutor)
        hermesExecutor = None
      })

    case HermesSupervisorActor.List(ids) =>
      log.debug("Received list message")
      execute(ids, () => {
        val status = hermesExecutor.map(_.status).getOrElse(false)
        sender ! s"$id | $status"
      })
  }

  protected[this] def isAMessageForMe(ids: Seq[String]): Boolean = ids.exists(x => id == x)

  protected[this] def execute(ids: Seq[String], callback: () => Unit): Unit =
    if(ids.nonEmpty && !isAMessageForMe(ids)) log.debug("Is not a message for me!") else callback()
}

/**
 * Thread that will generate data and it is controlled by the supervisor thanks to stopExecutor method.
 * @param hc with the Hermes' configuration.
 * @param config with general configuration.
 */
class HermesExecutor(hc: HermesConfig)(implicit config: Config) extends HermesExecutable with HermesLogging {

  val converter = new JsonAvroConverter()
  var running: Boolean = false

  /**
   * Starts the thread.
   * @param hc with all configuration needed to start the thread.
   */
  override def start(hc: HermesConfig): Unit = run()

  override def stopExecutor: Unit = running = false

  def status: Boolean = running

  //scalastyle:off
  override def run(): Unit = {
    running = true
    val kafkaClient = new KafkaClient[Object](hc.kafkaConfig)
    val template = TwirlHelper.template[(Hermes) => Txt](hc.templateContent, hc.templateName)
    val hermes = Hermes(hc.hermesI18n)

    val parserOption = hc.avroSchema.map(new Parser().parse(_))

    val timeoutNumberOfEventsOption = hc.timeoutNumberOfEventsOption
    val timeoutNumberOfEventsDurationOption = hc.timeoutNumberOfEventsDurationOption
    val stopNumberOfEventsOption = hc.stopNumberOfEventsOption

    /**
     * If you are defining the following example configuration:
     * timeout-rules {
     *   number-of-events: 1000
     *   duration: 2 seconds
     * }
     * Then when the node produces 1000 events, it will wait 2 seconds to start producing again.
     * @param numberOfEvents with the current number of events generated.
     */
    def performTimeout(numberOfEvents: Int): Unit =
      for {
        timeoutNumberOfEvents <- timeoutNumberOfEventsOption
        timeoutNumberOfEventsDuration <- timeoutNumberOfEventsDurationOption
        if(numberOfEvents % timeoutNumberOfEvents == 0)
      } yield ({
        log.debug(s"Sleeping executor thread $timeoutNumberOfEventsDuration")
        Thread.sleep(timeoutNumberOfEventsDuration.toMillis)
      })

    /**
     * Starts to generate events in a recursive way.
     * Note that this generation only will stop in two cases:
     *   1. The user sends an stop event to the supervisor actor; the supervisor change the state of running to false,
     *      stopping the execution.
     *   2. The user defines the following Hermes' configuration:
     *      stop-rules {
     *         number-of-events: 5000
     *      }
     *      In this case only it will generate 5000 events, then automatically the thread puts its state of running to
     *      false stopping the event generation.
     * @param numberOfEvents with the current number of events generated.
     */
    @tailrec
    def recursiveGeneration(numberOfEvents: Int): Unit =
      if(running) {
        log.debug(s"$numberOfEvents")
        val json = template.static(hermes).toString()
        parserOption match {
          case None =>
            kafkaClient.send(hc.topic, json)
            performTimeout(numberOfEvents)

          case Some(value) =>
            val record = converter.convertToGenericDataRecord(json.getBytes("UTF-8"), value)
            kafkaClient.send(hc.topic, record)
            performTimeout(numberOfEvents)
        }
        if(stopNumberOfEventsOption.filter(_ == numberOfEvents).map(_ => stopExecutor).isEmpty)
          recursiveGeneration(numberOfEvents + 1)
      }
    recursiveGeneration(0)
  }
}

trait HermesExecutable extends Thread {

  def start(hc: HermesConfig)
  def stopExecutor
  def status: Boolean
}

object HermesSupervisorActor {

  case class Start(workerIds: Seq[String], hermesConfig: HermesConfig)
  case class Stop(workerIds: Seq[String])
  case class List(workerIds: Seq[String])

  object WorkerStatus extends Enumeration {
    val Started, Stopped = Value
  }
}
