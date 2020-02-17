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
package com.stratio.khermes.cluster.supervisor.stream.fileimpl

import akka.NotUsed
import akka.stream.scaladsl.Flow
import cats.data.State
import com.stratio.khermes.cluster.supervisor.stream.StreamGenericOperations.executeBatchState
import com.stratio.khermes.commons.config.AppConfig
import com.stratio.khermes.persistence.file.FileClient

import scala.concurrent.ExecutionContext
import scala.io.Codec

object StreamFileOperations {
  case class FileOperations(counter: Int)

  def executeStateFile(fc: FileClient[String])(implicit ec: ExecutionContext) : String => FileOperations => FileOperations =
    (_: String) =>
      (st: FileOperations) => {
        st.copy(counter = st.counter + 1)
      }

  def fileFlow(config: AppConfig)(implicit codec: Codec,
                                  client: FileClient[String], ec: ExecutionContext): Flow[List[String], State[FileOperations, List[String]], NotUsed] = {
    Flow[List[String]].map { events =>
      executeBatchState(events)(executeStateFile(client))
    }
  }
}
