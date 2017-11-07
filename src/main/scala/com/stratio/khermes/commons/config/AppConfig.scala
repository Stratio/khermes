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
package com.stratio.khermes.commons.config

import java.time.Duration

import com.stratio.khermes.commons.constants.AppConstants
import com.typesafe.config.ConfigFactory

import scala.util.Try
import com.stratio.khermes.commons.config.AppConfig._

/**
 * Class used to load and parse configuration that will be used by the application.
 * Remember that it should be serializable because will be part as an Akka message.
 *
 * @param khermesConfigContent with configuration about khermes generator.
 * @param kafkaConfigContent with kafka configuration.
 * @param template to generate.
 * @param avroSchema in the case that you are using avro serialization.
 */
case class AppConfig(khermesConfigContent: String,
                     kafkaConfigContent: Option[String] = None,
                     localFileConfigContent: Option[String] = None,
                     template: String,
                     avroSchema: Option[String] = None) {

  val khermesConfig = ConfigFactory.parseString(khermesConfigContent)

  val kafkaConfig = kafkaConfigContent match {
    case Some(kcfg) =>
      // TODO: check that it can stat without kafka config
      val config = Some(ConfigFactory.parseString(kcfg))
      config
    case _ => None
  }

  val fileConfig = {
    localFileConfigContent match {
      case Some(fcfg) => Some(ConfigFactory.parseString(fcfg))
      case _ => None
    }
  }

  assertCorrectConfig()

  /** Check only if sink is Kafka !! **/
  protected[this] def assertCorrectConfig(): Unit = {
    def buildErrors(mandatoryFields: Seq[String]): Seq[String] = {
      // Check if Kafka is defined in configuration
        for {
          mandatoryField <- mandatoryFields
          if Try(khermesConfig.getAnyRef(mandatoryField)).isFailure && Try(kafkaConfig.get.getAnyRef(mandatoryField)).isFailure
        } yield (s"$mandatoryField not found in the config.")
    }

    def buildErrorsFile(mandatoryFields: Seq[String]): Seq[String] = {
      // Check if Kafka is defined in configuration
      for {
        mandatoryField <- mandatoryFields
        if Try(khermesConfig.getAnyRef(mandatoryField)).isFailure
      } yield (s"$mandatoryField not found in the config.")
    }

    // TODO: Check error for khermes and file
    val errors = {
      if (kafkaConfig.isDefined) {
        buildErrors(MandatoryFields) ++ (if (configType == ConfigType.Avro) buildErrors(AvroMandatoryFields) else Seq.empty)
      }
      /*
      else if(fileConfig.isDefined) {
        buildErrorsFile(FileMandatoryFields)
      }*/
      else {
        Seq.empty
      }
    }

    assert(errors.isEmpty, errors.mkString("\n"))
  }

  def configType(): ConfigType.Value = {
    // Check kafka config only when kafka is not None
    if (kafkaConfig.get.getString("kafka.key.serializer") == AppConstants.KafkaAvroSerializer) {
      ConfigType.Avro
    } else {
      ConfigType.Json
    }
  }

  def topic: String = khermesConfig.getString("khermes.topic")

  def templateName: String = khermesConfig.getString("khermes.template-name")

  def templateContent: String = template

  def khermesI18n: String = khermesConfig.getString("khermes.i18n")

  def strategy: Option[String] = Try(khermesConfig.getString("khermes.strategy")).toOption

  def timeoutNumberOfEventsOption: Option[Int] = Try(khermesConfig.getInt("khermes.timeout-rules.number-of-events")).toOption

  def timeoutNumberOfEventsDurationOption: Option[Duration] = Try(khermesConfig.getDuration("khermes.timeout-rules.duration")).toOption

  def stopNumberOfEventsOption: Option[Int] = Try(khermesConfig.getInt("khermes.stop-rules.number-of-events")).toOption

  def filePath: String = if(fileConfig.isDefined) fileConfig.get.getString("file.path") else ""

}

object AppConfig {

  val MandatoryFields = Seq(
    "khermes.topic",
    "khermes.template-name",
    "khermes.i18n",
    "kafka.key.serializer"
  )

  val AvroMandatoryFields = Seq(
    "kafka.schema.registry.url"
  )

  val FileMandatoryFields = Seq(
    "file.path",
    "khermes.template-name",
    "khermes.i18n"
  )

  object ConfigType extends Enumeration {
    val Avro, Json = Value
  }
}
