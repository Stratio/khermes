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

package com.stratio.hermes.utils.generators

import java.security.InvalidParameterException

import com.stratio.hermes.exceptions.HermesException
import com.stratio.hermes.utils.Hermes
import org.junit.runner.RunWith
import org.scalacheck.Prop.forAll
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class NumberGeneratorTest extends FlatSpec
  with Matchers {


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
    numberOfDigitsFromANumber(hermesNum.Number.decimal(9, 9)) shouldBe 18
    numberOfDigitsFromANumber(hermesNum.Number.decimal(9, 0)) shouldBe 10
    numberOfDigitsFromANumber(hermesNum.Number.decimal(8, Positive)) shouldBe 16
    numberOfDigitsFromANumber(hermesNum.Number.decimal(7, Negative)) shouldBe 14
    numberOfDigitsFromANumber(hermesNum.Number.decimal(9, 7, Positive)) shouldBe 16
    numberOfDigitsFromANumber(hermesNum.Number.decimal(2, 1, Negative)) shouldBe 3
    //scalastyle:on
  }

  it should "throw an InvalidParameterException when pass an sign that is null" in {
    val hermesNum = Hermes("")
    //scalastyle:off
    an[HermesException] should be thrownBy hermesNum.Number.number(2, null)
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
   * Returns length of a BigDecimal element.
   *
   * @param n number to calculate length.
   * @return size of the BigDecimal.
   */
  def numberOfDigitsFromANumber(n: BigDecimal): Int = n.abs.toString.length - 1

}
