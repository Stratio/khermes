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

package com.stratio.hermes.utils

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class HermesTest extends FlatSpec with Matchers {

  "A Hermes" should "generates random firstNames and lastNames with EN and ES locales" in {
    val hermesEN = Hermes()
    hermesEN.Name.nameModel.firstNames should contain (hermesEN.Name.firstName)
    hermesEN.Name.nameModel.lastNames should contain (hermesEN.Name.lastName)

    val hermesES = Hermes("ES")
    hermesES.Name.nameModel.firstNames should contain (hermesES.Name.firstName)
    hermesES.Name.nameModel.lastNames should contain (hermesES.Name.lastName)
  }

  it should "generate valid names: firstName lastName with EN and ES locales" in {
    val hermesEN = Hermes()
    hermesEN.Name.name should fullyMatch regex """[a-zA-Z]+ [a-zA-Z]+"""

    val hermesES = Hermes("ES")
    hermesES.Name.name should fullyMatch regex """[a-zA-Z]+ [a-zA-Z]+"""
  }

  it should "generate valid midle names: firstName firstName with EN and ES locales" in {
    val hermesEN = Hermes()
    hermesEN.Name.midleName should fullyMatch regex """[a-zA-Z]+ [a-zA-Z]+"""

    val hermesES = Hermes("ES")
    hermesES.Name.midleName should fullyMatch regex """[a-zA-Z]+ [a-zA-Z]+"""
  }
}
