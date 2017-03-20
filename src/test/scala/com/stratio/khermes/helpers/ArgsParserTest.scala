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
package com.stratio.khermes.helpers

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class ArgsParserTest extends FlatSpec with Matchers{
    it should "give a message with configuration empties" in {
      val line = "start --kafka k1 -t t1 --khermes k1 --ids 11231232123213 3123123434535345345"
      val argsParser = new ArgsParser
      argsParser.parse(line) shouldBe Map("kafka" -> List("k1"),"t"->List("t1"),"khermes"->List("k1"),"ids"->List("11231232123213","3123123434535345345"))
    }
}
