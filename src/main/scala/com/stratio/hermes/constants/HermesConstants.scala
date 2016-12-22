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

package com.stratio.hermes.constants

/**
 * Global constants used in the application.
 */
object HermesConstants {

  val ConstantDefaultLocale = "ALL"
  val ConstantAkkaClusterName = "hermes"
  val ConstantDecimalValue = 10
  val ConstantWorkerSupervisorTimeout = 5

  val ConstantGeneratedTemplatesPrefix = "generated-templates"
  val ConstantGeneratedClassesPrefix = "generated-classes"

  val AvroSchema =
    """
      |{
      |  "type": "record",
      |  "name": "myrecord",
      |  "fields":
      |    [
      |      {
      |        "name":"id",
      |        "type":"string"
      |      },
      |      {
      |        "name":"customerId",
      |        "type":"int"
      |      },
      |      {
      |        "name":"customerName",
      |        "type": "string"
      |      },
      |      {
      |        "name":"latitude",
      |        "type": "double"
      |      },
      |      {
      |        "name": "longitude",
      |        "type": "double"
      |      }
      |    ]
      |}
    """.stripMargin

  val TwirlTemplate =
    """
      |@import com.stratio.hermes.utils.Hermes
      |@import com.stratio.hermes.utils.Positive
      |@import java.util.UUID
      |
      |@(hermes: Hermes)
      |{
      |  "id" : "@(UUID.randomUUID().toString)",
      |  "customerId": @(hermes.Number.number(1,Positive)),
      |  "customerName": "@(hermes.Name.fullName)",
      |  "latitude": @(hermes.Geo.geolocation.latitude),
      |  "longitude": @(hermes.Geo.geolocation.longitude),
      |  "productIds": [@((1 to 5).map(x => hermes.Number.number(1, Positive)).mkString(","))]
      |}
    """.stripMargin

}
