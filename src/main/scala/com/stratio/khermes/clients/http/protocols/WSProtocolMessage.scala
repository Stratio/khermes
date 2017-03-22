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

package com.stratio.khermes.clients.http.protocols

import com.stratio.khermes.clients.http.protocols.WsProtocolCommand.WsProtocolCommandValue

case class WSProtocolMessage(command: WsProtocolCommandValue, args: Seq[String])

case object WsProtocolCommand extends Enumeration {

  type WsProtocolCommandValue = Value
  val Ls = Value("ls")
  val Start = Value("start")
  val CreateTemplate = Value("create template")
  val ReadTemplate = Value("read template")
  val UpdateTemplate = Value("update template")
  val DeleteTemplate = Value("delete template")

  //scalastyle:off
  def parseTextBlock(block: String): WSProtocolMessage = {
    def parseLines(lines: Seq[String],
                   command: Option[String],
                   args: Seq[String],
                   isCommand: Boolean,
                   isArg: Boolean) : (Option[String], Seq[String]) = {
      lines.headOption match {
        case None => (command, args)
        case Some(line) if line.trim == "" => parseLines(lines.tail, command, args, isCommand, isArg)
        case Some(line) if line.toLowerCase == "[command]" => parseLines(lines.tail, command, args, true, false)
        case Some(line) if line.toLowerCase() == "[arg]" => parseLines(lines.tail, command, args, false, true)
        case Some(line) if isCommand => parseLines(lines.tail, Option(line), args, true, false)
        case Some(line) if isArg => parseLines(lines.tail, Option(line), args ++ Seq(line), false, true)
        case _ => parseLines(lines.tail, command, args, isCommand, isArg)
      }
    }

    val result = parseLines(block.split("\n"), None, Seq.empty, false, false)
    WSProtocolMessage(
      WsProtocolCommand.withName(result._1.getOrElse(
        throw new IllegalStateException("Imposible to parse command"))), result._2)

  }
  //scalastyle:on
}
