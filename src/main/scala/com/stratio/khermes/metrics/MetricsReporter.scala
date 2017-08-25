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

import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

import com.codahale.metrics.graphite.{Graphite, GraphiteReporter}
import com.codahale.metrics.jvm.{ClassLoadingGaugeSet, GarbageCollectorMetricSet, MemoryUsageGaugeSet, ThreadStatesGaugeSet}
import com.codahale.metrics.{MetricFilter, MetricRegistry, SharedMetricRegistries, Slf4jReporter}
import com.stratio.khermes.commons.constants.AppConstants
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.slf4j.LoggerFactory

import scala.util.Try

object MetricsReporter extends LazyLogging {

  val config: Config = com.stratio.khermes.commons.implicits.AppImplicits.config
  val loggerReporterEnabled: Boolean = Try(config.getBoolean(AppConstants.LoggerEnabled)).getOrElse(
    AppConstants.LoggerEnabledDefault)
  val graphiteReporterEnabled: Boolean = Try(config.getBoolean(AppConstants.GraphiteEnabled)).getOrElse(
    AppConstants.GraphiteEnabledDefault)
  val loggerReporterName = Try(config.getString(AppConstants.LoggerReporterName)).getOrElse(
    AppConstants.LoggerReporterNameDefault)
  val graphiteReporterName = Try(config.getString(AppConstants.GraphiteReporterName)).getOrElse(
    AppConstants.GraphiteReporterNameDefault)
  val graphiteReporterHost = Try(config.getString(AppConstants.GraphiteReporterHost)).getOrElse(
    AppConstants.GraphiteReporterHostDefault)
  val graphiteReporterPort = Try(config.getInt(AppConstants.GraphiteReporterPort)).getOrElse(
    AppConstants.GraphiteReporterPortDefault)
  val metricsFrequencyInSeconds = 1

  def start(): Unit = {
    if (loggerReporterEnabled) {
      logger.info("Starting the logger metrics reporter...")
      startSlf4jReporter
    }

    if (graphiteReporterEnabled) {
      logger.info("Starting the graphite metrics reporter...")
      startGraphiteReporter
    }
  }

  private def startSlf4jReporter = {
    val logReporter = Slf4jReporter.forRegistry(
      SharedMetricRegistries.getOrCreate(loggerReporterName))
      .outputTo(LoggerFactory.getLogger("metrics"))
      .convertRatesTo(TimeUnit.SECONDS)
      .convertDurationsTo(TimeUnit.MILLISECONDS)
      .build
    logReporter.start(metricsFrequencyInSeconds, TimeUnit.SECONDS)
  }

  private def startGraphiteReporter = {
    logger.info(s"Writing graphite data to: $graphiteReporterHost:$graphiteReporterPort")
    logger.error(s"WITH THE FOLLOWING PREFIX: $graphiteReporterName")
    val graphite = new Graphite(new InetSocketAddress(graphiteReporterHost, graphiteReporterPort))
    val graphiteRegistry = SharedMetricRegistries.getOrCreate(graphiteReporterName)
    val graphiteReporter = GraphiteReporter
      .forRegistry(
        graphiteRegistry)
      .prefixedWith(graphiteReporterName)
      .convertRatesTo(TimeUnit.SECONDS)
      .convertDurationsTo(TimeUnit.MILLISECONDS)
      .filter(MetricFilter.ALL)
      .build(graphite)
    graphiteReporter.start(metricsFrequencyInSeconds, TimeUnit.SECONDS)
    setUpJVMInstrumentation(graphiteRegistry)
  }

  private def setUpJVMInstrumentation(graphiteRegistry: MetricRegistry) = {
    setUpClassLoadingMetricsGauge(graphiteRegistry)
    setUpMemoryUsageMetricsGauge(graphiteRegistry)
    setUpGarbageCollectorGauge(graphiteRegistry)
    setUpThreadsStatusGauge(graphiteRegistry)
  }

  private def setUpClassLoadingMetricsGauge(registry: MetricRegistry) =
    registry.register("ClassLoading", new ClassLoadingGaugeSet())

  private def setUpMemoryUsageMetricsGauge(registry: MetricRegistry) =
    registry.register("MemoryUsage", new MemoryUsageGaugeSet())

  private def setUpGarbageCollectorGauge(registry: MetricRegistry) =
    registry.register("GarbageCollector", new GarbageCollectorMetricSet())

  private def setUpThreadsStatusGauge(registry: MetricRegistry) =
    registry.register("ThreadStates", new ThreadStatesGaugeSet())
}
