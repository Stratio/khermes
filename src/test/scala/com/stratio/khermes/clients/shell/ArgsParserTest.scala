/*
 * Copyright (C) 2016 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.khermes.clients.shell

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class ArgsParserTest extends FlatSpec with Matchers {
  it should "give a map with arg as key and a list of the values" in {
    val line = "start --kafka k1 --template t1 --khermes k1 --ids 112312-32123213 3123-12343-453534-5345"
    val argsParser = new ArgsParser
    argsParser.commandWord(line) shouldBe "start"
    argsParser.parseArgsValues(line) shouldBe Map("kafka" -> "k1", "template" -> "t1", "khermes" -> "k1", "ids" -> "112312-32123213 3123-12343-453534-5345")
  }

  it should "when an arg do not have value that return a empty list" in {
    val line = "start --kafka --template t1 --khermes k1 --ids 1123-123212-3213 31231-2343453-5345345"
    val argsParser = new ArgsParser
    argsParser.parseArgsValues(line) shouldBe Map("kafka" -> "", "template" -> "t1", "khermes" -> "k1", "ids" -> "1123-123212-3213 31231-2343453-5345345")
  }

  it should "give an empty map when there are not args" in {
    val line = "save"
    val argsParser = new ArgsParser
    argsParser.parseArgsValues(line) shouldBe Map()
  }


}