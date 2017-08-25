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

import com.stratio.khermes.clients.http.protocols.WsProtocolCommand
import org.json4s.ext.EnumNameSerializer
import org.json4s.{DefaultFormats, Formats}

/**
 * Defines how json is serialized / unserialized.
 * Remember that all custom serializations such as enums should be here.
 */
trait AppSerializer {

  implicit val json4sFormats: Formats = DefaultFormats + new EnumNameSerializer(WsProtocolCommand)
}

