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

import com.stratio.khermes.clients.http.protocols.WsProtocolCommand.WsProtocolCommandValue

case class WSProtocolMessage(command: WsProtocolCommandValue, args: Map[String, String])

case object WsProtocolCommand extends Enumeration {

  type WsProtocolCommandValue = Value

  val Ls = Value("ls")
  val Start = Value("start")
  val Stop = Value("stop")
  val CreateTwirlTemplate = Value("create twirl-template")
  val CreateKafkaConfig = Value("create kafka-config")
  val CreateGeneratorConfig = Value("create generator-config")
  val CreateAvroConfig = Value("create avro-config")
  val CreateFileConfig = Value("create file-config")


  val ShowTwirlTemplate = Value("show twirl-template")
  val ShowKafkaConfig = Value("show kafka-config")
  val ShowGeneratorConfig = Value("show generator-config")
  val ShowAvroConfig = Value("show avro-config")
  val ShowFileConfig = Value("show file-config")

  val ArgsName = "name"
  val ArgsContent = "content"
  val ArgsTwirlTemplate = "twirl-template"
  val ArgsKafkaConfig = "kafka-config"
  val ArgsGeneratorConfig = "generator-config"
  val ArgsAvroConfig = "avro-config"
  val ArgsNodeIds = "node-ids"
  val ArgsFileConfig = "file-config"

  //scalastyle:off
  def parseTextBlock(block: String): WSProtocolMessage = {
    def parseLines(lines: Seq[String],
                   commandOption: Option[String],
                   argOption: Option[String],
                   args: Map[String, String],
                   isCommand: Boolean,
                   isArg: Boolean) : (Option[String], Map[String, String]) = {
      lines.headOption match {
        case None => (commandOption, args)
        case Some(line) if line.trim == "" =>
          parseLines(lines.tail, commandOption, argOption, args, isCommand, isArg)
        case Some(line) if line.toLowerCase == "[command]" =>
          parseLines(lines.tail, commandOption, argOption, args, true, false)
        case Some(line) if line.toLowerCase().startsWith("[") =>
          parseLines(lines.tail, commandOption, Option(line.replace("[", "").replace("]", "")), args, false, true)
        case Some(line) if isCommand =>
          parseLines(lines.tail, Option(line), argOption, args, true, false)
        case Some(line) if isArg =>
          val arg = argOption.getOrElse(
            throw new IllegalStateException("Something was wrong taking the name of the arg (it is None)"))
          val value = (args.get(arg).toSeq ++ Seq(line)).mkString("\n")
          val newArgs = args + (arg -> value)
          parseLines(lines.tail, commandOption, argOption, newArgs, false, true)
        case _ =>
          parseLines(lines.tail, commandOption, argOption, args, isCommand, isArg)
      }
    }

    val result = parseLines(block.split("\n"), None, None, Map.empty, false, false)
    WSProtocolMessage(
      WsProtocolCommand.withName(result._1.getOrElse(
        throw new IllegalStateException("Impossible to parse command"))), result._2)
  }
  //scalastyle:on
}
