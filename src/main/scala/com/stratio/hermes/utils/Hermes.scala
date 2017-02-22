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

import java.io.File
import java.security.InvalidParameterException
import com.stratio.hermes.constants.HermesConstants
import com.stratio.hermes.exceptions.HermesException
import com.stratio.hermes.helpers.RandomHelper
import com.stratio.hermes.implicits.HermesSerializer
import com.stratio.hermes.models.{GeoModel, NameModel}
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, Seconds}
import org.json4s._
import org.json4s.native.Serialization.read
import scala.collection.immutable.Seq
import scala.language.postfixOps
import scala.math.BigDecimal.RoundingMode
import scala.util.{Failure, Random, Success, Try}

/**
 * Hermes util used for to generate random values.
 */
case class Hermes(locale: String = HermesConstants.DefaultLocale) extends HermesSerializer {

  /**
   * Generates random names.
   */
  object Name extends HermesUnit {

    override def unitName(): String = "name"

    lazy val nameModel: NameModel = locale match {
      case HermesConstants.DefaultLocale =>
        val listNameModels = new File(getClass.getResource(s"/locales/$unitName").getFile).list().map(x => {
          read[NameModel](getClass.getResourceAsStream(s"/locales/$unitName/$x"))
        })
        val firstNames = listNameModels.flatMap(_.firstNames).toList
        val lastNames = listNameModels.flatMap(_.lastNames).toList
        NameModel(firstNames, lastNames)
      case _ => Try(read[NameModel](getClass.getResourceAsStream(s"/locales/$unitName/$locale.json")))
        .getOrElse(throw new IllegalStateException(s"Error loading locale: /locales/$unitName/$locale.json"))
    }

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
        val first = Random.nextInt(HermesConstants.DecimalValue - 1) + 1
        val randSeq = first +: (1 until n).map { _ => Random.nextInt(HermesConstants.DecimalValue) }
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
        val nonZero = Random.nextInt(HermesConstants.DecimalValue - 1) + 1
        val randSeq = (1 until n).map { _ => Random.nextInt(HermesConstants.DecimalValue) } :+ nonZero
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
      case _ => throw new HermesException(s"The sign must be Positive or Negative")
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
    def decimal(n: Int): BigDecimal = setScale(number(n).toString + "." + numberDec(n), n)

    /**
     * Example: "decimal(3,Sign.-) -> -123.456".
     * @param n decimal part size.
     * @return a random double with same integer and decimal part with defined sign.
     */
    def decimal(n: Int, sign: NumberSign): BigDecimal = setScale(number(n, sign).toString + "." + numberDec(n), n)

    /**
     * Example: "decimal(3,1) -> 123.4".
     * @param m integer part size.
     * @param n decimal part size.
     * @return a random double with random sign.
     */
    def decimal(m: Int, n: Int): BigDecimal = setScale(number(m).toString + "." + numberDec(n), n)

    /**
     * Example: "decimal(3,2,Sign.-) -> -123.45".
     * @param m    integer part size.
     * @param n    decimal part size.
     * @param sign sign positive or negative.
     * @return a random double with defined sign.
     */
    def decimal(m: Int, n: Int, sign: NumberSign): BigDecimal = setScale(number(m, sign).toString + "." + numberDec(n), n)

    def setScale(s: String, n: Int): BigDecimal = {
      if (n == 0) BigDecimal.valueOf(s.toDouble).setScale(1, RoundingMode.HALF_UP) else BigDecimal.valueOf(s.toDouble).setScale(n, RoundingMode.HALF_UP)
    }
  }

  /**
   * Generates random locations.
   */
  object Geo extends HermesUnit with HermesLogging {

    override def unitName(): String = "geo"

    lazy val geoModel = locale match {
      case HermesConstants.DefaultLocale => {
        val fileNames = new File(getClass.getResource(s"/locales/$unitName").getFile).list()
        val geoModelAndErrors: List[Either[String, Seq[GeoModel]]] = (for {
          filename <- fileNames
        } yield parser(filename)).toList
        if (parseErrorList(geoModelAndErrors).nonEmpty) log.warn(s"${parseErrorList(geoModelAndErrors)}")
        geoModelAndErrors
      }
      case localeMatch => List(parser(s"$localeMatch.json"))
    }

    def parser(s: String): Either[String, Seq[GeoModel]] = {
      Try(read[List[GeoModel]](getClass.getResourceAsStream(s"/locales/$unitName/$s"))) match {
        case Success(v) => Right(v)
        case Failure(e) => Left(s"${e.getMessage} in file /locales/$unitName/$s")
      }
    }

    /**
     * Example: "geolocation() -> 57.585393,-157.571944".
     * @return a random geolocation.
     */
    def geolocation(): (GeoModel) = {

      RandomHelper.randomElementFromAList[(GeoModel)](geoModelList(geoModel))
        .getOrElse(throw new HermesException(s"Error loading locate /locales/$unitName/$locale.json"))
    }

    def geoModelList(l: List[Either[String, Seq[GeoModel]]]): List[GeoModel] = {
      l.filter(_.isRight).flatMap(_.right.toOption.get)
    }

    def parseErrorList(l: List[Either[String, Seq[GeoModel]]]): List[String] = {
      l.filter(_.isLeft).map(_.left.toOption.get)
    }
  }

  /**
   * Generates random dates.
   */
  object Datetime {
    /**
     * Example: "dateTime("1970-1-12" ,"2017-1-1") -> 2005-03-01T20:34:30.000+01:00".
     * @return a random dateTime.
     */
    def datetime(from: DateTime, to: DateTime, format: Option[String] = None): String = {
      assert(to.getMillis > from.getMillis, throw new InvalidParameterException(s"$to must be greater than $from"))
      val diff = Seconds.secondsBetween(from, to).getSeconds
      val randomDate = new Random(System.nanoTime)
      val date: DateTime = from.plusSeconds(randomDate.nextInt(diff.toInt))

      //      format.flatMap(pattern => {
//        Try(DateTimeFormat.forPattern(pattern).print(date)).toOption
//      }).getOrElse(throw HermesException(s"Invalid DateTimeFormat"))
      format match {
        case Some(stringFormat) => Try(DateTimeFormat.forPattern(stringFormat).print(date)).getOrElse(throw new HermesException(s"Invalid DateTimeFormat"))
        case None => date.toString()
      }
    }
  }

}
sealed trait NumberSign
case object Positive extends NumberSign
case object Negative extends NumberSign
