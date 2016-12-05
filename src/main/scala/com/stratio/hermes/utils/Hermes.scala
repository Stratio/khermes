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

import com.stratio.hermes.constants.HermesConstants
import com.stratio.hermes.implicits.HermesSerializer
import com.stratio.hermes.models.NameModel
import org.json4s._
import org.json4s.native.Serialization.read
import com.stratio.hermes.helpers.RandomHelper
import scala.language.postfixOps
import scala.util.{Random, Try}

/**
 * Hermes util used for to generate random values.
 */
case class Hermes(locale: String = HermesConstants.ConstantDefaultLocale) extends HermesSerializer {

  /**
   * Generates random names.
   */
  object Name extends HermesUnit {

    override def unitName(): String = "name"

    lazy val nameModel =
      Try(read[NameModel](getClass.getResourceAsStream(s"/locales/$unitName/$locale.json")))
        .getOrElse(throw new IllegalStateException(s"Error loading locale: /locales/$unitName/$locale.json"))

    /**
     * Example: "Bruce Wayne".
     * @return a full name.
     */
    def fullName(): String = s"$firstName $lastName"

    /**
     * Example: "Bruce Lex".
     * @return a middle name.
     */
    def middleName(): String = s"$firstName $firstName"

    /**
     * Example: "Bruce".
     * @return a first name.
     */
    def firstName(): String =
      RandomHelper.randomElementFromAList[String](nameModel.firstNames).getOrElse(throw new NoSuchElementException)

    /**
     * Example: "Wayne".
     * @return a last name.
     */
    def lastName(): String =
      RandomHelper.randomElementFromAList[String](nameModel.lastNames).getOrElse(throw new NoSuchElementException)
  }

  /**
   * Generates random numbers.
   */
  object Number {

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
        val first = Random.nextInt(HermesConstants.ConstantDecimalValue - 1) + 1
        val randSeq = first +: (1 until n).map { _ => Random.nextInt(HermesConstants.ConstantDecimalValue) }
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
        val nonZero = Random.nextInt(HermesConstants.ConstantDecimalValue - 1) + 1
        val randSeq = (1 until n).map { _ => Random.nextInt(HermesConstants.ConstantDecimalValue) } :+ nonZero
        randSeq.mkString
      }
    }

    /**
     * Example: "number(3,Sign.-) -123".
     * @return an Integer positive or negative depending of Sign parameter.
     */
    def number(n: Int, sign: Sign): Int = {
      if (sign.equals(Positive)) Math.abs(number(n)) else Math.abs(number(n)) * -1
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
    def decimal(n: Int): Double = (number(n).toString + "." + numberDec(n)).toDouble

    /**
     * Example: "decimal(3,Sign.-) -> -123.456".
     * @param n decimal part size.
     * @return a random double with same integer and decimal part with defined sign.
     */
    def decimal(n: Int, sign: Sign): Double = (number(n, sign).toString + "." + numberDec(n)).toDouble

    /**
     * Example: "decimal(3,1) -> 123.4".
     * @param m integer part size.
     * @param n decimal part size.
     * @return a random double with random sign.
     */
    def decimal(m: Int, n: Int): Double = (number(m).toString + "." + numberDec(n)).toDouble

    /**
     * Example: "decimal(3,2,Sign.-) -> -123.45".
     * @param m    integer part size.
     * @param n    decimal part size.
     * @param sign sign positive or negative.
     * @return a random double with defined sign.
     */
    def decimal(m: Int, n: Int, sign: Sign): Double = (number(m, sign).toString + "." + numberDec(n)).toDouble
  }

}


