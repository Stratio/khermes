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

package com.stratio.khermes.utils.generators

import java.security.InvalidParameterException

import com.stratio.khermes.exceptions.KHermesException
import com.stratio.khermes.implicits.KHermesSerializer
import org.joda.time.{DateTime, LocalTime, Seconds}
import org.joda.time.format.DateTimeFormat

import scala.util.{Random, Try}

/**
 * Generates random dates.
 */
class DatetimeGenerator extends KHermesSerializer {
  /**
   * Example: "dateTime("1970-1-12" ,"2017-1-1") -> 2005-03-01T20:34:30.000+01:00".
   *
   * @return a random dateTime.
   */
  def datetime(from: DateTime, to: DateTime, format: Option[String] = None): String = {
    assert(to.getMillis > from.getMillis, throw new InvalidParameterException(s"$to must be greater than $from"))
    val diff = Seconds.secondsBetween(from, to).getSeconds
    val randomDate = new Random(System.nanoTime)
    val date: DateTime = from.plusSeconds(randomDate.nextInt(diff.toInt))
    format match {
      case Some(stringFormat) => Try(DateTimeFormat.forPattern(stringFormat).print(date)).getOrElse(
        throw new KHermesException(s"Invalid DateTimeFormat"))
      case None => date.toString()
    }
  }

  /**
   * Example: time() -> 15:30:00.000+01:00
   *
   * @return a random time from 00:00:00.000 to 23:59:59.999 (ISO8601)
   */
  def time(): String = {
    //scalastyle:off
    new LocalTime(Random.nextInt(24), Random.nextInt(60), Random.nextInt(60), Random.nextInt(1000)).toString()
    //scalastyle:on
  }
}
