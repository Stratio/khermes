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

package com.stratio.khermes.helpers.faker.generators

import java.util.NoSuchElementException

import com.stratio.khermes.helpers.faker.Faker
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class NameGeneratorTest extends FlatSpec
  with Matchers {


  "A Khermes" should "generates random firstNames and lastNames with EN and ES locales" in {
    val khermesEN = Faker()
    khermesEN.Name.firstNames(khermesEN.Name.nameModel) should contain(khermesEN.Name.firstName)
    khermesEN.Name.lastNames(khermesEN.Name.nameModel) should contain(khermesEN.Name.lastName)

    val khermesES = Faker("ES")
    khermesES.Name.firstNames(khermesEN.Name.nameModel) should contain(khermesES.Name.firstName)
    khermesES.Name.lastNames(khermesEN.Name.nameModel) should contain(khermesES.Name.lastName)
  }

  it should "generate valid names: firstName lastName with EN and ES locales" in {
    val khermesEN = Faker()
    val fullNameEN = khermesEN.Name.fullName
    fullNameEN should fullyMatch regex """[a-zA-Z]+ [a-zA-Z]+"""
    khermesEN.Name.firstNames(khermesEN.Name.nameModel) should contain(fullNameEN.split(" ")(0))
    khermesEN.Name.lastNames(khermesEN.Name.nameModel) should contain(fullNameEN.split(" ")(1))

    val khermesES = Faker("ES")
    val fullNameES = khermesES.Name.fullName
    fullNameES should fullyMatch regex """[a-zA-Z]+ [a-zA-Z]+"""
    khermesES.Name.firstNames(khermesEN.Name.nameModel) should contain(fullNameES.split(" ")(0))
    khermesES.Name.lastNames(khermesEN.Name.nameModel) should contain(fullNameES.split(" ")(1))
  }

  it should "generate valid middle names: firstName firstName with EN and ES locales" in {
    val khermesEN = Faker()
    val middleNameEN = khermesEN.Name.middleName
    middleNameEN should fullyMatch regex """[a-zA-Z]+ [a-zA-Z]+"""
    khermesEN.Name.firstNames(khermesEN.Name.nameModel) should contain(middleNameEN.split(" ")(0))
    khermesEN.Name.firstNames(khermesEN.Name.nameModel) should contain(middleNameEN.split(" ")(1))

    val khermesES = Faker("ES")
    val middleNameES = khermesES.Name.middleName
    middleNameES should fullyMatch regex """[a-zA-Z]+ [a-zA-Z]+"""
    khermesES.Name.firstNames(khermesEN.Name.nameModel) should contain(middleNameES.split(" ")(0))
    khermesES.Name.firstNames(khermesEN.Name.nameModel) should contain(middleNameES.split(" ")(1))
  }

  it should "raise an exception when it gets a firstName/lastName and firstNames/lastNames are empty in the locale" in {
    val khermes = Faker("XX")
    //scalastyle:off
    an[NoSuchElementException] should be thrownBy khermes.Name.firstName()
    an[NoSuchElementException] should be thrownBy khermes.Name.lastName()
    //scalastyle:on
  }

  it should "raise an exception when it tries to load a locale that don't exist" in {
    //scalastyle:off
    val model = Faker("XY").Name.nameModel
    //scalastyle:on
    model.map(_.left.get should equal(s"Error loading invalid resource /locales/name/XY.json"))
  }
}
