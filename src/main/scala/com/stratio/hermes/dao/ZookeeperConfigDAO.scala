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
package com.stratio.hermes.dao

import com.stratio.hermes.constants.HermesConstants
import com.stratio.hermes.exceptions.HermesException
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry

import scala.util.{Failure, Success, Try}

class ZookeeperConfigDAO extends ConfigDAO[String] {
  val config = com.stratio.hermes.implicits.HermesImplicits.config
  lazy val curatorFramework: CuratorFramework = buildCurator
  lazy val connectionString = Try(config.getString(HermesConstants.ZookeeperConnection)).getOrElse(HermesConstants.ZookeeperConnectionDefault)
  lazy val connectionTimeout = config.getInt(HermesConstants.ZookeeperConnectionTimeout)
  lazy val sessionTimeout = config.getInt(HermesConstants.ZookeeperSessionTimeout)
  lazy val retryAttempts = config.getInt(HermesConstants.ZookeeperRetryAttempts)
  lazy val retryInterval = config.getInt(HermesConstants.ZookeeperRetryInterval)

  override def saveConfig(path: String, config: String): Unit = Try(
    if (existsConfig(path)) {
      updateConfig(path, config)
    }
    else {
      curatorFramework.create().creatingParentsIfNeeded().forPath(s"${HermesConstants.ZookeeperParentPath}/$path")
      curatorFramework.setData().forPath(s"${HermesConstants.ZookeeperParentPath}/$path", config.getBytes())
    }
  ) match {
    case Success(ids) => ids.toString
    case Failure(e) => throw new HermesException(e.getMessage)
  }

  override def loadConfig(path: String): String = Try(
    new String(curatorFramework.getData.forPath(s"${HermesConstants.ZookeeperParentPath}/$path"))
  )
  match {
    case Success(ids) => ids
    case Failure(e) => throw new HermesException(e.getMessage)
  }

  override def existsConfig(path: String): Boolean = {
    //scalastyle:off
    curatorFramework.checkExists().forPath(s"${HermesConstants.ZookeeperParentPath}/$path") != null
    //scalastyle:on
  }

  override def removeConfig(path: String): Unit = {
    curatorFramework.delete().deletingChildrenIfNeeded().forPath(s"/$path")
  }

  override def updateConfig(path: String, config: String): Unit = {
    curatorFramework.setData().forPath(s"${HermesConstants.ZookeeperParentPath}/$path", config.getBytes())
  }


  def buildCurator: CuratorFramework = {
    Try {
      val cf = CuratorFrameworkFactory.builder()
        .connectString(connectionString)
        .connectionTimeoutMs(connectionTimeout)
        .sessionTimeoutMs(sessionTimeout)
        .retryPolicy(new ExponentialBackoffRetry(retryAttempts, retryInterval))
        .build()
      cf.start()
      log.info(s"Zookeeper connection to ${connectionString} was STARTED.")
      cf
    }.getOrElse {
      log.error("Impossible to start Zookeeper connection")
      throw new HermesException("Impossible to start Zookeeper connection")
    }
  }

}
