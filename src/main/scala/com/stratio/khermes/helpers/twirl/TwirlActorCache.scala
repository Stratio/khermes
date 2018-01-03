package com.stratio.khermes.helpers.twirl

import akka.actor.{Actor, PoisonPill}
import com.stratio.khermes.commons.config.AppConfig
import com.stratio.khermes.helpers.faker.Faker
import com.stratio.khermes.helpers.twirl.TwirlActorCache.{FakeEvent, NextEvent, Stop}
import com.typesafe.config.Config
import play.twirl.api.Txt

/**
  * Created by e049627 on 10/11/17.
  */
class TwirlActorCache(val hc: AppConfig)(implicit config: Config)  extends Actor {

  /**
    * Init one actor per template. This will be copiled once one actor is up and running
    * @return
    */
  lazy val template = TwirlHelper.template[(Faker) => Txt](hc.templateContent, hc.templateName)
  val khermes = Faker(hc.khermesI18n, hc.strategy)

  override def receive: Receive = {
    case NextEvent =>
      sender ! FakeEvent(template.static(khermes).toString())
    case Stop =>
      self ! PoisonPill
  }
}

object TwirlActorCache {
  case object NextEvent
  case object Stop
  case class FakeEvent(ev: String)
}

