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

/**
 * Generates random locations.
 */
case class GeoGenerator(locale: String) extends FakerGenerator
  with LazyLogging
  with AppSerializer {

  override def name: String = "geo"

  lazy val geoModel = locale match {
    case AppConstants.DefaultLocale => {
      val resources = getResources(name)
        .map(parse[Seq[GeoModel]](name, _))
      if (parseErrors[Seq[GeoModel]](resources).nonEmpty) logger.warn(s"${parseErrors[Seq[GeoModel]](resources)}")
      resources
    }
    case localeMatch => Seq(parse[Seq[GeoModel]](name, s"$localeMatch.json"))
  }

  /**
   * Example: "geolocation() -> 40.493556, -3.566764, Madrid".
   * @return a random geolocation.
   */
  def geolocation(): GeoModel = {

    randomElementFromAList[GeoModel](geoModelList(geoModel))
      .getOrElse(throw new KhermesException(s"Error loading locate /locales/$name/$locale.json"))
  }

  /**
   * Example: "geolocationWithoutCity() -> 28.452717, -13.863761".
   * @return a random geolocation.
   */
  def geolocationWithoutCity(): (Double, Double) = {
    val geo = geolocation
    (geo.longitude, geo.latitude)
  }

  /**
   * Example: "city() -> Tenerife".
   * @return a random city name.
   */
  def city(): String = {
    geolocation.city
  }

  def geoModelList(l: Seq[Either[String, Seq[GeoModel]]]): Seq[GeoModel] = {
    l.filter(_.isRight).flatMap(_.right.get)
  }

  def geoWithoutCityList(l: Seq[Either[String, Seq[GeoModel]]]): Seq[(Double, Double)] = {
    geoModelList(l).map(geomodel => (geomodel.longitude, geomodel.latitude))
  }

  def cityList(l: Seq[Either[String, Seq[GeoModel]]]): Seq[String] = {
    geoModelList(l).map(geomodel => geomodel.city)
  }

  /**
   * Return the locale code of input data
   * @return locale code (e.g. ES, EN)
   */
  def country: String = locale

}

case class GeoModel(latitude: Double, longitude: Double, city: String)
