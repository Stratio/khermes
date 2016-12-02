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

package com.stratio.hermes.multinode

import akka.cluster.Cluster
import akka.cluster.MemberStatus.Up
import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpec}
import akka.testkit.ImplicitSender
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration._

object ClusterHermesMultiJvmSpec extends MultiNodeConfig {
  commonConfig(ConfigFactory.parseString(
    """
      |akka {
      | cluster.auto-join = off
      | actor.provider = akka.cluster.ClusterActorRefProvider
      | akka.testconductor.barrier-timeout = 4000
      |}
    """.stripMargin))

  val first = role("first")
  val second = role("second")
  val third = role("third")
}

class ClusterHermesMultiJvmNode1 extends ClusterHermesSpec
class ClusterHermesMultiJvmNode2 extends ClusterHermesSpec
class ClusterHermesMultiJvmNode3 extends ClusterHermesSpec

class ClusterHermesSpec extends MultiNodeSpec(ClusterHermesMultiJvmSpec)
  with GenericMultiNodeSpec with ImplicitSender {

  import ClusterHermesMultiJvmSpec._

  val GenericTimeout = 20 seconds
  val firstAddress = node(first).address
  val secondAddress = node(second).address
  val thirdAddress = node(third).address
  val cluster = Cluster(system)

  override def initialParticipants: Int = roles.size

  "An Hermes cluster" must {
    "add a new node and checks that it is joined and up" in {
      runOn(first) {
        cluster.join(firstAddress)
        awaitCond(Cluster(system).state.members.exists(member => member.address == firstAddress && member.status == Up))
      }
      enterBarrier("cluster-seed")
    }

    "add two nodes more and checks that them are joined and up" in  {
      runOn(second, third) {
        cluster join firstAddress
      }
      val expected = Set(firstAddress, secondAddress, thirdAddress)
      awaitCond(cluster.state.members.map(_.address) == expected)
      awaitCond(cluster.state.members.forall(_.status == Up))
      enterBarrier("cluster-nodes")
    }

    "shutdown the second node and checks that other nodes should detect it as unreachable" in within (GenericTimeout) {
      runOn(first) {
        Await.result(testConductor.shutdown(second), 5 second)
      }
      runOn(first, third) {
        awaitCond(cluster.state.unreachable.exists(_.address == secondAddress))
      }
    }
  }
}
