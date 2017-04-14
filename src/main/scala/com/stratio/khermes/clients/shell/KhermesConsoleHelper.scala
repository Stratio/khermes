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

import com.stratio.khermes.cluster.supervisor.KhermesClientActor
import com.stratio.khermes.commons.config.AppConfig
import com.stratio.khermes.commons.constants.AppConstants._
import com.stratio.khermes.commons.implicits.AppImplicits.configDAO
import jline.console.ConsoleReader
import jline.console.completer.{ArgumentCompleter, NullCompleter, StringsCompleter}

import scala.util.{Failure, Success, Try}

// TODO (Alvaro Nistal): This should be refactored.
case class KhermesConsoleHelper(client: KhermesClientActor) {

  lazy val reader = createDefaultReader()
  lazy val argsParser = new ArgsParser

  //scalastyle:off
  def parseLines: Unit = {
    reader.readLine.trim match {
      case value if value.startsWith("save") =>
        KhermesConsoleHelper.save(argsParser.parseArgsValues(value), argsParser.commandWord(value), this.setConfiguration())
        parseLines

      case value if value.startsWith("show") =>
        println(KhermesConsoleHelper.show(argsParser.parseArgsValues(value), argsParser.commandWord(value)))
        parseLines

      case value if value.startsWith("start") =>
        start(argsParser.parseArgsValues(value), argsParser.commandWord(value))
        parseLines

      case value if value.startsWith("stop") =>
        stop(argsParser.parseArgsValues(value), argsParser.commandWord(value))
        parseLines

      case "ls" =>
        ls
        parseLines

      case "help" =>
        println(KhermesConsoleHelper.help)
        parseLines

      case "clear" =>
        clearScreen
        parseLines

      case "exit" | "quit" | "bye" =>
        System.exit(0)

      case "" =>
        parseLines

      case _ =>
        println(KhermesConsoleHelper.printNotFoundCommand)
        parseLines
    }
    reader.setPrompt("khermes> ")
  }

  def setConfiguration(): Option[String] = {
    println("Press Control + D to finish")
    val parsedBlock = Option(parseBlock())
    reader.setPrompt("khermes> ")
    parsedBlock
  }

  def start(args: Map[String, String], commandWord: String): Unit = {
    Try {
      val khermes = args(args.filterKeys(x => x == "khermes").head._1)
      val kafka = args(args.filterKeys(x => x == "kafka").head._1)
      val template = args(args.filterKeys(x => x == "template").head._1)
      val avro = Try(args(args.filterKeys(x => x == "avro").head._1))
      val ids = Try(args(args.filterKeys(x => x == "ids").head._1).split(" ").toSeq)
      println(s"Ids ${ids.get}")
      println(s"kafka $KafkaConfigPath/$kafka")
      println(s"khermes $GeneratorConfigPath/$khermes")
      println(s"template $TwirlTemplatePath/$template")

      val khermesConfig = AppConfig(
        KhermesConsoleHelper.load(s"$GeneratorConfigPath/$khermes").get,
        KhermesConsoleHelper.load(s"$KafkaConfigPath/$kafka").get,
        KhermesConsoleHelper.load(s"$TwirlTemplatePath/$template").get,
        KhermesConsoleHelper.load(s"$AvroConfigPath/$avro")
      )
      client.start(khermesConfig, ids.get)
    }.getOrElse(println(s"Bad arguments."))
  }

  def stop(args: Map[String, String], commandWord: String): Unit = {
    val ids = Try(args(args.filterKeys(x => x == "ids").head._1).split(" ").toSeq)
    ids.get.map(id => println(s"Sending $id stop message"))
    client.stop(ids.get)
  }

  def ls: Unit = {
    println("Node Id                                Status")
    println("------------------------------------   ------")
    client.ls
    Thread.sleep(KhermesConsoleHelper.TimeoutWhenLsMessage)
  }

  def clearScreen: Unit = {
    reader.clearScreen()
  }

  //scalastyle:on

  def parseBlock(result: String = ""): String = {
    reader.setPrompt("")
    Option(reader.readLine()).map(currentLine => parseBlock(s"$result\n$currentLine")).getOrElse(result)
  }

  protected[this] def createDefaultReader(): ConsoleReader = {
    val reader = new ConsoleReader()
    reader.setHandleUserInterrupt(true)
    reader.setExpandEvents(false)
    val completer=new StringsCompleter("bye","clear","exit","help","ls","save","show","start","stop","quit")
    reader.addCompleter(completer)
    val argumentShowCompleter =new ArgumentCompleter(
      new StringsCompleter("show"),
      new StringsCompleter("--kafka","--template", "--khermes","--avro"),
      new NullCompleter())
    val argumentSaveCompleter =new ArgumentCompleter(
      new StringsCompleter("save"),
      new StringsCompleter("--kafka", "--template", "--khermes","--avro"),
      new NullCompleter())
    val argumentStartCompleter =new ArgumentCompleter(
      new StringsCompleter("start"),
      new StringsCompleter("--kafka","--template","--khermes","--avro","--ids"),
      new NullCompleter())
    val argumentStopCompleter =new ArgumentCompleter(new StringsCompleter("stop"), new StringsCompleter("--ids"), new NullCompleter())
    reader.addCompleter(argumentShowCompleter)
    reader.addCompleter(argumentSaveCompleter)
    reader.addCompleter(argumentStartCompleter)
    reader.addCompleter(argumentStopCompleter)
    reader.setPrompt("\u001B[33mkhermes> \u001B[0m")
    reader
  }
}

