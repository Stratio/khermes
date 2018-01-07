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
package com.stratio.khermes.persistence.file
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.IOException

import com.typesafe.scalalogging.LazyLogging

class FileClient[K](path: String)
    extends LazyLogging {

  var bw: BufferedWriter = null
  def send(message: String): Unit = {
    try {
      bw = new BufferedWriter(new FileWriter(s"$path", true))
      if(!message.isEmpty) {
        bw.write(message.trim() + "\n")
        bw.flush()
      }
    } catch {
      case ioe: IOException =>
        ioe.printStackTrace()
    } finally {
      if (bw != null)
        try bw.close()
        catch {
          case ioe2: IOException =>
        }
    }
  }
}







