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

import scala.util.Try

// TODO (Alvaro Nistal): This should be refactored.
case class KhermesConsoleHelper(client: KhermesClientActor) {

  lazy val reader = createDefaultReader()

  //scalastyle:off
  def parseLines: Unit = {
    reader.readLine.trim match {
      case value if value.startsWith("save") =>
        KhermesConsoleHelper.save(KhermesConsoleHelper.commandArgumentsAndValues(value), this.setConfiguration())
        parseLines

      case value if value.startsWith("show") =>
        println(KhermesConsoleHelper.show(KhermesConsoleHelper.commandArgumentsAndValues(value)))
        parseLines

      case value if value.startsWith("start") =>
        start(KhermesConsoleHelper.commandArgumentsAndValues(value))
        parseLines

      case value if value.startsWith("stop") =>
        stop(KhermesConsoleHelper.commandArgumentsAndValues(value))
        parseLines

      case "ls" =>
        ls()
        parseLines

      case "help" =>
        println(HelpMessage)
        parseLines

      case "clear" =>
        clearScreen
        parseLines

      case "exit" | "quit" | "bye" =>
        System.exit(0)

      case "" =>
        parseLines

      case _ =>
        println(CommandNotFoundMessage)
        parseLines
    }
    reader.setPrompt("\u001B[33mkhermes> \u001B[0m")
  }

  def setConfiguration(): Option[String] = {
    println("Press Control + D to finish")
    val parsedBlock = Option(parseBlock())
    reader.setPrompt("\u001B[33mkhermes> \u001B[0m")
    parsedBlock
  }

  def start(args: Map[String, String]): Unit = {
    Try {
      val khermes = args("generator-config")
      val kafka = args("kafka-config")
      val template = args("twirl-template")
      val avro = Try(args("avro-template"))
      val ids = Try(args("ids").split(" ").toSeq)
      val khermesConfig = AppConfig(
        configDAO.read(s"$GeneratorConfigPath/$khermes"),
        configDAO.read(s"$KafkaConfigPath/$kafka"),
        configDAO.read(s"$TwirlTemplatePath/$template"),
        Try(configDAO.read(s"$AvroConfigPath/$avro")).toOption
      )
      client.start(khermesConfig, ids.getOrElse(Seq.empty))
    }.getOrElse(println(s"Bad arguments."))
  }

  def stop(args: Map[String, String]): Unit = {
    val ids = Try(args("ids").split(" ").toSeq)
    ids.toOption.map(id => println(s"Sending $id stop message"))
    client.stop(ids.getOrElse(Seq.empty))
  }

  def ls(): Unit = {
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
    reader.setHandleUserInterrupt(false)
    reader.setExpandEvents(false)
    val completer = new StringsCompleter("bye", "clear", "exit", "help", "ls", "save", "show", "start", "stop", "quit")
    reader.addCompleter(completer)
    val argumentShowCompleter = new ArgumentCompleter(
      new StringsCompleter("show"),
      new StringsCompleter("--kafka-config", "--twirl-template", "--generator-config", "--avro-template"),
      new NullCompleter())
    val argumentSaveCompleter = new ArgumentCompleter(
      new StringsCompleter("save"),
      new StringsCompleter("--kafka-config", "--twirl-template", "--generator-config", "--avro-template"),
      new NullCompleter())
    val argumentStartCompleter = new ArgumentCompleter(
      new StringsCompleter("start"),
      new StringsCompleter("--kafka-config", "--twirl-template", "--generator-config", "--avro-template", "--ids"),
      new NullCompleter())
    val argumentStopCompleter = new ArgumentCompleter(new StringsCompleter("stop"),
      new StringsCompleter("--ids"),
      new NullCompleter())
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

  def show(args: Map[String, String]): String = {
    val a = args.map {
      case ("generator-config", value) => Try(configDAO.read(s"$GeneratorConfigPath/$value"))
        .getOrElse(s"Khermes $value config is empty")
      case ("kafka-config", value) => Try(configDAO.read(s"$KafkaConfigPath/$value"))
        .getOrElse(s"Khermes $value config is empty")
      case ("twirl-template", value) => Try(configDAO.read(s"$TwirlTemplatePath/$value"))
        .getOrElse(s"Khermes $value config is empty")
      case ("avro-template", value) => Try(configDAO.read(s"$AvroConfigPath/$value"))
        .getOrElse(s"Khermes $value config is empty")
      case value => s"Arg ${value._1} is not correct."
    }
    a.mkString
  }

  def save(args: Map[String, String], config: Option[String]): Unit = {
    Try {
      require(args.nonEmpty)
      args.foreach {
        case ("generator-config", value) => configDAO.create(s"$GeneratorConfigPath/$value", config.get)
        case ("kafka-config", value) => configDAO.create(s"$KafkaConfigPath/$value", config.get)
        case ("twirl-template", value) => configDAO.create(s"$TwirlTemplatePath/$value", config.get)
        case ("avro-template", value) => configDAO.create(s"$AvroConfigPath/$value", config.get)
        case value => println(s"Arg ${value._1} is not correct.")
      }
    }.getOrElse(println("You must provide arguments."))
  }

  /**
    * Help to obtain arguments and their values.
    *
    * @param line
    * @return Map with arguments and their values.
    */
  def commandArgumentsAndValues(line: String): Map[String, String] = {
    val splitWords = line.split("--").filter(_ != "")
    val filterFirstWord = splitWords.drop(1).map(_.trim)
    filterFirstWord
      .map(_.split(" ", 2))
      .map(c => c.head -> c.tail.mkString)
      .toMap
  }
}
