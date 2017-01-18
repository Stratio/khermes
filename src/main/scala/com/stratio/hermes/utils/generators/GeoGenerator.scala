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
import com.stratio.hermes.exceptions.HermesException
import com.stratio.hermes.helpers.ParserHelper._
import com.stratio.hermes.helpers.RandomHelper
import com.stratio.hermes.helpers.ResourcesHelper._
import com.stratio.hermes.implicits.HermesSerializer
import com.stratio.hermes.models.GeoModel
import com.stratio.hermes.utils.{HermesLogging, HermesUnit}


/**
 * Generates random locations.
 */
case class GeoGenerator(locale: String) extends HermesUnit
  with HermesLogging
  with HermesSerializer {

  override def unitName: String = "geo"

  lazy val geoModel = locale match {
    case HermesConstants.DefaultLocale => {
      val resources = getResources(unitName)
        .map(parse[Seq[GeoModel]](unitName, _))
      if (parseErrors[Seq[GeoModel]](resources).nonEmpty) log.warn(s"${parseErrors[Seq[GeoModel]](resources)}")
      resources
    }
    case localeMatch => Seq(parse[Seq[GeoModel]](unitName, s"$localeMatch.json"))
  }

  /**
   * Example: "geolocation -> 57.585393,-157.571944".
   *
   * @return a random geolocation.
   */
  def geolocation: GeoModel = {

    RandomHelper.randomElementFromAList[GeoModel](geoModelList(geoModel))
      .getOrElse(throw new HermesException(s"Error loading locate /locales/$unitName/$locale.json"))
  }

  /**
   * Example: "geolocationWithoutCity() -> 28.452717, -13.863761".
   * @return a random geolocation.
   */
  def geolocationWithoutCity(): (Double, Double) = {
    val geo = geolocation()
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

  def geoWithoutCityList(l: List[Either[String, Seq[GeoModel]]]): Seq[(Double,Double)] = {
    geoModelList(l).map(geomodel => (geomodel.longitude,geomodel.latitude))
  }

  def cityList(l: List[Either[String, Seq[GeoModel]]]): Seq[String] = {
    geoModelList(l).map(geomodel => geomodel.city)
  }

  /**
   * Return the locale code of input data
   * @return locale code (e.g. ES, EN)
   */
  def country: String = locale

}
