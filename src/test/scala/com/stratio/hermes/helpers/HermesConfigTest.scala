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

import com.stratio.hermes.utils.HermesLogging
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class HermesConfigTest  extends FlatSpec with Matchers with BeforeAndAfter with HermesLogging  {

  val hermesConfig =
    """
      |hermes {
      |  templates-path = "/some/test/path"
      |  template-name = "someTemplate"
      |  topic = "someTopic"
      |  i18n = "EN"
      |}
    """.stripMargin

  val jsonKafkaConfig =
    """
      |kafka {
      |  bootstrap.servers = "localhost:9092"
      |  acks = "-1"
      |  key.serializer = "org.apache.kafka.common.serialization.StringSerializer"
      |  value.serializer = "org.apache.kafka.common.serialization.StringSerializer"
      |}
    """.stripMargin

  val avroKafkaConfig =
    """
      |kafka {
      |  bootstrap.servers = "localhost:9092"
      |  acks = "-1"
      |  key.serializer = "io.confluent.kafka.serializers.KafkaAvroSerializer"
      |  value.serializer = "io.confluent.kafka.serializers.KafkaAvroSerializer"
      |  schema.registry.url = "http://localhost:16803"
      |}
    """.stripMargin

  val wrongHermesConfig =
    """
      |hermes {
      |  templates-path = "/some/test/path"
      |  template-name = "someTemplate"
      |  i18n = "EN"
      |}
    """.stripMargin

  val wrongKafkaConfig =
    """
      |kafka {
      |  bootstrap.servers = "localhost:9092"
      |  acks = "-1"
      |  key.serializer = "io.confluent.kafka.serializers.KafkaAvroSerializer"
      |  value.serializer = "io.confluent.kafka.serializers.KafkaAvroSerializer"
      |}
    """.stripMargin

  val template =
    """
      |@import com.stratio.hermes.utils.Hermes
      |
      |@(hermes: Hermes)
      |{
      |  "name" : "@(hermes.Name.firstName)"
      |}
    """.stripMargin

  val avroSchema =
    """
      |{
      |  "type": "record",
      |  "name": "myrecord",
      |  "fields":
      |    [
      |      {"name": "name", "type":"int"}
      |    ]
      |}
    """.stripMargin

  "An HermesConfig" should "parse a correct config when serializer is Json" in {
    val hc = HermesConfig(hermesConfig, jsonKafkaConfig, template)
    checkCommonFields(hc)
    hc.configType should be(HermesConfig.ConfigType.Json)
    hc.avroSchema should be(None)
  }

  it should "parse a correct config when the serializer is Avro" in {
    val hc = HermesConfig(hermesConfig, avroKafkaConfig, template, Option(avroSchema))
    checkCommonFields(hc)
    hc.configType should be(HermesConfig.ConfigType.Avro)
    hc.avroSchema should be(Option(avroSchema))
  }

  it should "throw an error when a mandatory field is not supplied when the serializer is JSON" in {
    a[AssertionError] should be thrownBy {
      HermesConfig(wrongHermesConfig, jsonKafkaConfig, template)
    }
  }

  it should "throw an error when a mandatory field is not supplied when the serializer is Avro" in {
    a[AssertionError] should be thrownBy {
      HermesConfig(hermesConfig, wrongKafkaConfig, template)
    }
  }

  private[this] def checkCommonFields(hc: HermesConfig): Unit = {
    hc.hermesI18n should be("EN")
    hc.templateContent should be(template)
    hc.templateName should be("someTemplate")
    hc.template should be(template)
    hc.topic should be("someTopic")
  }
}
