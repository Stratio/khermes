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

package com.stratio.khermes.metrics

import com.stratio.khermes.commons.constants.AppConstants
import com.codahale.metrics.{Counter, Meter, SharedMetricRegistries, Timer}

import scala.util.Try

trait KhermesMetrics {

  val khermesConfig = com.stratio.khermes.commons.implicits.AppImplicits.config
  lazy val logReporterEnabled = Try(khermesConfig.getBoolean(AppConstants.LoggerEnabled)).getOrElse(
    AppConstants.LoggerEnabledDefault)
  lazy val graphiteReporterEnabled = Try(khermesConfig.getBoolean(AppConstants.GraphiteEnabled)).getOrElse(
    AppConstants.GraphiteEnabledDefault)
  val loggerReporterName = Try(khermesConfig.getString(AppConstants.LoggerReporterName)).getOrElse(
    AppConstants.LoggerReporterNameDefault)
  val graphiteReporterName = Try(khermesConfig.getString(AppConstants.GraphiteReporterName)).getOrElse(
    AppConstants.GraphiteReporterNameDefault)
  val availableMetrics = List(logReporter, graphiteReporter).flatten

  def getAvailableCounterMetrics(counterName: String): List[Counter] =
    for (availableMetric <- availableMetrics) yield
      SharedMetricRegistries.getOrCreate(availableMetric).counter(counterName)

  def getAvailableMeterMetrics(counterName: String): List[Meter] =
    for (availableMetric <- availableMetrics) yield
      SharedMetricRegistries.getOrCreate(availableMetric).meter(counterName)

  def increaseCounterMetric[T](metrics: List[Counter]): Unit =
    for (metric <- metrics) metric.inc()

  def markMeterMetric[T](metrics: List[Meter]): Unit =
    for (metric <- metrics) metric.mark()

  private def logReporter() =
    if (logReporterEnabled) {
      Some(loggerReporterName)
    }
    else {
      None
    }

  private def graphiteReporter() =
    if (graphiteReporterEnabled) {
      Some(graphiteReporterName)
    }
    else {
      None
    }
}
