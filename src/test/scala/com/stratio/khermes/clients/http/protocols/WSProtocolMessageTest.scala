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
package com.stratio.khermes.clients.http.protocols

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class WSProtocolMessageTest extends FlatSpec with Matchers  {

  "A WSProtocolCommand" should "parse a configuration" in {
    val block =
      """
        |[command]
        |ls
        |[arg1]
        |one line content
        |[arg2]
        |multiline line 1 content
        |multiline line 2 content
      """.stripMargin

    val result = WsProtocolCommand.parseTextBlock(block)
    result should be(WSProtocolMessage(WsProtocolCommand.Ls, Map(
      "arg1" -> "one line content",
      "arg2" -> "multiline line 1 content\nmultiline line 2 content"
    )))
  }
}
