package com.stratio.hermes.kafka

import java.util.Properties
import java.util

import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}




@RunWith(classOf[JUnitRunner])
class KafkaProducerTest extends FlatSpec with Matchers {

  val props = new Properties
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("bootstrap.servers", "localhost:9092")
    props.put("enable.auto.commit", "true")
    props.put("group.id", "0")
    props.put("auto.commit.interval.ms", "1000")
    props.put("session.timeout.ms", "30000")

  "A KafkaProducer" should "Produce a message with a json" in {

    val kafkaProducer=KafkaProducer.getInstance(ConfigFactory.parseResources("kafka.conf"))


//    for (line <- Source.fromFile("/home/eruiz/Proyectos/Hermes/src/test/resources/test").getLines){
    val consumer = new KafkaConsumer(props)
    consumer.subscribe(util.Arrays.asList("test"))

    val records1 = consumer.poll(1000)
    records1.count() shouldEqual 0

    KafkaProducer.send(kafkaProducer,"test","{\"name\":\"amparo\"}")
    val records2 = consumer.poll(1000)
    records2.count() shouldEqual 1

    kafkaProducer.close()
    consumer.close()


    //    KafkaProducer.send(kafkaProducer,"topic2",Source.fromFile("/home/eruiz/Proyectos/Hermes/src/test/resources/test").getLines())


  }
}