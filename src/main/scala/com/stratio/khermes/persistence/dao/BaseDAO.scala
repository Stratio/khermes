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

trait BaseDAO[T] {

  def create(entity: T, data: T)

  def read(entity: T): T

  def update(entity: T, data: T)

  def delete(entity: T)

  def exists(entity: T): Boolean

  def list(entity: T): T
}
