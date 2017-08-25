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
package com.stratio.khermes.persistence.dao

import com.stratio.khermes.commons.exceptions.KhermesException
import com.stratio.khermes.utils.EmbeddedServersUtils
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class ZkDAOTest extends FlatSpec
  with Matchers
  with EmbeddedServersUtils{
  withEmbeddedZookeeper() { zookeeperServer =>
    val khermesConfigDAO = new ZkDAO(Option(zookeeperServer.getConnectString))

    "An KhermesConfig" should "be save in zookeeper path and could be loaded" in {
      khermesConfigDAO.create("testZkPath", "myConfig")
      val data = khermesConfigDAO.read("testZkPath")
      khermesConfigDAO.delete("stratio")
      data shouldBe "myConfig"
    }
    "An KhermesConfig" should "be updated when we save over an existing config" in {
      khermesConfigDAO.create("testZkPath", "myConfig")
      khermesConfigDAO.create("testZkPath", "myConfig2")
      val data = khermesConfigDAO.read("testZkPath")
      khermesConfigDAO.delete("stratio")
      data shouldBe "myConfig2"
    }
    "An KhermesConfig" should "have a way to obtain the list of configs" in {
      khermesConfigDAO.create("a/b/c","config1")
      khermesConfigDAO.create("a/b/d","config2")
      khermesConfigDAO.list("a/b") shouldBe "c\nd"
    }

    it should "raise an exception when it save or load a config in a path that does not exists" in {
      an[KhermesException] should be thrownBy khermesConfigDAO.read("")
      an[KhermesException] should be thrownBy khermesConfigDAO.create("", "config")
    }
  }
}