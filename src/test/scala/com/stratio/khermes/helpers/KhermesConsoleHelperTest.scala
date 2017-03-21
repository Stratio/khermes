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
class KhermesConsoleHelperTest extends FlatSpec
  with Matchers {
  val helpMessage =
    s"""Khermes client provide the next commands to manage your Khermes cluster:
       |  Usage: COMMAND [args...]
       |
               |  Commands:
       |     start [command options] : Starts event generation in N nodes.
       |       -h, --khermes    : Khermes configuration
       |       -k, --kafka      : Kafka configuration
       |       -t, --template   : Template to generate data
       |       -a, --avro       : Avro configuration
       |       -i, --ids        : Node id where start khermes
       |     stop [command options] : Stop event generation in N nodes.
       |       -i, --ids        : Node id where start khermes
       |     ls                    : List the nodes with their current status
       |     save [command options] : Save your configuration in zookeeper
       |       -h, --khermes    : Khermes configuration
       |       -k, --kafka      : Kafka configuration
       |       -t, --template   : Template to generate data
       |       -a, --avro       : Avro configuration
       |     show [command options] : Show your configuration
       |       -h, --khermes    : Khermes configuration
       |       -k, --kafka      : Kafka configuration
       |       -t, --template   : Template to generate data
       |       -a, --avro       : Avro configuration
       |     clear                 : Clean the screen.
       |     help                  : Print this usage.
       |     exit | quit | bye     : Exit of Khermes Cli.   """.stripMargin

  val khermesConsoleHelper = KhermesConsoleHelper

  it should "give a message a help message" in {
    khermesConsoleHelper.help shouldBe helpMessage
  }
//  it should "give a message a when the command is not valid" in {
//    khermesConsoleHelper.printNotFoundCommand shouldBe "Command not found. Type help to list available commands."
//  }
//  it should "give a config message when show a config that you save" in {
//    khermesConsoleHelper.save(Map("kafka" -> List("k1")),"save",Option("k1Config"))
//    khermesConsoleHelper.save(Map("khermes" -> List("h1")),"save",Option("h1Config"))
//    khermesConsoleHelper.save(Map("template" -> List("t1")),"save",Option("t1Config"))
//    khermesConsoleHelper.save(Map("avro" -> List("a1")),"save",Option("a1Config"))
//    khermesConsoleHelper.show(Map("kafka" -> List("k1")),"show") shouldBe "k1Config"
//    khermesConsoleHelper.show(Map("khermes" -> List("h1")),"show") shouldBe "h1Config"
//    khermesConsoleHelper.show(Map("template" -> List("t1")),"show") shouldBe "t1Config"
//    khermesConsoleHelper.show(Map("avro" -> List("a1")),"show") shouldBe "a1Config"
//  }
//  it should "load a None when do not find a config in a specific path" in {
//    khermesConsoleHelper.load("")shouldBe None
//  }
//  it should "give a message a when the params are not correct" in {
//    khermesConsoleHelper.show(Map("fail" -> List("a1")),"show") shouldBe "Arg fail is not correct."
//  }
}
