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
package com.stratio.khermes.clients.shell

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class KhermesConsoleHelperTest extends FlatSpec
  with Matchers {

  val khermesConsoleHelper = KhermesConsoleHelper

  it should "give a map with arg as key and a list of the values" in {
    val line = "start --kafka k1 --template t1 --khermes k1 --ids 112312-32123213 3123-12343-453534-5345"
    khermesConsoleHelper.commandArgumentsAndValues(line) shouldBe Map("kafka" -> "k1", "template" -> "t1", "khermes" -> "k1", "ids" -> "112312-32123213 3123-12343-453534-5345")
  }

  it should "when an arg do not have value raise an exception" in {
    val line = "start --kafka --template t1 --khermes k1 --ids 1123-123212-3213 31231-2343453-5345345"
    khermesConsoleHelper.commandArgumentsAndValues(line) shouldBe Map("kafka" -> "", "template" -> "t1", "khermes" -> "k1", "ids" -> "1123-123212-3213 31231-2343453-5345345")
  }

  it should "give an empty map when there are not args" in {
    val line = "save"
    khermesConsoleHelper.commandArgumentsAndValues(line) shouldBe Map()
  }

  // TODO: Add commands for file processing
}
