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

