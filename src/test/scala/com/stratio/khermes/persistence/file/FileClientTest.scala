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

import java.io.File

import com.stratio.khermes.cluster.BaseActorTest
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class FileFlowTest extends BaseActorTest {

  "The FileClient " should {
    "Write n generated events in a file" in {
      val path = "/tmp/file.test"
      var result: List[String] = List()

      val client = new FileClient[String](path)
      client.send("Event1")

      for (line <- scala.io.Source.fromFile(path).getLines) {
        result = line :: result
      }

      result.length shouldBe 1
      result.head shouldBe "Event1"

      new File(path).delete()
    }
  }
}
