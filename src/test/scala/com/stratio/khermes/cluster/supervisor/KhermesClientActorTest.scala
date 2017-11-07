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
package com.stratio.khermes.cluster.supervisor

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class KhermesClientActorTest extends FlatSpec
  with Matchers{
  // TODO: change. This check should accept either kafka or file
  /*
  it should "give a message with configuration empties" in {
    val khermes = KhermesClientActor
    khermes.messageFeedback(None,None,None) shouldBe "Error: To start nodes is necessary to set template and kafka and khermes configuration."
  }*/

  // TODO: change this
  /*
  it should "give a message with kafka and template configuration" in {
    val khermes = KhermesClientActor
    khermes.messageFeedback(Option("khermes"),None,None) shouldBe "Error: To start nodes is necessary to set template and kafka configuration."
  }*/
  it should "give a message with template configuration" in {
    val khermes = KhermesClientActor
    khermes.messageFeedback(Option("khermes"),Option("kafka"),None) shouldBe "Error: To start nodes is necessary to set template configuration."
  }
  it should "do not give a message because the configurations are OK" in {
    val khermes = KhermesClientActor
    khermes.messageFeedback(Option("khermes"),Option("kafka"), Option("template")) shouldBe "Your configuration is OK"
}

}
