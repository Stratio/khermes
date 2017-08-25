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

import akka.actor.{Actor, ActorLogging}
import akka.cluster.pubsub.{DistributedPubSub, DistributedPubSubMediator}
import com.stratio.khermes.cluster.supervisor.NodeSupervisorActor.Result
import com.stratio.khermes.commons.config.AppConfig
import com.stratio.khermes.helpers.faker.Faker
import com.stratio.khermes.helpers.twirl.TwirlHelper
import com.stratio.khermes.metrics.KhermesMetrics
import com.stratio.khermes.persistence.kafka.KafkaClient
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.apache.avro.Schema.Parser
import play.twirl.api.Txt
import tech.allegro.schema.json2avro.converter.JsonAvroConverter

import scala.annotation.tailrec
import scala.util.Try

/**
 * Supervisor that will manage a thread that will generate data along the cluster.
 * @param config with all needed configuration.
 */
class NodeSupervisorActor(implicit config: Config) extends Actor with ActorLogging with KhermesMetrics {

  import DistributedPubSubMediator.Subscribe

  val mediator = DistributedPubSub(context.system).mediator
  mediator ! Subscribe("content", self)

  var khermesExecutor: Option[NodeExecutorThread] = None
  val id = UUID.randomUUID.toString

  val khermes = Faker(Try(config.getString("khermes.i18n")).toOption.getOrElse("EN"),
    Try(config.getString("khermes.strategy")).toOption)

  override def receive: Receive = {
    case NodeSupervisorActor.Start(ids, hc) =>
      log.debug("Received start message")

      execute(ids, () => {
        if (khermesExecutor.isEmpty) {
          khermesExecutor = Option(new NodeExecutorThread(hc))
        } else {
          khermesExecutor.foreach(_.stopExecutor)
          khermesExecutor = Option(new NodeExecutorThread(hc))
        }
        khermesExecutor.foreach(_.start())
      })

    case NodeSupervisorActor.Stop(ids) =>
      log.debug("Received stop message")
      execute(ids, () => {
        khermesExecutor.foreach(_.stopExecutor)
        khermesExecutor = None
      })

    case NodeSupervisorActor.List(ids, commandId) =>
      log.debug("Received list message")
      execute(ids, () => {
        val status = khermesExecutor.map(_.status).getOrElse(false)
        sender ! Result(s"$id | $status", commandId)
        //context.system.eventStream.publish(s"$id | $status")
      })
  }

  protected[this] def isAMessageForMe(ids: Seq[String]): Boolean = ids.exists(x => id == x)

  protected[this] def execute(ids: Seq[String], callback: () => Unit): Unit =
    if (ids.nonEmpty && !isAMessageForMe(ids)) log.debug("Is not a message for me!") else callback()
}

/**
 * Thread that will generate data and it is controlled by the supervisor thanks to stopExecutor method.
 * @param hc     with the Hermes' configuration.
 * @param config with general configuration.
 */
class NodeExecutorThread(hc: AppConfig)(implicit config: Config) extends NodeExecutable
  with LazyLogging with KhermesMetrics {

  val converter = new JsonAvroConverter()
  var running: Boolean = false
  val messageSentCounterMetric = getAvailableCounterMetrics("khermes-messages-count")
  val messageSentMeterMetric = getAvailableMeterMetrics("khermes-messages-meter")
  /**
   * Starts the thread.
   * @param hc with all configuration needed to start the thread.
   */
  override def start(hc: AppConfig): Unit = run()

  override def stopExecutor: Unit = running = false

  def status: Boolean = running

  //scalastyle:off
  override def run(): Unit = {
    running = true
    val kafkaClient = new KafkaClient[Object](hc.kafkaConfig)
    val template = TwirlHelper.template[(Faker) => Txt](hc.templateContent, hc.templateName)
    val khermes = Faker(hc.khermesI18n, hc.strategy)

    val parserOption = hc.avroSchema.map(new Parser().parse(_))

    val timeoutNumberOfEventsOption = hc.timeoutNumberOfEventsOption
    val timeoutNumberOfEventsDurationOption = hc.timeoutNumberOfEventsDurationOption
    val stopNumberOfEventsOption = hc.stopNumberOfEventsOption

    /**
     * If you are defining the following example configuration:
     * timeout-rules {
     * number-of-events: 1000
     * duration: 2 seconds
     * }
     * Then when the node produces 1000 events, it will wait 2 seconds to start producing again.
     * @param numberOfEvents with the current number of events generated.
     */
    def performTimeout(numberOfEvents: Int): Unit =
      for {
        timeoutNumberOfEvents <- timeoutNumberOfEventsOption
        timeoutNumberOfEventsDuration <- timeoutNumberOfEventsDurationOption
        if (numberOfEvents % timeoutNumberOfEvents == 0)
      } yield ({
        logger.debug(s"Sleeping executor thread $timeoutNumberOfEventsDuration")
        Thread.sleep(timeoutNumberOfEventsDuration.toMillis)
      })


    /**
     * Starts to generate events in a recursive way.
     * Note that this generation only will stop in two cases:
     *   1. The user sends an stop event to the supervisor actor; the supervisor change the state of running to false,
     * stopping the execution.
     *   2. The user defines the following Hermes' configuration:
     * stop-rules {
     * number-of-events: 5000
     * }
     * In this case only it will generate 5000 events, then automatically the thread puts its state of running to
     * false stopping the event generation.
     * @param numberOfEvents with the current number of events generated.
     */
    @tailrec
    def recursiveGeneration(numberOfEvents: Int): Unit =
    if (running) {
      logger.debug(s"$numberOfEvents")
      val json = template.static(khermes).toString()
      parserOption match {
        case None => {
          kafkaClient.send(hc.topic, json)
          increaseCounterMetric(messageSentCounterMetric)
          markMeterMetric(messageSentMeterMetric)
          performTimeout(numberOfEvents)
        }

        case Some(value) => {
          val record = converter.convertToGenericDataRecord(json.getBytes("UTF-8"), value)
          kafkaClient.send(hc.topic, record)
          increaseCounterMetric(messageSentCounterMetric)
          markMeterMetric(messageSentMeterMetric)
          performTimeout(numberOfEvents)
        }
      }
      if (stopNumberOfEventsOption.filter(_ == numberOfEvents).map(_ => stopExecutor).isEmpty)
        recursiveGeneration(numberOfEvents + 1)
    }

    recursiveGeneration(0)
  }
}

trait NodeExecutable extends Thread {

  def start(hc: AppConfig)

  def stopExecutor

  def status: Boolean
}

object NodeSupervisorActor {

  case class Start(workerIds: Seq[String], khermesConfig: AppConfig)

  case class Stop(workerIds: Seq[String])

  case class List(workerIds: Seq[String], commandId: String)

  case class Result(value: String, commandId: String)

  object WorkerStatus extends Enumeration {
    val Started, Stopped = Value
  }

}
