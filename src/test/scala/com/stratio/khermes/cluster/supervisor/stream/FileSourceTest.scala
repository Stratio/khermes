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

import java.io.File

import akka.NotUsed
import akka.actor.Props
import akka.stream.{ActorMaterializer, KillSwitches}
import akka.stream.scaladsl.{Keep, Sink, Source}
import com.stratio.khermes.cluster.BaseActorTest
import com.stratio.khermes.cluster.supervisor.{SourceImplementations, StreamFileOperations, StreamGenericOperations}
import com.stratio.khermes.commons.config.{AppConfig, AppConfigTest}
import com.stratio.khermes.helpers.twirl.TwirlActorCache
import com.stratio.khermes.persistence.file.FileClient
import com.stratio.khermes.utils.EmbeddedServersUtils
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.concurrent.duration._
import scala.concurrent.Await

@RunWith(classOf[JUnitRunner])
class FileSourceTest extends BaseActorTest with EmbeddedServersUtils {

  import StreamGenericOperations._
  import CommonsConfig._

  implicit val am = ActorMaterializer()

  def getResult(hc: AppConfig) : List[String] = {
    var result : List[String] = List()
    val twirlActorCacheProps = Props(new TwirlActorCache(hc))
    val twitlActorCacheRef = system.actorOf(twirlActorCacheProps)
    val dataPublisherProps = Props(new EventPublisher(hc, twitlActorCacheRef))
    val dataPublisherRef = system.actorOf(dataPublisherProps)

    implicit val client = new FileClient[String](hc.filePath)

    val source = SourceImplementations(hc, dataPublisherRef).createFileSource.commonStart(hc)
    Await.result(source._2, 100 seconds)

    for (line <- scala.io.Source.fromFile(hc.filePath).getLines) {
      result = line :: result
    }

    val s = new File(hc.filePath).delete()
    result
  }

  def getSource(hc: AppConfig): Source[StreamFileOperations.FileOperations, NotUsed] = {
    var result : List[String] = List()
    val twirlActorCacheProps = Props(new TwirlActorCache(hc))
    val twitlActorCacheRef = system.actorOf(twirlActorCacheProps)
    val dataPublisherProps = Props(new EventPublisher(hc, twitlActorCacheRef))
    val dataPublisherRef = system.actorOf(dataPublisherProps)

    implicit val am = ActorMaterializer()
    implicit val client = new FileClient[String](hc.filePath)

    SourceImplementations(hc, dataPublisherRef).createFileSource
  }

  "A File Source implementation" should {
    "Write n generated events to a file" in {

      val fileConfigContent =
        """
          |file {
          | path = "/tmp/file3.json"
          |}
        """.stripMargin

      val hc = AppConfigTest.testConfig.copy(kafkaConfigContent = None,
        localFileConfigContent = Some(fileConfigContent), khermesConfigContent = khermesConfigContent, template = templateContent)

      getResult(hc).length shouldBe 60
    }

    "Write n generated events to a file without timeout rules" in {

      val fileConfigContent =
        """
          |file {
          | path = "/tmp/file3.json"
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
          |  stop-rules {
          |    number-of-events: 5
          |  }
          |}
        """.stripMargin

      val hc = AppConfigTest.testConfig.copy(kafkaConfigContent = None,
        localFileConfigContent = Some(fileConfigContent), khermesConfigContent = khermesConfigContent, template = templateContent)

      getResult(hc).length shouldBe 5
    }

    "Write n generated events to a file with a timeout rule" in {
      val fileConfigContent =
        """
          |file {
          | path = "/tmp/file3.json"
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
          |  stop-rules {
          |    number-of-events: 5
          |  }
          |
          |  timeout-rules {
          |    number-of-events: 1
          |    duration: 5
          |  }
          |}
        """.stripMargin

      val hc = AppConfigTest.testConfig.copy(kafkaConfigContent = None,
        localFileConfigContent = Some(fileConfigContent), khermesConfigContent = khermesConfigContent, template = templateContent)

      getResult(hc).length shouldBe 5
    }

    "Write infinite generated events to a file without duration" in {

      val fileConfigContent =
        """
          |file {
          | path = "/tmp/file3.json"
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
          |    number-of-events: 1
          |  }
          |}
        """.stripMargin

      val hc = AppConfigTest.testConfig.copy(kafkaConfigContent = None,
        localFileConfigContent = Some(fileConfigContent), khermesConfigContent = khermesConfigContent, template = templateContent)

      // number is introduced from the 'outside'
      val pseudon = 43738

      val source = getSource(hc).take(pseudon).viaMat(KillSwitches.single)(Keep.right)
        .toMat(Sink.ignore)(Keep.both)
        .run

      Await.result(source._2, 100 seconds)

      var result: List[String] = List()

      for (line <- scala.io.Source.fromFile(hc.filePath).getLines) {
        result = line :: result
      }

      val s = new File(hc.filePath).delete()
      result.length shouldBe pseudon
    }
  }
}
