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

import akka.actor.Props
import com.stratio.khermes.cluster.BaseActorTest
import com.stratio.khermes.commons.config.AppConfigTest
import com.stratio.khermes.helpers.twirl.TwirlActorCache
import com.stratio.khermes.helpers.twirl.TwirlActorCache.{FakeEvent, NextEvent}

import scala.concurrent.duration._
class TwirlActorCacheTest extends BaseActorTest {

  val template =
    """
      |@(faker: Faker)
      |@(faker.Boolean.random)
    """.stripMargin

  val twirlActorCacheProps  = Props(new TwirlActorCache(AppConfigTest.testConfig.copy(template = template)))
  val twitlActorCacheRef    = system.actorOf(twirlActorCacheProps)

  "An TwirlCacheActor" should {
    "Provide events of a specific twirl template " in {
      twitlActorCacheRef ! NextEvent
      expectMsgPF(15 seconds) {
        case FakeEvent(event) =>
          event.replace("\n", "").trim() should (
            equal ("true") or equal ("false"))
      }
    }
  }
}
