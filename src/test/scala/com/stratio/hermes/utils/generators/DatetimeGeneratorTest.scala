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
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DatetimeGeneratorTest extends FlatSpec
  with Matchers {

  it should "generate a random date between two dates" in {
    val hermes = Hermes()
    val startDate = new DateTime("1970-1-1")
    val endDate = new DateTime("2017-1-1")
    val randomDateString = hermes.Datetime.datetime(startDate, endDate)
    val randomDate = DateTime.parse(randomDateString)
    assert(startDate.compareTo(randomDate) * randomDate.compareTo(endDate) > 0)
  }

  it should "raise an exception if start Date is greater than end Date" in {
    val hermes = Hermes()
    val startDate = new DateTime("2017-1-1")
    val endDate = new DateTime("1985-1-1")
    an[InvalidParameterException] should be thrownBy hermes.Datetime.datetime(startDate, endDate, None)
  }

  it should "generate a random date in a custom format" in {
    val hermes = Hermes()
    val startDate = new DateTime("1970-1-1")
    val endDate = new DateTime("1985-1-1")
    val randomDateString = hermes.Datetime.datetime(startDate, endDate, Option("yyyy-MM-dd"))
    val randomDate = DateTime.parse(randomDateString)
    randomDateString shouldBe DateTimeFormat.forPattern(randomDateString.format("yyyy-MM-dd")).print(randomDate)
  }

  it should "generate a random date in a complex format" in {
    val hermes = Hermes()
    val startDate = new DateTime("1970-1-1")
    val endDate = new DateTime("1985-1-1")
    val randomDateString = hermes.Datetime.datetime(startDate, endDate, Option("yyyy-MM-dd'T'HH:mm:ss.SSS"))
    val randomDate = DateTime.parse(randomDateString)
    randomDateString shouldBe DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").print(randomDate)
  }

  it should "generate a random date with a bad format" in {
    val hermes = Hermes()
    val startDate = new DateTime("1970-1-1")
    val endDate = new DateTime("1985-1-1")
    //scalastyle:off
    an[HermesException] should be thrownBy hermes.Datetime.datetime(startDate, endDate, Option("Invalid format"))
    //scalastyle:on
  }

  "time" should "return a valid hour (0:0:0.000 - 23:59:59.999)" in {
    val hermes = Hermes()
    val time = hermes.Datetime.time
    time shouldBe a[String]
    val timeAsString = time.split(":").toList
    timeAsString(0).toInt should (be >= 0 and be < 24)
    timeAsString(1).toInt should (be >= 0 and be < 60)
    timeAsString(2).split("\\.")(0).toInt should (be >= 0 and be < 60)
    timeAsString(2).split("\\.")(1).toInt should (be >= 0 and be < 1000)

  }

}
