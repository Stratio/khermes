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

package com.stratio.khermes.clients.http.flows

import akka.NotUsed
import akka.actor.ActorRef
import akka.http.scaladsl.model.ws.TextMessage.Strict
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.stratio.khermes.clients.http.protocols.{WSProtocolMessage, WsProtocolCommand}
import com.stratio.khermes.cluster.collector
import com.stratio.khermes.cluster.collector.CommandCollectorActor
import com.typesafe.scalalogging.LazyLogging
import org.json4s.Formats
import org.json4s.native.Serialization.{read, write}

import scala.util.Try

/**
 * All flows that will be used in the communication with the WebSocket should be implemented here.
 */
case object WSFlow extends LazyLogging {

  val source = Source.actorPublisher[CommandCollectorActor.Result](collector.CommandCollectorActor.props)

  /**
   * Defines what to do when the CommandCollector produces an output. In first instance only it needs to write
   * the result in the websocket.
   * @param formats needed for serialization and deserialization.
   * @return a flow needed to run the server.
   */
  def outputFlow()(implicit formats: Formats): Flow[Any, Strict, NotUsed] =
    Flow.fromSinkAndSource(Sink.ignore, source.map(x => {
      TextMessage.Strict(write(x))
    }))

  /**
   * Defines how to parse messages that have been sent from the websocket. It converts the message to a ProtocolMessage
   * and sends messages to the collector.
   * @param commandCollector is an actorref that will receive orders to execute in the cluster.
   * @param formats needed for serialization and deserialization.
   * @return a flow needed to run the server.
   */
  def inputFlow(commandCollector: ActorRef)(implicit formats: Formats): Flow[Message, Message, Any] =
    Flow[Any].mapConcat {
      case tm: TextMessage ⇒
        val message = tm.getStrictText
        Try(WsProtocolCommand.parseTextBlock(message)).toOption.map(commandCollector ! _)
          .getOrElse(logger.error(s"Imposible to serialize message: $message"))
        Nil
    }
}
