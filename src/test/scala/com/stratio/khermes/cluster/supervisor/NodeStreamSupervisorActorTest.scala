package com.stratio.khermes.cluster.supervisor

import akka.actor.Props
import com.stratio.khermes.cluster.BaseActorTest
import com.stratio.khermes.cluster.supervisor.NodeSupervisorActor.{Result, Start, WorkerStatus}
import com.stratio.khermes.commons.config.AppConfig

import scala.concurrent.Await
import scala.concurrent.duration._
/**
  * Created by e049627 on 7/11/17.
  */
class NodeStreamSupervisorActorTest extends BaseActorTest {

  val nodeSupervisor = system.actorOf(Props(new NodeStreamSupervisorActor()), "node-supervisor")

  val fileConfigContent =
    """
      |file {
      | path = "/tmp/file2.json"
      |}
    """.stripMargin

  val khermesConfigContent =
    """
      |khermes {
      |  templates-path = "/tmp/khermes/templates"
      |  topic = "khermes"
      |  template-name = "khermestemplate"
      |  i18n = "EN"
      |
      |  timeout-rules {
      |    number-of-events: 10
      |    duration: 5 seconds
      |  }
      |
      |  stop-rules {
      |    number-of-events: 5000
      |  }
      |}
    """.stripMargin

  val templateContent =
    """|@import com.stratio.khermes.helpers.faker.generators._
       |@import scala.util.Random
       |@import com.stratio.khermes.helpers.faker.Faker
       |@import com.stratio.khermes.helpers.faker.generators.Positive
       |@import org.joda.time.DateTime
       |@(faker: Faker)
       |@defining(faker, List(CategoryFormat("MASTERCARD", "0.5"),CategoryFormat("VISA", "0.5")),List(CategoryFormat("MOVISTAR", "0.5"),CategoryFormat("IUSACELL", "0.5"))){ case (f,s,s2) =>
       |@f.Name.fullName,@f.Categoric.runNext(s),@f.Number.numberInRange(10000,50000),@f.Geo.geolocation.city,@f.Number.numberInRange(1000,10000),@f.Categoric.runNext(s2),@f.Number.numberInRange(1,5000),@f.Datetime.datetime(new DateTime("2000-01-01"), new DateTime("2016-01-01"), Option("yyyy-MM-dd")) }
    """.stripMargin

  "An NodeStreamSupervisorActor" should {
    "Start a Akka Stream fo event generation" in {
      within(100 seconds) {
        nodeSupervisor ! Start(Seq.empty, AppConfig(khermesConfigContent, None, Some(fileConfigContent), templateContent))
        expectMsgPF(100 seconds) {
          case (id, status) =>
            status shouldBe "Running"
        }
      }
    }
  }

  "An NodeStreamSupervisorActor" should {
    "Stop the Stream when an 'Stop' message is received " in {
      within(50 seconds) {
        var streamId: Seq[String] = Nil

        nodeSupervisor ! Start(Seq.empty, AppConfig(khermesConfigContent, None, Some(fileConfigContent), templateContent))
        expectMsgPF(10 seconds) {
          case (id: String, status) =>
            status shouldBe "Running"
            streamId = Seq(id)
        }

        Thread.sleep(20000)

        nodeSupervisor ! NodeSupervisorActor.Stop(streamId)
        expectMsgPF(10 seconds) {
          case (id: String) =>
            streamId = Seq(id)
        }

        nodeSupervisor ! NodeSupervisorActor.List(streamId, "")
        expectMsgPF(10 seconds) {
          case (Result(status, _)) =>
            status.split(" | ")(2) shouldBe "Stopped"
        }
      }
    }
  }
}
