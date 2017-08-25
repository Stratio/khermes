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
package com.stratio.khermes.commons.implicits

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.stratio.khermes.commons.constants.AppConstants
import com.stratio.khermes.persistence.dao.ZkDAO
import com.typesafe.config.{Config, ConfigFactory, ConfigResolveOptions}
import com.typesafe.scalalogging.LazyLogging

/**
 * General implicits used in the application.
 */
object AppImplicits extends AppSerializer with LazyLogging {

  lazy implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy implicit val config: Config = ConfigFactory
    .load(getClass.getClassLoader,
      ConfigResolveOptions.defaults.setAllowUnresolved(true))
    .resolve

  lazy implicit val system: ActorSystem = ActorSystem(AppConstants.AkkaClusterName, config)
  lazy implicit val configDAO: ZkDAO = new ZkDAO
  lazy implicit val materializer = ActorMaterializer()
}
