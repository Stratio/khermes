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

import com.stratio.hermes.constants.HermesConstants
import com.stratio.hermes.helpers.ParserHelper._
import com.stratio.hermes.helpers.RandomHelper
import com.stratio.hermes.helpers.ResourcesHelper._
import com.stratio.hermes.implicits.HermesSerializer
import com.stratio.hermes.models.NameModel
import com.stratio.hermes.utils.{HermesLogging, HermesUnit}

/**
 * Generates random names.
 */
case class NameGenerator(locale: String) extends HermesUnit
  with HermesSerializer
  with HermesLogging {

  override def unitName: String = "name"

  lazy val nameModel = locale match {
    case HermesConstants.DefaultLocale =>
      val resources = getResources(unitName)
        .map(parse[NameModel](unitName, _))
      if (parseErrors[NameModel](resources).nonEmpty) log.warn(s"${parseErrors[NameModel](resources)}")
      resources
    case localeValue => Seq(parse[NameModel](unitName, s"$localeValue.json"))
  }

  /**
   * Example: "Bruce Wayne".
   *
   * @return a full name.
   */
  def fullName(): String = s"$firstName $lastName"

  /**
   * Example: "Bruce Lex".
   *
   * @return a middle name.
   */
  def middleName(): String = s"$firstName $firstName"

  /**
   * Example: "Bruce".
   *
   * @return a first name.
   */
  def firstName(): String =
    RandomHelper.randomElementFromAList[String](firstNames(nameModel)).getOrElse(throw new NoSuchElementException)


  /**
   * Example: "Wayne".
   *
   * @return a last name.
   */
  def lastName(): String =
    RandomHelper.randomElementFromAList[String](lastNames(nameModel)).getOrElse(throw new NoSuchElementException)

  def lastNames(resources: Seq[Either[String, NameModel]]): Seq[String] = {
    getName(resources: Seq[Either[String, NameModel]]).flatMap(_.firstNames)
  }

  private def getName(resources: Seq[Either[String, NameModel]]): Seq[NameModel] = {
    resources.filter(_.isRight).map(_.right.get)
  }

  def firstNames(resources: Seq[Either[String, NameModel]]): Seq[String] = {
    getName(resources).flatMap(_.firstNames)
  }

}