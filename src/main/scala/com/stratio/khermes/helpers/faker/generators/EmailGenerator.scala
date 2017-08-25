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
import com.stratio.khermes.commons.exceptions.KhermesException
import com.stratio.khermes.commons.implicits.AppSerializer
import com.stratio.khermes.helpers.faker.FakerGenerator
import com.typesafe.scalalogging.LazyLogging

import scala.util.Try

case class EmailGenerator(locale: String) extends FakerGenerator
  with AppSerializer
  with LazyLogging {

  override def name: String = "email"

  lazy val emailModel = locale match {
    case AppConstants.DefaultLocale =>
      val resources = getResources(name)
        .map(parse[Seq[String]](name, _))
      val maybeErrors = parseErrors[Seq[String]](resources)
      if (maybeErrors.nonEmpty) logger.warn(s"$maybeErrors")
      resources
    case localeValue =>
      val resource = Seq(parse[Seq[String]](name, s"$localeValue.json"))
      val maybeErrors = parseErrors[Seq[String]](resource)
      if (maybeErrors.nonEmpty) logger.warn(s"$maybeErrors")
      resource
  }

  def domains(emailModel: Seq[Either[String, Seq[String]]]): Seq[String] =
    emailModel
      .filter(_.isRight)
      .flatMap(_.right.get)

  /**
   * Returns an email address using a fullname and a random domain
   * @param fullname Name and surname
   * @return A valid email address, as string, concatenating the first letter from the name and the whole surname,
   *         and finally a random domain
   */
  def address(fullname: String): String = {
    val domain = randomElementFromAList[String](domains(emailModel)).getOrElse(
      throw new KhermesException(s"Error loading locate /locales/$name/$locale.json"))
    s"${getInitial(fullname)}${getSurname(fullname)}@$domain"
  }

  private def getInitial(fullname: String) = {
    Try(getName(fullname).charAt(0)).getOrElse(throw new KhermesException(s"Error parsing a no valid name"))
  }

  def getName(fullName: String): String =
    Try(fullName.trim.split(" ")(0)).getOrElse(
      throw new KhermesException(s"Error extracting the name value")).toLowerCase

  def getSurname(fullName: String): String =
    Try(fullName.trim.split(" ")(1)).getOrElse(
      throw new KhermesException(s"Error extracting the surname value")).toLowerCase
}

