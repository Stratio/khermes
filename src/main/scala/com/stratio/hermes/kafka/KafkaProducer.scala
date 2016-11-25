package com.stratio.hermes.kafka

import java.util.Properties

import com.typesafe.config.Config
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}


object KafkaProducer {


  def getInstance(config: Config) : KafkaProducer[AnyRef, AnyRef]= {
    val props: Properties = new Properties()
    props.put("metadata.broker.list", config.getString("brokerList"))
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("bootstrap.servers", config.getString("servers"))
    props.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer")
    props.put("request.requieres.acks", "1")
    new KafkaProducer(props)
  }

  def send(producer: KafkaProducer[AnyRef, AnyRef], topic: String, message: String): Unit = {
    val record = new ProducerRecord[AnyRef, AnyRef](topic, message)
    producer.send(record)
  }
  def close(producer: KafkaProducer[AnyRef, AnyRef]): Unit = producer.close()

}