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
package com.stratio.khermes.cluster.supervisor.stream.kafkaimpl

import akka.NotUsed
import akka.stream.scaladsl.Flow
import cats.data.State
import com.stratio.khermes.cluster.supervisor.stream.StreamGenericOperations._
import com.stratio.khermes.commons.config.AppConfig
import com.stratio.khermes.persistence.kafka.KafkaClient
import org.apache.kafka.clients.producer.RecordMetadata

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Codec

object StreamKafkaOperations {
  case class KafkaOperations(topic: String, counter: Int, suspend: List[Future[RecordMetadata]])

  def executeStateKafka(k: KafkaClient[String])(implicit ec: ExecutionContext): (String) => (KafkaOperations) => KafkaOperations =
    (event: String) =>
      (st: KafkaOperations) => {
        st.copy(counter = st.counter + 1)
      }

  def kafkaFlow[A](config: AppConfig)
                  (implicit codec: Codec,
                   client: KafkaClient[String],
                   ec: ExecutionContext): Flow[List[String], State[KafkaOperations, List[String]], NotUsed] = {
    Flow[List[String]].map { events =>
      executeBatchState(events)(executeStateKafka(client))
    }
  }
}
