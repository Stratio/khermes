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
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class DatetimeGeneratorTest extends FlatSpec
  with Matchers {

  it should "generate a random date between two dates" in {
    val khermes = Faker()
    val startDate = new DateTime("1970-1-1")
    val endDate = new DateTime("2017-1-1")
    val randomDateString = khermes.Datetime.datetime(startDate, endDate)
    val randomDate = DateTime.parse(randomDateString)
    assert(startDate.compareTo(randomDate) * randomDate.compareTo(endDate) > 0)
  }

  it should "raise an exception if start Date is greater than end Date" in {
    val khermes = Faker()
    val startDate = new DateTime("2017-1-1")
    val endDate = new DateTime("1985-1-1")
    an[InvalidParameterException] should be thrownBy khermes.Datetime.datetime(startDate, endDate, None)
  }

  it should "generate a random date in a custom format" in {
    val khermes = Faker()
    val startDate = new DateTime("1970-1-1")
    val endDate = new DateTime("1985-1-1")
    val randomDateString = khermes.Datetime.datetime(startDate, endDate, Option("yyyy-MM-dd"))
    val randomDate = DateTime.parse(randomDateString)
    randomDateString shouldBe DateTimeFormat.forPattern(randomDateString.format("yyyy-MM-dd")).print(randomDate)
  }

  it should "generate a random date in a complex format" in {
    val khermes = Faker()
    val startDate = new DateTime("1970-1-1")
    val endDate = new DateTime("1985-1-1")
    val randomDateString = khermes.Datetime.datetime(startDate, endDate, Option("yyyy-MM-dd'T'HH:mm:ss.SSS"))
    val randomDate = DateTime.parse(randomDateString)
    randomDateString shouldBe DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").print(randomDate)
  }

  it should "generate a random date with a bad format" in {
    val khermes = Faker()
    val startDate = new DateTime("1970-1-1")
    val endDate = new DateTime("1985-1-1")
    //scalastyle:off
    an[KhermesException] should be thrownBy khermes.Datetime.datetime(startDate, endDate, Option("Invalid format"))
    //scalastyle:on
  }

  "time" should "return a valid hour (0:0:0.000 - 23:59:59.999)" in {
    val khermes = Faker()
    val time = khermes.Datetime.time
    time shouldBe a[String]
    val timeAsString = time.split(":").toList
    timeAsString(0).toInt should (be >= 0 and be < 24)
    timeAsString(1).toInt should (be >= 0 and be < 60)
    timeAsString(2).split("\\.")(0).toInt should (be >= 0 and be < 60)
    timeAsString(2).split("\\.")(1).toInt should (be >= 0 and be < 1000)

  }

}
