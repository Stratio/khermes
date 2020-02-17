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
import akka.stream.ActorMaterializer
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.{Sink, Source}
import akka.testkit.TestProbe
import com.stratio.khermes.cluster.BaseActorTest
import com.stratio.khermes.cluster.supervisor.stream.StreamGenericOperations.EventPublisher
import com.stratio.khermes.commons.config.AppConfigTest
import com.stratio.khermes.helpers.twirl.TwirlActorCache

import scala.concurrent.duration._

class PublisherBasedSourceTest extends BaseActorTest {

  implicit val materializer = ActorMaterializer()

  val template =
    """
      |@(faker: Faker)
      |@(faker.Boolean.random)
    """.stripMargin

  val khermesConfigWithNumberOfEvents =
    """
      |khermes {
      |  templates-path = "/some/test/path"
      |  template-name = "someTemplate"
      |  topic = "someTopic"
      |  i18n = "EN"
      |  timeout-rules {
      |    number-of-events: 5
      |  }
      |  stop-rules {
      |    number-of-events: 2
      |  }
      |}
    """.stripMargin

  val hc = AppConfigTest.testConfig.copy(template = template, khermesConfigContent = khermesConfigWithNumberOfEvents)

  val twirlActorCacheProps  = Props(new TwirlActorCache(hc))
  val twitlActorCacheRef    = system.actorOf(twirlActorCacheProps)

  val dataPublisherProps    = Props(new EventPublisher(hc, twitlActorCacheRef))
  val dataPublisherRef      = system.actorOf(dataPublisherProps)

  val sourceUnderTest = Source.fromPublisher(ActorPublisher(dataPublisherRef))

  val probe = TestProbe()
  val cancellable = sourceUnderTest.to(Sink.actorRef(probe.ref, "completed")).run()

  // Event 1
  probe.expectMsgPF(15 seconds) {
    case ev: String => {
       ev.trim() should (equal ("true") or equal ("false"))
    }
  }

  // Event 2
  probe.expectMsgPF(15 seconds) {
    case ev: String => {
      ev.trim() should (equal ("true") or equal ("false"))
    }
  }

  // Completion
  probe.expectMsgPF(15 seconds) {
    case ev => {
      ev shouldBe "completed"
    }
  }
}
