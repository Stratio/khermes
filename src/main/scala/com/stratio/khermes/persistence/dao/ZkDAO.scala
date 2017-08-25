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
package com.stratio.khermes.persistence.dao

import com.stratio.khermes.commons.constants.AppConstants
import com.stratio.khermes.commons.exceptions.KhermesException
import com.typesafe.scalalogging.LazyLogging
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry

import scala.util.{Failure, Success, Try}

class ZkDAO(connectionServer: Option[String] = None) extends BaseDAO[String] with LazyLogging {
  val config = com.stratio.khermes.commons.implicits.AppImplicits.config
  lazy val curatorFramework: CuratorFramework = buildCurator(connectionServer.getOrElse(connectionString))
  lazy val connectionString = Try(config.getString(AppConstants.ZookeeperConnection)).getOrElse(
    AppConstants.ZookeeperConnectionDefault)
  lazy val connectionTimeout = config.getInt(AppConstants.ZookeeperConnectionTimeout)
  lazy val sessionTimeout = config.getInt(AppConstants.ZookeeperSessionTimeout)
  lazy val retryAttempts = config.getInt(AppConstants.ZookeeperRetryAttempts)
  lazy val retryInterval = config.getInt(AppConstants.ZookeeperRetryInterval)

  override def create(path: String, config: String): Unit = Try(
    if (exists(path)) {
      update(path, config)
    }
    else {
      curatorFramework.create().creatingParentsIfNeeded().forPath(s"${AppConstants.ZookeeperParentPath}/$path")
      curatorFramework.setData().forPath(s"${AppConstants.ZookeeperParentPath}/$path", config.getBytes())
    }
  ) match {
    case Success(ids) => ids.toString
    case Failure(e) => throw new KhermesException(e.getMessage)
  }

  override def read(path: String): String = Try(
    new String(curatorFramework.getData.forPath(s"${AppConstants.ZookeeperParentPath}/$path"))
  )
  match {
    case Success(ids) => ids
    case Failure(e) => throw new KhermesException(e.getMessage)
  }

  override def exists(path: String): Boolean = {
    //scalastyle:off
    curatorFramework.checkExists().forPath(s"${AppConstants.ZookeeperParentPath}/$path") != null
    //scalastyle:on
  }

  override def delete(path: String): Unit = {
    curatorFramework.delete().deletingChildrenIfNeeded().forPath(s"/$path")
  }

  override def update(path: String, config: String): Unit = {
    curatorFramework.setData().forPath(s"${AppConstants.ZookeeperParentPath}/$path", config.getBytes())
  }

  override def list(path: String): String = {
    val children = curatorFramework.getChildren.forPath(s"${AppConstants.ZookeeperParentPath}/$path")
    import collection.JavaConverters._
    children.asScala.mkString("\n")
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
