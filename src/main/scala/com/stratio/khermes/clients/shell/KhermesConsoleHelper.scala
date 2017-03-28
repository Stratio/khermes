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
import com.stratio.khermes.commons.constants.AppConstants
import com.stratio.khermes.commons.implicits.AppImplicits.configDAO
import jline.console.ConsoleReader

import scala.util.{Failure, Success, Try}

// TODO (Alvaro Nistal): This should be refactored.
case class KhermesConsoleHelper(client: KhermesClientActor) {

  lazy val reader = createDefaultReader()

  parseLines(
    firstLoad(AppConstants.KhermesConfigNodePath),
    firstLoad(AppConstants.KafkaConfigNodePath),
    firstLoad(AppConstants.TemplateNodePath),
    firstLoad(AppConstants.AvroConfigNodePath)
  )

  //scalastyle:off
  def parseLines(khermesConfig: Option[String] = None,
                 kafkaConfig: Option[String] = None,
                 template: Option[String] = None,
                 avroConfig: Option[String] = None): Unit = {
    reader.readLine.trim match {
      case "set khermes" =>
        val config = setConfiguration(khermesConfig, kafkaConfig, template, avroConfig)
        configDAO.create(AppConstants.KhermesConfigNodePath, config.get)
        parseLines(config, kafkaConfig, template, avroConfig)

      case "set kafka" =>
        val config = setConfiguration(khermesConfig, kafkaConfig, template, avroConfig)
        configDAO.create(AppConstants.KafkaConfigNodePath, config.get)
        parseLines(khermesConfig, config, template, avroConfig)

      case "set template" =>
        val config = setConfiguration(khermesConfig, kafkaConfig, template, avroConfig)
        configDAO.create(AppConstants.TemplateNodePath, config.get)
        parseLines(khermesConfig, kafkaConfig, config, avroConfig)

      case "set avro" =>
        val config = setConfiguration(khermesConfig, kafkaConfig, template, avroConfig)
        configDAO.create(AppConstants.AvroConfigNodePath, config.get)
        parseLines(khermesConfig, kafkaConfig, template, config)

      case value if value.startsWith("start") =>
        startStop(value, "start", khermesConfig, kafkaConfig, template, avroConfig)
        parseLines(khermesConfig, kafkaConfig, template, avroConfig)

      case value if value.startsWith("stop") =>
        startStop(value, "stop", khermesConfig, kafkaConfig, template, avroConfig)
        parseLines(khermesConfig, kafkaConfig, template, avroConfig)

      case "ls" =>
        ls
        parseLines(khermesConfig, kafkaConfig, template, avroConfig)

      case "show config" =>
        showConfig(khermesConfig, kafkaConfig, template, avroConfig)
        parseLines(khermesConfig, kafkaConfig, template, avroConfig)

      case "help" =>
        help
        parseLines(khermesConfig, kafkaConfig, template, avroConfig)

      case "clear" =>
        clearScreen
        parseLines(khermesConfig, kafkaConfig, template, avroConfig)

      case "exit" | "quit" | "bye" =>
        System.exit(0)

      case "" =>
        parseLines(khermesConfig, kafkaConfig, template, avroConfig)

      case _ =>
        printNotFoundCommand
        parseLines(khermesConfig, kafkaConfig, template, avroConfig)
    }
  }

  def setConfiguration(khermesConfig: Option[String] = None,
                       kafkaConfig: Option[String] = None,
                       template: Option[String] = None,
                       avroConfig: Option[String] = None): Option[String] = {
    println("Press Control + D to finish")
    val parsedBlock = Option(parseBlock())
    reader.setPrompt("khermes> ")
    parsedBlock
  }

  def startStop(line: String,
                firstWord: String,
                khermesConfig: Option[String] = None,
                kafkaConfig: Option[String] = None,
                template: Option[String] = None,
                avroConfig: Option[String] = None): Unit = {
    val ids = line.replace(firstWord, "").trim.split(",").map(_.trim).filter("" != _)
    ids.map(id => println(s"Sending $id start message"))
    firstWord match {
      case "start" =>
        ids.map(id => println(s"Sending $id start message"))
        client.start(khermesConfig, kafkaConfig, template, avroConfig, ids)
      case "stop" =>
        ids.map(id => println(s"Sending $id stop message"))
        client.stop(ids)
    }
    reader.setPrompt("khermes> ")
  }

  def ls: Unit = {
    println("Node Id                                Status")
    println("------------------------------------   ------")
    client.ls
    Thread.sleep(KhermesConsoleHelper.TimeoutWhenLsMessage)
    reader.setPrompt("khermes> ")
  }

  def showConfig(khermesConfig: Option[String] = None,
                 kafkaConfig: Option[String] = None,
                 template: Option[String] = None,
                 avroConfig: Option[String] = None) = {
    println("Kafka configuration:")
    println(kafkaConfig.getOrElse("Kafka config is empty"))
    println("Khermes configuration:")
    println(khermesConfig.getOrElse("Khermes config is empty"))
    println("Template:")
    println(template.getOrElse("Template is empty"))
    println("Avro configuration:")
    println(avroConfig.getOrElse("Avro is empty"))
  }

  def help: Unit = {
    println("Khermes client provide the next commands to manage your Khermes cluster:")
    println("set khermes            Add your Khermes configuration.")
    println("set kafka              Add your Kafka configuration.")
    println("set template           Add your template.")
    println("set avro               Add your Avro configuration.")
    println("show config            Show all configurations.")
    println("ls                     List the nodes with their current status")
    println("start <Node Id>        Starts event generation in N nodes.")
    println("stop <Node Id>         Stop event generation in N nodes.")
    println("clear                  Clean the screen.")
    println("help                   Show this help.")
    println("exit | quit | bye      Exit of Khermes Cli.")
    reader.setPrompt("khermes> ")
  }

  def clearScreen: Unit = {
    reader.clearScreen()
  }

  def printNotFoundCommand: Unit = {
    println("Command not found. Type help to list available commands.")
  }

  def firstLoad(path: String): Option[String] = {
    Try(configDAO.read(path)) match {
      case Success(config) => print(s"${path.capitalize} configuration loaded successfully.")
        Option(config)
      case Failure(_) => println(s"${path.capitalize} config is empty")
        None
    }
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
    reader.setPrompt("khermes> ")
    reader
  }
}

object KhermesConsoleHelper {

  val TimeoutWhenLsMessage = 200L
}
