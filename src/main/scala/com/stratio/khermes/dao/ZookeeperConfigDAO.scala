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
package com.stratio.khermes.dao

import com.stratio.khermes.constants.KhermesConstants
import com.stratio.khermes.exceptions.KhermesException
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry

import scala.util.{Failure, Success, Try}

class ZookeeperConfigDAO(connectionServer: Option[String] = None) extends ConfigDAO[String] {
  val config = com.stratio.khermes.implicits.KhermesImplicits.config
  lazy val curatorFramework: CuratorFramework = buildCurator(connectionServer.getOrElse(connectionString))
  lazy val connectionString = Try(config.getString(KhermesConstants.ZookeeperConnection)).getOrElse(
    KhermesConstants.ZookeeperConnectionDefault)
  lazy val connectionTimeout = config.getInt(KhermesConstants.ZookeeperConnectionTimeout)
  lazy val sessionTimeout = config.getInt(KhermesConstants.ZookeeperSessionTimeout)
  lazy val retryAttempts = config.getInt(KhermesConstants.ZookeeperRetryAttempts)
  lazy val retryInterval = config.getInt(KhermesConstants.ZookeeperRetryInterval)

  override def saveConfig(path: String, config: String): Unit = Try(
    if (existsConfig(path)) {
      updateConfig(path, config)
    }
    else {
      curatorFramework.create().creatingParentsIfNeeded().forPath(s"${KhermesConstants.ZookeeperParentPath}/$path")
      curatorFramework.setData().forPath(s"${KhermesConstants.ZookeeperParentPath}/$path", config.getBytes())
    }
  ) match {
    case Success(ids) => ids.toString
    case Failure(e) => throw new KhermesException(e.getMessage)
  }

  override def loadConfig(path: String): String = Try(
    new String(curatorFramework.getData.forPath(s"${KhermesConstants.ZookeeperParentPath}/$path"))
  )
  match {
    case Success(ids) => ids
    case Failure(e) => throw new KhermesException(e.getMessage)
  }

  override def existsConfig(path: String): Boolean = {
    //scalastyle:off
    curatorFramework.checkExists().forPath(s"${KhermesConstants.ZookeeperParentPath}/$path") != null
    //scalastyle:on
  }

  override def removeConfig(path: String): Unit = {
    curatorFramework.delete().deletingChildrenIfNeeded().forPath(s"/$path")
  }

  override def updateConfig(path: String, config: String): Unit = {
    curatorFramework.setData().forPath(s"${KhermesConstants.ZookeeperParentPath}/$path", config.getBytes())
  }


  def buildCurator(connectionServer: String): CuratorFramework = {
    Try {
      val cf = CuratorFrameworkFactory.builder()
        .connectString(connectionServer)
        .connectionTimeoutMs(connectionTimeout)
        .sessionTimeoutMs(sessionTimeout)
        .retryPolicy(new ExponentialBackoffRetry(retryAttempts, retryInterval))
        .build()
      cf.start()
      logger.info(s"Zookeeper connection to ${connectionServer} was STARTED.")
      cf
    }.getOrElse {
      logger.error("Impossible to start Zookeeper connection")
      throw new KhermesException("Impossible to start Zookeeper connection")
    }
  }

}
