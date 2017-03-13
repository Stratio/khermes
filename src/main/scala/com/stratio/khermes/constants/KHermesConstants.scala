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

package com.stratio.khermes.constants

/**
 * Global constants used in the application.
 */
object KHermesConstants {

  val DefaultLocale = "ALL"
  val AkkaClusterName = "khermes"
  val DecimalValue = 10
  val SupervisorStart = 5
  val SupervisorStop = 30

  val GeneratedTemplatesPrefix = "generated-templates"
  val GeneratedClassesPrefix = "generated-classes"
  val KafkaAvroSerializer = "io.confluent.kafka.serializers.KafkaAvroSerializer"

  val ZookeeperParentPath= "/stratio/khermes"
  val ZookeeperConnection = "zookeeper.connection"
  val ZookeeperConnectionDefault = "master.mesos:2181"
  val ZookeeperConnectionTimeout = "zookeeper.connectionTimeout"
  val ZookeeperSessionTimeout = "zookeeper.sessionTimeout"
  val ZookeeperRetryAttempts = "zookeeper.retryAttempts"
  val ZookeeperRetryInterval = "zookeeper.retryInterval"

  val KafkaConfigNodePath = "kafka"
  val KHermesConfigNodePath = "khermes"
  val TemplateNodePath = "template"
  val AvroConfigNodePath = "avro"
}
