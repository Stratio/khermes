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

import com.stratio.hermes.constants.HermesConstants
import com.stratio.hermes.helpers.HermesConfig._
import com.stratio.hermes.kafka.KafkaClient
import com.typesafe.config.ConfigFactory
import io.confluent.kafka.serializers.KafkaAvroSerializer


import scala.util.Try

case class HermesConfig(hermesConfigContent: String,
                        kafkaConfigContent: String,
                        template: String,
                        avroSchema: Option[String] = None) {

  val hermesConfig = ConfigFactory.parseString(hermesConfigContent)
  val kafkaConfig = ConfigFactory.parseString(kafkaConfigContent)

  assertCorrectConfig()
  kafkaClientInstance().parseProperties()

  protected[this] def assertCorrectConfig(): Unit = {
    def buildErrors(mandatoryFields: Seq[String]): Seq[String] =
      for {
        mandatoryField <- mandatoryFields
        if Try(hermesConfig.getAnyRef(mandatoryField)).isFailure && Try(kafkaConfig.getAnyRef(mandatoryField)).isFailure
      } yield(s"$mandatoryField not found in the config.")

    val errors = buildErrors(MandatoryFields) ++ (if(configType == ConfigType.Avro) buildErrors(AvroMandatoryFields) else Seq.empty)
    assert(errors.isEmpty, errors.mkString("\n"))
  }

   def kafkaClientInstance[T](): KafkaClient[T] = {
    new KafkaClient[T](kafkaConfig)
  }

  def configType(): ConfigType.Value =
    if(kafkaConfig.getString("kafka.key.serializer") == HermesConstants.KafkaAvroSerializer) {
      ConfigType.Avro
    } else {
      ConfigType.Json
    }

  def topic: String = hermesConfig.getString("hermes.topic")

  def templateName: String = hermesConfig.getString("hermes.template-name")

  def templateContent: String = template

  def hermesI18n: String = hermesConfig.getString("hermes.i18n")

}

object HermesConfig {

  val MandatoryFields = Seq(
    "hermes.topic",
    "hermes.template-name",
    "hermes.i18n",
    "kafka.key.serializer"
  )

  val AvroMandatoryFields = Seq(
    "kafka.schema.registry.url"
  )

  object ConfigType extends Enumeration {
    val Avro, Json = Value
  }
}