object KhermesConsoleHelper {

  val TimeoutWhenLsMessage = 200L

  def printNotFoundCommand: String = {
    "Command not found. Type help to list available commands."
  }

  def help: String = {
    s"""Khermes client provides the next commands to manage your Khermes cluster:
       |  Usage: COMMAND [args...]
       |
               |  Commands:
       |     start [command options] : Starts event generation in N nodes.
       |       --khermes    : Khermes configuration
       |       --kafka      : Kafka configuration
       |       --template   : Template to generate data
       |       --avro       : Avro configuration
       |       --ids        : Node id where start khermes
       |     stop [command options] : Stop event generation in N nodes.
       |       --ids        : Node id where start khermes
       |     ls                    : List the nodes with their current status
       |     save [command options] : Save your configuration in zookeeper
       |       --khermes    : Khermes configuration
       |       --kafka      : Kafka configuration
       |       --template   : Template to generate data
       |       --avro       : Avro configuration
       |     show [command options] : Show your configuration
       |       --khermes    : Khermes configuration
       |       --kafka      : Kafka configuration
       |       --template   : Template to generate data
       |       --avro       : Avro configuration
       |     clear                 : Clean the screen.
       |     help                  : Print this usage.
       |     exit | quit | bye     : Exit of Khermes Cli.   """.stripMargin
  }

  def show(args: Map[String, String], firstWord: String): String = {
    Try {
      val a = args.map {
        case ("khermes", _) => load(s"$GeneratorConfigPath/${args.head._2}").getOrElse(s"Khermes ${args.head._2} config is empty")
        case ("kafka", _) => load(s"$KafkaConfigPath/${args.head._2}").getOrElse(s"Khermes ${args.head._2} config is empty")
        case ("template", _) => load(s"$TwirlTemplatePath/${args.head._2}").getOrElse(s"Khermes ${args.head._2} config is empty")
        case ("avro", _) => load(s"$AvroConfigPath/${args.head._2}").getOrElse(s"Khermes ${args.head._2} config is empty")
        case value => s"Arg ${value._1} is not correct."
      }
      a.mkString
    }.getOrElse("Bad arguments.")
  }

  def save(args: Map[String, String], commandWord: String, config: Option[String]): Unit = {
    Try {
      args.foreach {
        case ("khermes", _) => configDAO.create(s"$GeneratorConfigPath/${args.head._2}", config.get)
        case ("kafka", _) => configDAO.create(s"$KafkaConfigPath/${args.head._2}", config.get)
        case ("template", _) => configDAO.create(s"$TwirlTemplatePath/${args.head._2}", config.get)
        case ("avro", _) => configDAO.create(s"$AvroConfigPath/${args.head._2}", config.get)
        case value => println(s"Arg ${value._1} is not correct.")
      }
    }.getOrElse(println("Bad arguments."))
  }

  def load(path: String): Option[String] = {
    Try(configDAO.read(path)) match {
      case Success(config) => Option(config)
      case Failure(_) => None
    }
  }
}