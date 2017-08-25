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
package com.stratio.khermes

import com.stratio.khermes.cluster.BaseActorTest
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class KhermesTestIT extends BaseActorTest {

  "An KhermesRunner" should {
    "run main without any kind of error" in {
      Khermes.main(Array.empty)
    }

    "print a welcome message and start the akka system without errors" in {
      Khermes.welcome
    }

    "start a worker supervisor without errors" in {
      Khermes.workerSupervisor
    }
  }
}
