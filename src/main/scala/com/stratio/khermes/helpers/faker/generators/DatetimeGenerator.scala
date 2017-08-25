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
import com.stratio.khermes.commons.implicits.AppSerializer
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, LocalTime, Seconds}

import scala.util.{Random, Try}

/**
 * Generates random dates.
 */
class DatetimeGenerator extends AppSerializer {
  /**
   * Example: "dateTime("1970-1-12" ,"2017-1-1") -> 2005-03-01T20:34:30.000+01:00".
   * @return a random dateTime.
   */
  def datetime(from: DateTime, to: DateTime, format: Option[String] = None): String = {
    assert(to.getMillis > from.getMillis, throw new InvalidParameterException(s"$to must be greater than $from"))
    val diff = Seconds.secondsBetween(from, to).getSeconds
    val randomDate = new Random(System.nanoTime)
    val date: DateTime = from.plusSeconds(randomDate.nextInt(diff.toInt))
    format match {
      case Some(stringFormat) => Try(DateTimeFormat.forPattern(stringFormat).print(date)).getOrElse(
        throw new KhermesException(s"Invalid DateTimeFormat"))
      case None => date.toString()
    }
  }

  /**
   * Example: time() -> 15:30:00.000+01:00
   * @return a random time from 00:00:00.000 to 23:59:59.999 (ISO8601)
   */
  def time(): String = {
    //scalastyle:off
    new LocalTime(Random.nextInt(24), Random.nextInt(60), Random.nextInt(60), Random.nextInt(1000)).toString()
    //scalastyle:on
  }
}
