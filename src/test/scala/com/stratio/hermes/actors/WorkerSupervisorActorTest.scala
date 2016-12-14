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

package com.stratio.hermes.actors

import akka.testkit.TestActorRef
import com.stratio.hermes.actors.WorkerSupervisorActor._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class WorkerSupervisorActorTest extends HermesActorTest {

  val workerSupervisor = TestActorRef(new WorkerSupervisorActor)

  override def afterAll {
    shutdown()
  }

  "An WorkerSupervisorActor" should {
    "Start n threads of working kafka producers" in {
      within(5 seconds) {
        workerSupervisor ! Start
        expectMsg(StartOK)
        shutdown()
      }
    }
  }
}
