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

import com.stratio.khermes.utils.KHermesLogging

/**
 * Base DAO to manage configuration resources in KHermes.
 * @tparam T
 */
trait ConfigDAO[T] extends KHermesLogging {
  /**
   * Save the configuration in your persistence system.
   * @param entity Path where you are going to store your configuration.
   * @param data   Configuration data
   */
  def saveConfig(entity: T, data: T)

  /**
   * Load configuration from a path.
   * @param entity Path where your configuration is stored.
   * @return Configuration data.
   */
  def loadConfig(entity: T): T

  /**
   * Check if exists any configuration stored in a path.
   * @param entity Path where your configuration is stored
   * @return True if it is already saved any configuration in that path.
   */
  def existsConfig(entity: T): Boolean

  /**
   * Remove data store in a path.
   * @param entity Path where your data is stored.
   */
  def removeConfig(entity: T)

  /**
   * Update configuration stored in a path.
   * @param entity Path where your data is stored.
   * @param data Your new configuration.
   */
  def updateConfig(entity: T, data: T)

}
