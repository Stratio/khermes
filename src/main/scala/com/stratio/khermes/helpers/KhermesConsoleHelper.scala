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

import com.stratio.khermes.actors.KhermesClientActor
import com.stratio.khermes.constants.KhermesConstants._
import com.stratio.khermes.implicits.KhermesImplicits.khermesConfigDAO
import jline.console.ConsoleReader
import jline.console.completer._

import scala.util.{Failure, Success, Try}

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

  def start(args: Map[String, List[String]], commandWord: String): Unit = {
    Try {
      val khermes = args(args.filterKeys(x => x == "kh" || x == "khermes").head._1).head
      val kafka = args(args.filterKeys(x => x == "ka" || x == "kafka").head._1).head
      val template = args(args.filterKeys(x => x == "t" || x == "template").head._1).head
      val avro = Try(args(args.filterKeys(x => x == "a" || x == "avro").head._1).head)
      val ids = Try(args(args.filterKeys(x => x == "i" || x == "ids").head._1))
      val khermesConfig = KhermesConfig(
        KhermesConsoleHelper.load(s"$KhermesConfigNodePath/$khermes").get,
        KhermesConsoleHelper.load(s"$KafkaConfigNodePath/$kafka").get,
        KhermesConsoleHelper.load(s"$TemplateNodePath/$template").get,
        KhermesConsoleHelper.load(s"$AvroConfigNodePath/$avro")
      )
      client.start(khermesConfig, ids.get)
    }.getOrElse(println("Bad arguments."))
  }

  def stop(args: Map[String, List[String]], commandWord: String): Unit = {
    val ids = Try(args(args.filterKeys(x => x == "i" || x == "ids").head._1))
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
      new StringsCompleter("-ka", "--kafka", "-t", "--template", "-kh","--khermes","-a","--avro"),
      new NullCompleter())
    val argumentSaveCompleter =new ArgumentCompleter(
      new StringsCompleter("save"),
      new StringsCompleter("-ka", "--kafka", "-t", "--template", "-kh","--khermes","-a","--avro"),
      new NullCompleter())
    val argumentStartCompleter =new ArgumentCompleter(
      new StringsCompleter("start"),
      new StringsCompleter("-ka", "--kafka", "-t", "--template", "-kh","--khermes","-a","--avro","-i","--ids"),
      new NullCompleter())
    val argumentStopCompleter =new ArgumentCompleter(new StringsCompleter("stop"), new StringsCompleter("-i","--ids"), new NullCompleter())
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
       |       -kh, --khermes    : Khermes configuration
       |       -ka, --kafka      : Kafka configuration
       |       -t, --template   : Template to generate data
       |       -a, --avro       : Avro configuration
       |       -i, --ids        : Node id where start khermes
       |     stop [command options] : Stop event generation in N nodes.
       |       -i, --ids        : Node id where start khermes
       |     ls                    : List the nodes with their current status
       |     save [command options] : Save your configuration in zookeeper
       |       -kh, --khermes    : Khermes configuration
       |       -ka, --kafka      : Kafka configuration
       |       -t, --template   : Template to generate data
       |       -a, --avro       : Avro configuration
       |     show [command options] : Show your configuration
       |       -kh, --khermes    : Khermes configuration
       |       -ka, --kafka      : Kafka configuration
       |       -t, --template   : Template to generate data
       |       -a, --avro       : Avro configuration
       |     clear                 : Clean the screen.
       |     help                  : Print this usage.
       |     exit | quit | bye     : Exit of Khermes Cli.   """.stripMargin
  }

  def show(args: Map[String, List[String]], firstWord: String): String = {
    Try {
      val a = args.map {
        case ("kh", _) | ("khermes", _) => load(s"$KhermesConfigNodePath/${args.head._2.head}").getOrElse(s"Khermes ${args.head._2.head} config is empty")
        case ("ka", _) | ("kafka", _) => load(s"$KafkaConfigNodePath/${args.head._2.head}").getOrElse(s"Khermes ${args.head._2.head} config is empty")
        case ("t", _) | ("template", _) => load(s"$TemplateNodePath/${args.head._2.head}").getOrElse(s"Khermes ${args.head._2.head} config is empty")
        case ("a", _) | ("avro", _) => load(s"$AvroConfigNodePath/${args.head._2.head}").getOrElse(s"Khermes ${args.head._2.head} config is empty")
        case value => s"Arg ${value._1} is not correct."
      }
      a.mkString
    }.getOrElse("Bad arguments.")
  }

  def save(args: Map[String, List[String]], commandWord: String, config: Option[String]): Unit = {
    Try {
      args.foreach {
        case ("kh", _) | ("khermes", _) => khermesConfigDAO.saveConfig(s"$KhermesConfigNodePath/${args.head._2.head}", config.get)
        case ("ka", _) | ("kafka", _) => khermesConfigDAO.saveConfig(s"$KafkaConfigNodePath/${args.head._2.head}", config.get)
        case ("t", _) | ("template", _) => khermesConfigDAO.saveConfig(s"$TemplateNodePath/${args.head._2.head}", config.get)
        case ("a", _) | ("avro", _) => khermesConfigDAO.saveConfig(s"$AvroConfigNodePath/${args.head._2.head}", config.get)
        case value => println(s"Arg ${value._1} is not correct.")
      }
    }.getOrElse(println("Bad arguments."))
  }

  def load(path: String): Option[String] = {
    Try(khermesConfigDAO.loadConfig(path)) match {
      case Success(config) => Option(config)
      case Failure(_) => None
    }
  }
}
