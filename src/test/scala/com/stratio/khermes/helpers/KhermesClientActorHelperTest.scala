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
package com.stratio.khermes.helpers

import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class KhermesClientActorHelperTest extends FlatSpec
  with Matchers{
  it should "give a message with configuration empties" in {
    val khermes = KhermesClientActorHelper
    khermes.messageFeedback(None,None,None) shouldBe "Error: To start nodes is necessary to set template and kafka and khermes configuration."
  }
  it should "give a message with kafka and template configuration" in {
    val khermes = KhermesClientActorHelper
    khermes.messageFeedback(Option("khermes"),None,None) shouldBe "Error: To start nodes is necessary to set template and kafka configuration."
  }
  it should "give a message with template configuration" in {
    val khermes = KhermesClientActorHelper
    khermes.messageFeedback(Option("khermes"),Option("kafka"),None) shouldBe "Error: To start nodes is necessary to set template configuration."
  }
  it should "do not give a message because the configurations are OK" in {
    val khermes = KhermesClientActorHelper
    khermes.messageFeedback(Option("khermes"),Option("kafka"), Option("template")) shouldBe "Your configuration is OK"
}

}
