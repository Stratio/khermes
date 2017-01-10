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
import com.stratio.hermes.actors.HermesSupervisorActor.{Start, Status, Stop, WorkerStatus}
import com.stratio.hermes.helpers.{HermesConfigHelper, TwirlHelper}
import com.stratio.hermes.kafka.KafkaClient
import com.stratio.hermes.utils.Hermes
import com.typesafe.config.Config
import play.twirl.api.Txt
import tech.allegro.schema.json2avro.converter.JsonAvroConverter

class HermesSupervisorActor(implicit config: Config) extends Actor with ActorLogging {

  private lazy val workerExecutor = cluster.system.actorOf(Props(new HermesExecutorActor), "hermes-executor")

  val cluster = Cluster(context.system)
  //val id = UUID.randomUUID.toString
  val id = "chustas"
  var status = WorkerStatus.Stopped

  override def receive: Receive = {
    case Start(ids, configContent, templateContent) =>
      execute(ids, () => {
        assertStopped
        val hc1 = HermesConfigHelper(configContent, templateContent)
        val hc2 = HermesConfigHelper(configContent, templateContent)
        assertCorrectConfig(hc1)
        workerExecutor ! HermesExecutorActor.Start(hc1)
        status = WorkerStatus.Started
      })

    case Stop(ids) =>
      execute(ids, () => {
        cluster.system.stop(workerExecutor)
      })

    case Status(ids) =>
      execute(ids, () => {
        // TODO
      })
  }

  /////////////////////////// XXX Protected methods. ///////////////////////////

  protected[this] def isAMessageForMe(ids: Seq[String]): Boolean = ids.exists(x => id == x)

  protected[this] def assertStopped(): Unit =
    assert(status == WorkerStatus.Stopped, "The worker is started. Stop it before start.")

  protected[this] def assertCorrectConfig(hc: HermesConfigHelper): Unit = hc.assertConfig

  protected[this] def execute(ids: Seq[String], callback: () => Unit): Unit =
    if(ids.nonEmpty && !isAMessageForMe(ids)) log.debug("Is not a message for me!") else callback()

}

private class HermesExecutorActor(implicit config: Config) extends Actor with ActorLogging {

  val converter = new JsonAvroConverter()

  override def receive: Receive = {
    case HermesExecutorActor.Start(hc) =>
      //val kafkaClient = new KafkaClient[Object](hc.config)
      val kafkaClient = new KafkaClient[Object]()
      val template = TwirlHelper.template[(Hermes) => Txt](hc.templateContent, hc.templateName)
      val hermes = Hermes(hc.hermesI18n)

      log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> YA!!!!")


      def produce: Unit = {
        //val static  = template.static(hermes)
//        if (hc.isAvro) {
//          val parser = new Parser().parse(hc.avroSchema)
//          val record = converter.convertToGenericDataRecord(json.getBytes("UTF-8"), parser)
//          kafkaClient.send(hc.topic, record)
//        } else {
//          kafkaClient.send(hc.topic, json)
//        }
        //kafkaClient.send(hc.topic, static.toString())

        kafkaClient.send(hc.topic, s"${UUID.randomUUID().toString}")
//        Thread.sleep(1000l)
        produce
      }
      produce
  }
}

object HermesSupervisorActor {

  case class Start(workerIds: Seq[String], configContent: String, template: String)

  case class Stop(workerIds: Seq[String])

  case class Status(workerIds: Seq[String])

  object WorkerStatus extends Enumeration {
    val Started, Stopped = Value
  }
}

object HermesExecutorActor {

  case class Start(hc: HermesConfigHelper)
  case object Stop
}

