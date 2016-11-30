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

package com.stratio.hermes.actors

import akka.actor._
import akka.cluster.ClusterEvent._
import akka.cluster._

/**
 * Listener used to retrieve events when a node in the cluster join, leave, etc.
 */
class ClusterListenerActor extends Actor with ActorLogging {
  val cluster = Cluster(context.system)

  override def preStart(): Unit =
    cluster.subscribe(
      subscriber = self,
      initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])

  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive: Receive = {
    case MemberUp(member) =>
      log.info(s"Member is [Up]: ${member.address}")
    case UnreachableMember(member) =>
      log.info(s"Member is [Unreachable]: ${member.address}")
    case MemberRemoved(member, previousStatus) =>
      log.info(s"Member is [Removed]: ${member.address} with previous status: $previousStatus")
    case MemberExited(member) =>
      log.info(s"Member is [Exited]: ${member.address}")
    case _: MemberEvent =>
      log.debug("Nothing to do")
  }
}
