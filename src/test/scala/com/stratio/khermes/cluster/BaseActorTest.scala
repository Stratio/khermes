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
package com.stratio.khermes.cluster

import java.io.File

import akka.actor.ActorSystem
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.stratio.khermes.commons.implicits.AppImplicits
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import akka.util.Timeout

/**
 * Generic class used to test actors. All tests that uses akka should extend this class.
 */
abstract class BaseActorTest extends TestKit(ActorSystem("ActorTest", ConfigFactory.load()))
  with DefaultTimeout
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  lazy implicit val config: Config = AppImplicits.config
  lazy implicit val executionContext = AppImplicits.executionContext
  override implicit val timeout: Timeout = Timeout(10 seconds)

  val dir = new File("./tmp/khermes/templates")

  def deleteRecursively(file: File): Unit = {
    if (file.isDirectory) {
      file.listFiles.foreach(deleteRecursively)
    }
    if (file.exists && !file.delete) {
      throw new Exception(s"Unable to delete ${file.getAbsolutePath}")
    }
  }

  override def beforeAll(): Unit = {

    dir.mkdirs
  }

  override def afterAll {
    deleteRecursively(dir)
    system.terminate()
  }
}
