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

import java.security.InvalidParameterException

import org.junit.runner.RunWith
import org.scalacheck.Prop.forAll
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
    val fullNameEN = hermesEN.Name.fullName
    fullNameEN should fullyMatch regex """[a-zA-Z]+ [a-zA-Z]+"""
    hermesEN.Name.nameModel.firstNames should contain (fullNameEN.split(" ")(0))
    hermesEN.Name.nameModel.lastNames should contain (fullNameEN.split(" ")(1))

    val hermesES = Hermes("ES")
    val fullNameES = hermesES.Name.fullName
    fullNameES should fullyMatch regex """[a-zA-Z]+ [a-zA-Z]+"""
    hermesES.Name.nameModel.firstNames should contain (fullNameES.split(" ")(0))
    hermesES.Name.nameModel.lastNames should contain (fullNameES.split(" ")(1))
  }

  it should "generate valid middle names: firstName firstName with EN and ES locales" in {
    val hermesEN = Hermes()
    val middleNameEN = hermesEN.Name.middleName
    middleNameEN should fullyMatch regex """[a-zA-Z]+ [a-zA-Z]+"""
    hermesEN.Name.nameModel.firstNames should contain (middleNameEN.split(" ")(0))
    hermesEN.Name.nameModel.firstNames should contain (middleNameEN.split(" ")(1))

    val hermesES = Hermes("ES")
    val middleNameES = hermesES.Name.middleName
    middleNameES should fullyMatch regex """[a-zA-Z]+ [a-zA-Z]+"""
    hermesES.Name.nameModel.firstNames should contain (middleNameES.split(" ")(0))
    hermesES.Name.nameModel.firstNames should contain (middleNameES.split(" ")(1))
  }

  it should "raise an exception when it gets a firstName/lastName and firstNames/lastNames are empty in the locale" in {
    val hermes = Hermes("XX")
    //scalastyle:off
    an[NoSuchElementException] should be thrownBy hermes.Name.firstName()
    an[NoSuchElementException] should be thrownBy hermes.Name.lastName()
    //scalastyle:on
  }

  it should "raise an exception when it tries to load a locale that don't exist" in {
    //scalastyle:off
    val thrown = the[IllegalStateException] thrownBy Hermes("XY").Name.firstName()
    //scalastyle:on
    thrown.getMessage should equal(s"Error loading locale: /locales/name/XY.json")
  }

  it should "generate a random integer of 0 digit give it 0" in {
    val hermesNum = Hermes("")
    hermesNum.Number.number(0) shouldBe 0

  }

  it should "generate a random integer when it passed the number of digit" in {
    val hermesNum = Hermes("")
    forAll { (n: Int) =>
      numberOfDigitsFromANumber(hermesNum.Number.number(n)) == n
    }
  }

  it should "generate a random integer when it passed the number of digit and the sign" in {
    val hermesNum = Hermes("")
    //scalastyle:off
    forAll { (n: Int) =>
      hermesNum.Number.number(n, Positive) > 0
    }
    forAll { (n: Int) =>
      hermesNum.Number.number(n, Negative) < 0
    }
    //scalastyle:on
  }


  it should "throw an InvalidParameterException when a negative digit is passed or greater than the VAL_MAX" in {
    val hermesNum = Hermes("")
    //scalastyle:off
    an[InvalidParameterException] should be thrownBy hermesNum.Number.number(-2)
    an[InvalidParameterException] should be thrownBy hermesNum.Number.number(500)
    an[InvalidParameterException] should be thrownBy hermesNum.Number.decimal(-2)
    an[InvalidParameterException] should be thrownBy hermesNum.Number.decimal(2, -2)
    an[InvalidParameterException] should be thrownBy hermesNum.Number.decimal(2, 11)
    //scalastyle:on
  }
  it should "generate a random decimal of 0 digit give it 0.0" in {
    val hermesNum = Hermes("")
    hermesNum.Number.decimal(0) shouldBe 0.0
    hermesNum.Number.decimal(0, 0) shouldBe 0.0
  }

  it should "generate a random decimal when it passed the number of digit" in {
    val hermesNum = Hermes("")
    //scalastyle:off
    numberOfDigitsFromANumber(hermesNum.Number.decimal(2)) shouldBe 4
    numberOfDigitsFromANumber(hermesNum.Number.decimal(2, 4)) shouldBe 6
    numberOfDigitsFromANumber(hermesNum.Number.decimal(0, 2)) shouldBe 3
    numberOfDigitsFromANumber(hermesNum.Number.decimal(2, 0)) shouldBe 3
    numberOfDigitsFromANumber(hermesNum.Number.decimal(2, Positive)) shouldBe 4
    numberOfDigitsFromANumber(hermesNum.Number.decimal(2, Negative)) shouldBe 4
    numberOfDigitsFromANumber(hermesNum.Number.decimal(2, 1, Positive)) shouldBe 3
    numberOfDigitsFromANumber(hermesNum.Number.decimal(2, 1, Negative)) shouldBe 3
    //scalastyle:on
  }

  /**
    * Returns length of a Integer element.
    *
    * @param n number to calculate length.
    * @return size of the integer.
    */
  def numberOfDigitsFromANumber(n: Int): Int = if (n == 0) 1 else math.log10(math.abs(n)).toInt + 1

  /**
    * Returns length of a Double element.
    *
    * @param n number to calculate length.
    * @return size of the double.
    */
  def numberOfDigitsFromANumber(n: Double): Int = math.abs(n).toString.length - 1

}
