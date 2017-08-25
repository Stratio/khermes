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
