package com.stratio.khermes.cluster.supervisor

import akka.actor.Props
import com.stratio.khermes.cluster.BaseActorTest
import com.stratio.khermes.cluster.supervisor.NodeSupervisorActor.{Result, Start, WorkerStatus}
import com.stratio.khermes.commons.config.AppConfig

import scala.concurrent.duration._
/**
  * Created by e049627 on 7/11/17.
  */
class NodeStreamSupervisorActorTest extends BaseActorTest {

  val nodeSupervisor = system.actorOf(Props(new NodeStreamSupervisorActor()), "node-supervisor")

  val fileConfigContent =
    """
      |file {
      | path = "/tmp/file.json"
      |}
    """.stripMargin

  val khermesConfigContent =
    """
      |khermes {
      |  templates-path = "/tmp/khermes/templates"
      |  topic = "topic"
      |  template-name = "TemplateName"
      |  i18n = "ES"
      |}
    """.stripMargin

  val templateContent =
    """
      |@import com.stratio.khermes.helpers.faker.Faker
      |
      |@(faker: Faker)
      |{
      |  "name" : "@(faker.Name.firstName)"
      |}
    """.stripMargin

  "An NodeStreamSupervisorActor" should {
    "Start a Akka Stream fo event generation" in {
      within(10 seconds) {
        nodeSupervisor ! Start(Seq.empty, AppConfig(khermesConfigContent, None, Some(fileConfigContent), templateContent))
        expectMsgPF(10 seconds) {
          case (id, status) =>
            status shouldBe "Running"
        }
      }
    }
  }

  "An NodeStreamSupervisorActor" should {
    "Stop the Stream when an 'Stop' message is received " in {
      within(10 seconds) {
        var streamId: Seq[String] = Nil

        nodeSupervisor ! Start(Seq.empty, AppConfig(khermesConfigContent, None, Some(fileConfigContent), templateContent))
        expectMsgPF(10 seconds) {
          case (id: String, status) =>
            status shouldBe "Running"
            streamId = Seq(id)
        }

        nodeSupervisor ! NodeSupervisorActor.Stop(streamId)
        expectMsgPF(10 seconds) {
          case (id: String) =>
            streamId = Seq(id)
        }

        nodeSupervisor ! NodeSupervisorActor.List(streamId, "")
        expectMsgPF(10 seconds) {
          case (Result(status, _)) =>
            status.split(" | ")(2) shouldBe "Exited"
        }
      }
    }
  }
}
