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

import com.stratio.khermes.commons.constants.AppConstants
import com.stratio.khermes.commons.implicits.AppSerializer
import com.stratio.khermes.helpers.faker.FakerGenerator
import com.typesafe.scalalogging.LazyLogging

/**
 * Generates random names.
 */
case class NameGenerator(locale: String, strategy: Option[String]) extends FakerGenerator
  with AppSerializer
  with LazyLogging {

  override def name: String = "name"

  lazy val nameModel: Seq[Either[String, NameModel]] = locale match {
    case AppConstants.DefaultLocale =>
      val resources = getResources(name)
      if (strategy.isDefined) {
        val res = resources.map(parse[NameModel](name, _)
          .right.map(elem =>
          NameModel(
            repeatElementsInList(listWithStrategy(elem.firstNames, strategy.getOrElse(throw new IllegalArgumentException("Bad strategy definition"))))
            , repeatElementsInList(listWithStrategy(elem.lastNames, strategy.getOrElse(throw new IllegalArgumentException("Bad strategy definition")))))
        ))
        if (parseErrors[NameModel](res).nonEmpty) logger.warn(s"${parseErrors[NameModel](res)}")
        res
      } else {
        val res = resources.map(parse[NameModel](name, _))
        if (parseErrors[NameModel](res).nonEmpty) logger.warn(s"${parseErrors[NameModel](res)}")
        res
      }
    case localeValue =>
      if (strategy.isDefined) {
        Seq(parse[NameModel](name, s"$localeValue.json")
          .right.map(elem =>
          NameModel(repeatElementsInList(listWithStrategy(elem.firstNames, strategy.getOrElse(throw new IllegalArgumentException("Bad strategy definition")))),
            repeatElementsInList(listWithStrategy(elem.lastNames, strategy.getOrElse(throw new IllegalArgumentException("Bad strategy definition"))))))
        )
      } else {
        Seq(parse[NameModel](name, s"$localeValue.json"))
      }
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
  randomElementFromAList[String](firstNames(nameModel)).getOrElse(throw new NoSuchElementException)


  /**
   * Example: "Wayne".
   * @return a last name.
   */
  def lastName(): String =
  randomElementFromAList[String](lastNames(nameModel)).getOrElse(throw new NoSuchElementException)

  def lastNames(resources: Seq[Either[String, NameModel]]): Seq[String] = {
    getName(resources: Seq[Either[String, NameModel]]).flatMap(_.lastNames)
  }

  private def getName(resources: Seq[Either[String, NameModel]]): Seq[NameModel] = {
    resources.filter(_.isRight).map(_.right.get)
  }

  def firstNames(resources: Seq[Either[String, NameModel]]): Seq[String] = {
    getName(resources).flatMap(_.firstNames)
  }
}

case class NameModel(firstNames: Seq[String], lastNames: Seq[String])
