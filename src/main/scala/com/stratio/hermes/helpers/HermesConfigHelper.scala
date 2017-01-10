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

package com.stratio.hermes.helpers

import com.stratio.hermes.helpers.HermesConfigHelper._
import com.typesafe.config.{ConfigFactory, Config}

import scala.util.Try

case class HermesConfigHelper(configContent: String, template: String) {

  val config = ConfigFactory.parseString(configContent)

  def assertConfig(): Unit = {
    def buildErrors(mandatoryFields: Seq[String]): Seq[String] =
      for {
        mandatoryField <- mandatoryFields
        if Try(config.getAnyRef(mandatoryField)).isFailure
      } yield(s"$mandatoryField not found in the config.")

    val errors = buildErrors(MandatoryFields) ++ (if(isAvro) buildErrors(AvroMandatoryFields) else Seq.empty)
    assert(errors.isEmpty, errors.mkString("\n"))
  }

  def configType(): ConfigType.Value =
    if(config.getString("kafka.key.serializer") == "io.confluent.kafka.serializers.KafkaAvroSerializer") {
      ConfigType.Avro
    } else {
      ConfigType.Json
    }

  def isAvro: Boolean = configType() == ConfigType.Avro

  def isJson: Boolean = configType() == ConfigType.Json

  def topic: String = config.getString("hermes.topic-name")

  def templateName: String = config.getString("hermes.template-name")

  def templateContent: String = template

  def hermesI18n: String = config.getString("hermes.i18n")

  def avroSchema: String = config.getString(config.getString("hermes.avro-schema"))

  def kafkaConfig: Config = config.atKey("kakfa")
}


object HermesConfigHelper {

  val MandatoryFields = Seq(
    "hermes.topic-name",
    "hermes.template-name",
    "hermes.i18n",
    "kafka.key.serializer"
  )

  val AvroMandatoryFields = Seq(
    "hermes.avro-schema",
    "kafka.schema.registry.url"
  )

  object ConfigType extends Enumeration {
    val Avro, Json = Value
  }
}