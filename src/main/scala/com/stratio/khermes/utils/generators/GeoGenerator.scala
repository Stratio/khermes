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

import com.stratio.khermes.constants.KhermesConstants
import com.stratio.khermes.exceptions.KhermesException
import com.stratio.khermes.helpers.ParserHelper._
import com.stratio.khermes.helpers.RandomHelper
import com.stratio.khermes.helpers.ResourcesHelper._
import com.stratio.khermes.implicits.KhermesSerializer
import com.stratio.khermes.models.GeoModel
import com.stratio.khermes.utils.KhermesUnit
import com.typesafe.scalalogging.LazyLogging


/**
 * Generates random locations.
 */
case class GeoGenerator(locale: String) extends KhermesUnit
  with LazyLogging
  with KhermesSerializer {

  override def unitName: String = "geo"

  lazy val geoModel = locale match {
    case KhermesConstants.DefaultLocale => {
      val resources = getResources(unitName)
        .map(parse[Seq[GeoModel]](unitName, _))
      if (parseErrors[Seq[GeoModel]](resources).nonEmpty) logger.warn(s"${parseErrors[Seq[GeoModel]](resources)}")
      resources
    }
    case localeMatch => Seq(parse[Seq[GeoModel]](unitName, s"$localeMatch.json"))
  }

  /**
   * Example: "geolocation() -> 40.493556, -3.566764, Madrid".
   * @return a random geolocation.
   */
  def geolocation(): GeoModel = {

    RandomHelper.randomElementFromAList[GeoModel](geoModelList(geoModel))
      .getOrElse(throw new KhermesException(s"Error loading locate /locales/$unitName/$locale.json"))
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
