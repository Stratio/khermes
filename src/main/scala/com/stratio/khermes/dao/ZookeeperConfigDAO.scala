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

import com.stratio.khermes.constants.KHermesConstants
import com.stratio.khermes.exceptions.KHermesException
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry

import scala.util.{Failure, Success, Try}

class ZookeeperConfigDAO extends ConfigDAO[String] {
  val config = com.stratio.khermes.implicits.KHermesImplicits.config
  lazy val curatorFramework: CuratorFramework = buildCurator
  lazy val connectionString = Try(config.getString(KHermesConstants.ZookeeperConnection)).getOrElse(KHermesConstants.ZookeeperConnectionDefault)
  lazy val connectionTimeout = config.getInt(KHermesConstants.ZookeeperConnectionTimeout)
  lazy val sessionTimeout = config.getInt(KHermesConstants.ZookeeperSessionTimeout)
  lazy val retryAttempts = config.getInt(KHermesConstants.ZookeeperRetryAttempts)
  lazy val retryInterval = config.getInt(KHermesConstants.ZookeeperRetryInterval)

  override def saveConfig(path: String, config: String): Unit = Try(
    if (existsConfig(path)) {
      updateConfig(path, config)
    }
    else {
      curatorFramework.create().creatingParentsIfNeeded().forPath(s"${KHermesConstants.ZookeeperParentPath}/$path")
      curatorFramework.setData().forPath(s"${KHermesConstants.ZookeeperParentPath}/$path", config.getBytes())
    }
  ) match {
    case Success(ids) => ids.toString
    case Failure(e) => throw new KHermesException(e.getMessage)
  }

  override def loadConfig(path: String): String = Try(
    new String(curatorFramework.getData.forPath(s"${KHermesConstants.ZookeeperParentPath}/$path"))
  )
  match {
    case Success(ids) => ids
    case Failure(e) => throw new KHermesException(e.getMessage)
  }

  override def existsConfig(path: String): Boolean = {
    //scalastyle:off
    curatorFramework.checkExists().forPath(s"${KHermesConstants.ZookeeperParentPath}/$path") != null
    //scalastyle:on
  }

  override def removeConfig(path: String): Unit = {
    curatorFramework.delete().deletingChildrenIfNeeded().forPath(s"/$path")
  }

  override def updateConfig(path: String, config: String): Unit = {
    curatorFramework.setData().forPath(s"${KHermesConstants.ZookeeperParentPath}/$path", config.getBytes())
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
      throw new KHermesException("Impossible to start Zookeeper connection")
    }
  }

}
