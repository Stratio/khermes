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
package com.stratio.khermes.commons.exceptions

/**
 * Generic exception used when Khermes fails.
 * TODO (Alvaro Nistal) Take a look if really it is necessary.
 * @param message to set.
 */
class KhermesException(message: String) extends Exception(message)
