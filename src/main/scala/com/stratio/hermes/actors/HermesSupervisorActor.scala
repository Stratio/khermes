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

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.Cluster
import com.stratio.hermes.actors.HermesSupervisorActor.{Start, WorkerStatus}
import com.stratio.hermes.helpers.{HermesConfig, TwirlHelper}
import com.stratio.hermes.utils.Hermes
import com.typesafe.config.Config
import org.apache.avro.Schema.Parser
import play.twirl.api.Txt
import tech.allegro.schema.json2avro.converter.JsonAvroConverter

class HermesSupervisorActor(implicit config: Config) extends Actor with ActorLogging {

  private lazy val workerExecutor = cluster.system.actorOf(Props(new HermesExecutorActor), "hermes-executor")

  val cluster = Cluster(context.system)
  val id = UUID.randomUUID.toString

  var status = WorkerStatus.Stopped

  override def receive: Receive = {
    case Start(ids, hermesConfig) =>
      execute(ids, () => {
        assertStopped
        workerExecutor ! HermesExecutorActor.Start(hermesConfig)
        status = WorkerStatus.Started
        sender() ! WorkerStatus.Started
      })
  }

  /////////////////////////// XXX Protected methods. ///////////////////////////

  protected[this] def isAMessageForMe(ids: Seq[String]): Boolean = ids.exists(x => id == x)

  protected[this] def assertStopped(): Unit =
    assert(status == WorkerStatus.Stopped, "The worker is started. Stop it before start.")

  protected[this] def execute(ids: Seq[String], callback: () => Unit): Unit =
    if(ids.nonEmpty && !isAMessageForMe(ids)) log.debug("Is not a message for me!") else callback()

}

private class HermesExecutorActor(implicit config: Config) extends Actor with ActorLogging {

  val converter = new JsonAvroConverter()

  override def receive: Receive = {
    case HermesExecutorActor.Start(hermesConfig) =>
      val kafkaClient = hermesConfig.kafkaClientInstance[Object]()
      val template = TwirlHelper.template[(Hermes) => Txt](hermesConfig.templateContent, hermesConfig.templateName)
      val hermes = Hermes(hermesConfig.hermesI18n)
      val parserOption = hermesConfig.avroSchema.map(new Parser().parse(_))

      def produce: Unit = {
        val json = template.static(hermes).toString()

        parserOption.map(value => {
          val record = converter.convertToGenericDataRecord(json.getBytes("UTF-8"), value)
          kafkaClient.send(hermesConfig.topic, record)
        }).getOrElse(kafkaClient.send(hermesConfig.topic, json))

        produce
      }
      produce
  }
}

object HermesSupervisorActor {

  case class Start(workerIds: Seq[String], hermesConfig: HermesConfig)

  object WorkerStatus extends Enumeration {
    val Started, Stopped = Value
  }
}

object HermesExecutorActor {

  case class Start(hc: HermesConfig)
}

