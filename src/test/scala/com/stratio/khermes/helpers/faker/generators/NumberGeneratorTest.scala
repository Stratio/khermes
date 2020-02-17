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
package com.stratio.khermes.helpers.faker.generators

import java.security.InvalidParameterException

import com.stratio.khermes.commons.exceptions.KhermesException
import com.stratio.khermes.helpers.faker.Faker
import org.junit.runner.RunWith
import org.scalacheck.Prop.forAll
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

/*
@RunWith(classOf[JUnitRunner])
class NumberGeneratorTest extends FlatSpec
  with Matchers {

  it should "generate a random integer of 0 digit give it 0" in {
    val khermesNum = Faker("")
    khermesNum.Number.number(0) shouldBe 0
  }

  it should "generate a random integer when it passed the number of digit" in {
    val khermesNum = Faker("")
    forAll { (n: Int) =>
      numberOfDigitsFromANumber(khermesNum.Number.number(n)) == n
    }
  }

  it should "generate a random integer when it passed the number of digit and the sign" in {
    val khermesNum = Faker("")
    //scalastyle:off
    forAll { (n: Int) =>
      khermesNum.Number.number(n, Positive) > 0
    }
    forAll { (n: Int) =>
      khermesNum.Number.number(n, Negative) < 0
    }
    //scalastyle:on
  }

  it should "throw an InvalidParameterException when a negative digit is passed or greater than the VAL_MAX" in {
    val khermesNum = Faker("")
    //scalastyle:off
    an[InvalidParameterException] should be thrownBy khermesNum.Number.number(-2)
    an[InvalidParameterException] should be thrownBy khermesNum.Number.number(500)
    an[InvalidParameterException] should be thrownBy khermesNum.Number.decimal(-2)
    an[InvalidParameterException] should be thrownBy khermesNum.Number.decimal(2, -2)
    an[InvalidParameterException] should be thrownBy khermesNum.Number.decimal(2, 11)
    //scalastyle:on
  }
  it should "generate a random decimal of 0 digit give it 0.0" in {
    val khermesNum = Faker("")
    khermesNum.Number.decimal(0) shouldBe 0.0
    khermesNum.Number.decimal(0, 0) shouldBe 0.0
  }

  it should "generate a random decimal when it passed the number of digit" in {
    val khermesNum = Faker("")
    //scalastyle:off
    numberOfDigitsFromANumber(khermesNum.Number.decimal(2)) shouldBe 4
    numberOfDigitsFromANumber(khermesNum.Number.decimal(2, 4)) shouldBe 6
    numberOfDigitsFromANumber(khermesNum.Number.decimal(0, 2)) shouldBe 3
    numberOfDigitsFromANumber(khermesNum.Number.decimal(9, 9)) shouldBe 18
    numberOfDigitsFromANumber(khermesNum.Number.decimal(9, 0)) shouldBe 10
    numberOfDigitsFromANumber(khermesNum.Number.decimal(8, Positive)) shouldBe 16
    numberOfDigitsFromANumber(khermesNum.Number.decimal(7, Negative)) shouldBe 14
    numberOfDigitsFromANumber(khermesNum.Number.decimal(9, 7, Positive)) shouldBe 16
    numberOfDigitsFromANumber(khermesNum.Number.decimal(2, 1, Negative)) shouldBe 3
    //scalastyle:on
  }

  it should "throw an InvalidParameterException when pass an sign that is null" in {
    val khermesNum = Faker("")
    //scalastyle:off
    an[KhermesException] should be thrownBy khermesNum.Number.number(2, null)
    //scalastyle:on
  }

  it should "generate a random integer when it passed the range " in {
    val khermesNum = Faker("")
    forAll { (n: Int, m: Int) =>
      khermesNum.Number.numberInRange(n, m) <= m && khermesNum.Number.numberInRange(n, m) >= n
    }
    //scalastyle:off
    numberOfDigitsFromANumber(khermesNum.Number.numberInRange(1, 9)) shouldBe 1
    numberOfDigitsFromANumber(khermesNum.Number.numberInRange(99, 10)) shouldBe 2
    numberOfDigitsFromANumber(khermesNum.Number.numberInRange(100, 999)) shouldBe 3
    //scalastyle:on
  }

  it should "generate a random decimal between the range" in {
    val khermesNum = Faker("")
    val decimalInRange = khermesNum.Number.decimalInRange(1, 2).toInt

    decimalInRange should be (1)
  }

  it should "generate a random decimal when it passed the range " in {
    val khermesNum = Faker("")
    forAll { (n: Int, m: Int) =>
      khermesNum.Number.decimalInRange(n, m) <= m && khermesNum.Number.decimalInRange(n, m) >= n
    }
  }

  it should "generate a random rating number less than the number passed as parameter" in {
    val khermesNum = Faker("")

    val rating = khermesNum.Number.rating(1)
    rating shouldBe 0
  }

  it should "generate a random rating for all n's" in {
    val khermesNum = Faker("")

    forAll { (n: Int) =>
      khermesNum.Number.rating(n) < n
    }
  }

  /**
   * Returns length of a Integer element.
   * @param n number to calculate length.
   * @return size of the integer.
   */
  def numberOfDigitsFromANumber(n: Int): Int = if (n == 0) 1 else math.log10(math.abs(n)).toInt + 1

  /**
   * Returns length of a BigDecimal element.
   * @param n number to calculate length.
   * @return size of the BigDecimal.
   */
  def numberOfDigitsFromANumber(n: BigDecimal): Int = n.abs.toString.length - 1
}
*/