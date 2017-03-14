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

package com.stratio.khermes.implicits

import java.net.NetworkInterface

import akka.actor.ActorSystem
import com.stratio.khermes.constants.KhermesConstants
import com.stratio.khermes.dao.ZookeeperConfigDAO
import com.typesafe.config.{Config, ConfigFactory, ConfigResolveOptions}

import scala.collection.JavaConversions._

/**
 * General implicits used in the application.
 */
object KhermesImplicits {

  lazy implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy implicit val config: Config = ConfigFactory
    .load(getClass.getClassLoader,
      ConfigResolveOptions.defaults.setAllowUnresolved(true))
    .resolve

  lazy implicit val system: ActorSystem = ActorSystem(KhermesConstants.AkkaClusterName, config)
  lazy implicit val khermesConfigDAO: ZookeeperConfigDAO = new ZookeeperConfigDAO

  /**
   * Gets the IP of the current host .
   * @return if the ip is running in Docker it will search in eth0 interface, if not it returns 127.0.0.1
   */
  def getHostIP(): String =
    NetworkInterface.getNetworkInterfaces()
      .find(_.getName.equals("eth0"))
      .flatMap(_.getInetAddresses
          .find(_.isSiteLocalAddress)
          .map(_.getHostAddress))
      .getOrElse("127.0.0.1")
}
