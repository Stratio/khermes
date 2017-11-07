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
package com.stratio.khermes.commons.constants

/**
  * Global constants used in the application.
  */
object AppConstants {

  val DefaultLocale = "ALL"
  val AkkaClusterName = "khermes"
  val DecimalValue = 10
  val SupervisorStart = 5
  val SupervisorStop = 30

  val GeneratedTemplatesPrefix = "generated-templates"
  val GeneratedClassesPrefix = "generated-classes"
  val KafkaAvroSerializer = "io.confluent.kafka.serializers.KafkaAvroSerializer"

  val ZookeeperParentPath = "/stratio/khermes"
  val ZookeeperConnection = "zookeeper.connection"
  val ZookeeperConnectionDefault = "master.mesos:2181"
  val ZookeeperConnectionTimeout = "zookeeper.connectionTimeout"
  val ZookeeperSessionTimeout = "zookeeper.sessionTimeout"
  val ZookeeperRetryAttempts = "zookeeper.retryAttempts"
  val ZookeeperRetryInterval = "zookeeper.retryInterval"

  val KafkaConfigPath = "kafka-config"
  val GeneratorConfigPath = "generator-config"
  val TwirlTemplatePath = "twirl-template"
  val AvroConfigPath = "avro-config"

  // TODO: check this
  val FileConfigPath = "file-config"

  val CommandNotFoundMessage = "Command not found. Type help to list available commands."
  val HelpMessage =
    s"""Khermes client provides the next commands to manage your Khermes cluster:
        |  Usage: COMMAND [args...]
        |
        |  Commands:
        |     start [command options] : Starts event generation in N nodes.
        |       --generator-config  : Khermes configuration
        |       --kafka-config      : Kafka configuration
        |       --file-config       : Local file configuration
        |       --twirl-template    : Template to generate data
        |       --avro-template     : Avro configuration
        |       --ids               : Node id where start khermes
        |     stop [command options] : Stop event generation in N nodes.
        |       --ids               : Node id where start khermes
        |     ls : List the nodes with their current status
        |     save [command options] : Save your configuration in zookeeper
        |       --generator-config  : Khermes configuration
        |       --kafka-config      : Kafka configuration
        |       --file-config       : Localfile configuration
        |       --twirl-template    : Template to generate data
        |       --avro-template     : Avro configuration
        |     show [command options] : Show your configuration
        |       --generator-config  : Khermes configuration
        |       --kafka-config      : Kafka configuration
        |       --file-config       : Local file configuration
        |       --twirl-template    : Template to generate data
        |       --avro-template     : Avro configuration
        |     clear : Clean the screen.
        |     help : Print this usage.
        |     exit | quit | bye : Exit of Khermes Cli. """.stripMargin

  val DefaultWSHost = "0.0.0.0"
  val DefaultWSPort = 8080

  val DefaultStrategy = Option("default")

  val LoggerEnabled = "metrics.logger.enabled"
  val LoggerEnabledDefault = false
  val GraphiteEnabled = "metrics.graphite.enabled"
  val GraphiteEnabledDefault = false
  val LoggerReporterName = "metrics.logger.name"
  val LoggerReporterNameDefault = "khermes"
  val GraphiteReporterName = "metrics.graphite.name"
  val GraphiteReporterNameDefault = "khermes"
  val GraphiteReporterHost = "metrics.graphite.host"
  val GraphiteReporterHostDefault = "localhost"
  val GraphiteReporterPort = "metrics.graphite.port"
  val GraphiteReporterPortDefault = 2003

}
