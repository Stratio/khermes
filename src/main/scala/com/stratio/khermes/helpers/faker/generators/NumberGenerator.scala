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

import com.stratio.khermes.commons.constants.AppConstants
import com.stratio.khermes.commons.exceptions.KhermesException
import com.stratio.khermes.commons.implicits.AppSerializer

import scala.math.BigDecimal.RoundingMode
import scala.util.Random

/**
 * Generates random numbers.
 */

class NumberGenerator extends AppSerializer {


  val locale = new java.util.Locale("es", "ES")
  val formatter = java.text.NumberFormat.getNumberInstance(locale)

  val roundingMode = BigDecimal.RoundingMode.HALF_EVEN
  /**
   * Example: "number(3) 123".
   * @param n integer size.
   * @return a random integer.
   */
  def number(n: Int): Int = {
    assert(n >= 0 && n <= 9, throw new InvalidParameterException(s"$n must be between 0 and 9"))
    if (n == 0) {
      0
    } else {
      val first = Random.nextInt(AppConstants.DecimalValue - 1) + 1
      val randSeq = first +: (1 until n).map { _ => Random.nextInt(AppConstants.DecimalValue) }
      BigInt(randSeq.mkString).toInt * randSign
    }
  }

  /**
   * Example: "number(3) 123".
   * @param n integer size.
   * @return a random string that contain the decimal part of a number.
   */
  def numberDec(n: Int): String = {
    assert(n >= 0 && n <= 9, throw new InvalidParameterException(s"$n must be between 0 and 9"))
    if (n == 0) {
      "0"
    } else {
      val nonZero = Random.nextInt(AppConstants.DecimalValue - 1) + 1
      val randSeq = (1 until n).map { _ => Random.nextInt(AppConstants.DecimalValue) } :+ nonZero
      randSeq.mkString
    }
  }

  /**
   * Example: "number(3,Sign.-) -123".
   * @return an Integer positive or negative depending of Sign parameter.
   */
  def number(n: Int, sign: NumberSign): Int = sign match {
    case Positive => Math.abs(number(n))
    case Negative => Math.abs(number(n)) * -1
    case _ => throw new KhermesException(s"The sign must be Positive or Negative")
  }

  /**
   * @return a random 1 or -1.
   */
  def randSign: Int = if (Random.nextBoolean) 1 else -1

  /**
   * Example: "number(3) -> 123.456".
   * @param n decimal part size.
   * @return a random double with same integer and decimal part and random sign.
   */
  def decimal(n: Int): BigDecimal = setScale(number(n).toString + "," + numberDec(n), n)

  /**
   * Example: "decimal(3,Sign.-) -> -123.456".
   * @param n decimal part size.
   * @return a random double with same integer and decimal part with defined sign.
   */
  def decimal(n: Int, sign: NumberSign): BigDecimal = setScale(number(n, sign).toString + "," + numberDec(n), n)

  /**
   * Example: "decimal(3,1) -> 123.4".
   * @param m integer part size.
   * @param n decimal part size.
   * @return a random double with random sign.
   */
  def decimal(m: Int, n: Int): BigDecimal = setScale(number(m).toString + "," + numberDec(n), n)

  /**
   * Example: "decimal(3,2,Sign.-) -> -123.45".
   * @param m    integer part size.
   * @param n    decimal part size.
   * @param sign sign positive or negative.
   * @return a random double with defined sign.
   */
  def decimal(m: Int, n: Int, sign: NumberSign): BigDecimal = setScale(number(m, sign).toString + "," + numberDec(n), n)

  /**
   * Example: "numberInRange(3,5) 4".
   * @param m one end of range.
   * @param n the other end of range.
   * @return a random BigDecimal.
   */
  def numberInRange(m: Int, n: Int): Int = {
    Random.nextInt((Math.max(m, n) - Math.min(n, m)) + 1) + Math.min(n, m)
  }

  /**
   * Example: "decimalInRange(3,4) 3.1446350167374337".
   * @param m one end of range.
   * @param n the other end of range.
   * @return a random BigDecimal.
   */
  def decimalInRange(m: Int, n: Int): String = {
    val result = BigDecimal(Random.nextDouble() * (Math.max(m, n) - Math.min(n, m)) + Math.min(n, m)).setScale(2, roundingMode)
    val number = formatter.format(result)
    if (number == "0") {
      "0,00"
    }
    else if (number.length == 1) {
      s"$number,00"
    }
    else {
      number
    }
  }

  def setScale(s: String, n: Int): BigDecimal = {
    if (n == 0) BigDecimal.valueOf(s.toDouble).setScale(1, RoundingMode.HALF_UP) else BigDecimal.valueOf(
      s.toDouble).setScale(n, RoundingMode.HALF_UP)
  }

  /**
   * Example rating(5) 0-4
   * @param n max integer rating (exclude)
   * @return random integer rating [0-n)
   */
  def rating(n: Int): Int = {
    Random.nextInt(n)
  }
}

sealed trait NumberSign

case object Positive extends NumberSign

case object Negative extends NumberSign
