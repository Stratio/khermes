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
package com.stratio.khermes.helpers.twirl

import akka.actor.{Actor, PoisonPill}
import com.stratio.khermes.commons.config.AppConfig
import com.stratio.khermes.helpers.faker.Faker
import com.stratio.khermes.helpers.twirl.TwirlActorCache.{FakeEvent, NextEvent, Stop}
import com.typesafe.config.Config
import play.twirl.api.Txt

class TwirlActorCache(val hc: AppConfig)(implicit config: Config)  extends Actor {

  /**
    * Init one actor per template. This will be compiled once one actor is up and running
    * @return
    */
  lazy val template = TwirlHelper.template[(Faker) => Txt](hc.templateContent, hc.templateName)
  val khermes = Faker(hc.khermesI18n, hc.strategy)

  override def receive: Receive = {
    case NextEvent =>
      sender ! FakeEvent(template.static(khermes).toString())
    case Stop =>
      // Kill when source does not need more events
      self ! PoisonPill
  }
}

object TwirlActorCache {
  case object NextEvent
  case object Stop
  case class FakeEvent(ev: String)
}

