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

import com.stratio.hermes.constants.HermesConstants
import com.stratio.hermes.exceptions.HermesException
import com.stratio.hermes.implicits.HermesSerializer

import scala.math.BigDecimal.RoundingMode
import scala.util.Random

/**
 * Generates random numbers.
 */
class NumberGenerator extends HermesSerializer {

  /**
   * Example: "number(3) 123".
   *
   * @param n integer size.
   * @return a random integer.
   */
  def number(n: Int): Int = {
    assert(n >= 0 && n <= 9, throw new InvalidParameterException(s"$n must be between 0 and 9"))
    if (n == 0) {
      0
    } else {
      val first = Random.nextInt(HermesConstants.DecimalValue - 1) + 1
      val randSeq = first +: (1 until n).map { _ => Random.nextInt(HermesConstants.DecimalValue) }
      BigInt(randSeq.mkString).toInt * randSign
    }
  }

  /**
   * Example: "number(3) 123".
   *
   * @param n integer size.
   * @return a random string that contain the decimal part of a number.
   */
  def numberDec(n: Int): String = {
    assert(n >= 0 && n <= 9, throw new InvalidParameterException(s"$n must be between 0 and 9"))
    if (n == 0) {
      "0"
    } else {
      val nonZero = Random.nextInt(HermesConstants.DecimalValue - 1) + 1
      val randSeq = (1 until n).map { _ => Random.nextInt(HermesConstants.DecimalValue) } :+ nonZero
      randSeq.mkString
    }
  }

  /**
   * Example: "number(3,Sign.-) -123".
   *
   * @return an Integer positive or negative depending of Sign parameter.
   */
  def number(n: Int, sign: NumberSign): Int = sign match {
    case Positive => Math.abs(number(n))
    case Negative => Math.abs(number(n)) * -1
    case _ => throw new HermesException(s"The sign must be Positive or Negative")
  }

  /**
   * @return a random 1 or -1.
   */
  def randSign: Int = if (Random.nextBoolean) 1 else -1

  /**
   * Example: "number(3) -> 123.456".
   *
   * @param n decimal part size.
   * @return a random double with same integer and decimal part and random sign.
   */
  def decimal(n: Int): BigDecimal = setScale(number(n).toString + "." + numberDec(n), n)

  /**
   * Example: "decimal(3,Sign.-) -> -123.456".
   *
   * @param n decimal part size.
   * @return a random double with same integer and decimal part with defined sign.
   */
  def decimal(n: Int, sign: NumberSign): BigDecimal = setScale(number(n, sign).toString + "." + numberDec(n), n)

  /**
   * Example: "decimal(3,1) -> 123.4".
   *
   * @param m integer part size.
   * @param n decimal part size.
   * @return a random double with random sign.
   */
  def decimal(m: Int, n: Int): BigDecimal = setScale(number(m).toString + "." + numberDec(n), n)

  /**
   * Example: "decimal(3,2,Sign.-) -> -123.45".
   *
   * @param m    integer part size.
   * @param n    decimal part size.
   * @param sign sign positive or negative.
   * @return a random double with defined sign.
   */
  def decimal(m: Int, n: Int, sign: NumberSign): BigDecimal = setScale(number(m, sign).toString + "." + numberDec(n), n)

  def setScale(s: String, n: Int): BigDecimal = {
    if (n == 0) BigDecimal.valueOf(s.toDouble).setScale(1, RoundingMode.HALF_UP) else BigDecimal.valueOf(
      s.toDouble).setScale(n, RoundingMode.HALF_UP)
  }

  /**
   * Example rating(5) 0-4
   *
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
