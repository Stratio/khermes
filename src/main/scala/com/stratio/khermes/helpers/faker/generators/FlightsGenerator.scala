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

package com.stratio.khermes.helpers.faker.generators

import com.stratio.khermes.commons.constants.AppConstants
import com.stratio.khermes.commons.exceptions.KhermesException
import com.stratio.khermes.commons.implicits.AppSerializer
import com.stratio.khermes.helpers.faker.FakerGenerator
import com.typesafe.scalalogging.LazyLogging
import kantan.csv._
import kantan.csv.ops._
import kantan.csv.generic._

case class FlightsGenerator(locale: String) extends FakerGenerator
  with AppSerializer
  with LazyLogging {

  override def name: String = "flights"

  val resourcesFilesSmall = Seq("1990", "1991", "1992", "1993", "1994", "1995",
    "1996", "1997", "1998", "1999", "2000", "2001", "2002", "2003",
    "2004", "2005", "2006", "2007", "2008")

  lazy val flightsModel: Seq[FlightModel] = locale match {
    case AppConstants.DefaultLocale | "ALL-SMALL" | "EN" | "ES" =>
      logger.info("Flights from all resources .... ")
      resourcesFilesSmall.flatMap(resource => getFlights(resource))
    case resource =>
      logger.info("Flights from one resource .... ")
      getFlights(resource)
  }

  def getFlights(resource: String): Seq[FlightModel] = {
    logger.info(s"Loading resource: $resource .... ")
    val rawData = getClass.getResource(s"/locales/$name/$resource.csv")
    logger.info(s"Raw data : ${rawData.toString}")
    rawData.asCsvReader[FlightModel](rfc.withHeader).toSeq.filter(_.isSuccess).map(_.get)
  }

  def flight: FlightModel = {
    val randomElement = randomElementFromAList[FlightModel](flightsModel).getOrElse(
      throw new KhermesException(s"Error loading locate /locales/$name/$locale"))
      randomElement.copy(Cancelled = if(!randomElement.Cancelled.isEmpty) randomElement.Cancelled else "NA",
        Diverted = if(!randomElement.Diverted.isEmpty) randomElement.Diverted else "NA",
        AirTime = if(randomElement.AirTime.isEmpty || randomElement.AirTime == "NA") "0" else randomElement.AirTime,
        Distance = if(randomElement.Distance.isEmpty || randomElement.Distance == "NA") "0" else randomElement.Distance,
        ArrDelay = if(randomElement.ArrDelay.isEmpty || randomElement.ArrDelay == "NA") "0" else randomElement.ArrDelay,
        DepDelay = if(randomElement.DepDelay.isEmpty || randomElement.DepDelay == "NA") "0" else randomElement.DepDelay)
  }
}

final case class FlightModel(
                              Year: String,
                             Month: String,
                             DayofMonth: String,
                             DayOfWeek: String,
                             DepTime: String,
                             CRSDepTime: String,
                             ArrTime: String,
                             CRSArrTime: String,
                             UniqueCarrier: String,
                             FlightNum: String,
                             TailNum: String,
                             ActualElapsedTime: String,
                             CRSElapsedTime: String,
                             AirTime: String,
                             ArrDelay: String,
                             DepDelay: String,
                             Origin: String,
                             Dest: String,
                             Distance: String,
                             TaxiIn: String,
                             TaxiOut: String,
                             Cancelled: String,
                             CancellationCode: String,
                             Diverted: String,
                             CarrierDelay: String,
                             WeatherDelay: String,
                             NASDelay: String,
                             SecurityDelay: String,
                             LateAircraftDelay: String
                            )
