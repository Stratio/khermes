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
package com.stratio.khermes.helpers.twirl

import com.stratio.khermes.Khermes
import com.stratio.khermes.commons.exceptions.KhermesException
import com.stratio.khermes.helpers.faker.Faker
import com.stratio.khermes.helpers.twirl.TwirlHelper.CompilationError
import com.typesafe.scalalogging.LazyLogging
import org.json4s.JsonAST.JValue
import org.json4s.{DefaultFormats, Formats}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import play.twirl.api.{Html, Txt}
import org.json4s.native.Serialization._

@RunWith(classOf[JUnitRunner])
class TwirlHelperTest
    extends FlatSpec
    with Matchers
    with BeforeAndAfter
    with LazyLogging {

  implicit val config =
    com.stratio.khermes.commons.implicits.AppImplicits.config

  before {
    Khermes.createPaths
  }

  "A TwirlHelper" should "compile a simple template without object injection" in {
    val template = "Hello world"
    val result = cleanContent(
      TwirlHelper
        .template[() => Txt](template, "templateTest")
        .static()
        .toString())
    result should be("Hello world")
  }

  it should "compile a template and inject a string" in {
    val template =
      """
        |@(name: String)
        |Hello @name
      """.stripMargin
    val result = cleanContent(
      TwirlHelper
        .template[(String) => Txt](template, "templateTest")
        .static("Neo")
        .toString())
    result should be("Hello Neo")
  }

  it should "throw an error when the template is wrong" in {
    val template = "Hello @(error)"
    the[CompilationError] thrownBy TwirlHelper
      .template[() => Txt](template, "templateTest")
      .static()
      .toString() should have('line (1), 'column (8))
  }

  /**
    * Cleans the content deleting return carriages an not necessary spaces.
    * @param content with the original content.
    * @return a sanitized content.
    */
  def cleanContent(content: String): String =
    content.replace("\n", "").replaceAll("  +", "")

  it should "compile a template and inject an khermes helper" in {
    val template =
      """
        |@(faker: Faker)
        |Hello @(faker.Name.firstName)
      """.stripMargin

    val khermes = new Faker("EN")

    val result = cleanContent(
      TwirlHelper
        .template[(Faker) => Txt](template, "templateTest")
        .static(khermes)
        .toString())
    result should fullyMatch regex """Hello [a-zA-Z]+"""
  }

  /**
    * Test for categoric fields generator
    * @author: Emiliano Martinez
    */
  it should "compile a categoric generator" in {

    import com.stratio.khermes.helpers.faker.generators._

    val template =
      """
        |@import com.stratio.khermes.helpers.faker.generators._
        |
        |@(generator: CategoryGenerator)
        |@defining(generator.runNext(List(CategoryFormat("33","0.55"), CategoryFormat("34","0.45")))) { case(gen) =>
        |@gen
        |}
      """.stripMargin

    val result = cleanContent(
      TwirlHelper
        .template[(CategoryGenerator) => Txt](template, "templateTest")
        .static(CategoryGenerator())
        .toString())
    List(result) should contain oneOf ("34", "33")
  }


  it should "compile a categoric generator with bad specification" in {
    import com.stratio.khermes.helpers.faker.generators._

    val template =
      """
        |@import com.stratio.khermes.helpers.faker.generators._
        |
        |@(generator: CategoryGenerator)
        |@defining(generator.runNext(List(CategoryFormat("33","0.55")))) { case(gen) =>
        |@gen
        |}
      """.stripMargin

    an[KhermesException] should be thrownBy cleanContent(
      TwirlHelper
        .template[(CategoryGenerator) => Txt](template, "templateTest")
        .static(CategoryGenerator())
        .toString())
  }


  case class ToCheck(acquirer: String)

  it should "generate a new json text with categoric values" in {
    import com.stratio.khermes.helpers.faker.generators._

    implicit val json4sFormats: Formats = DefaultFormats

    val template =
      """
        |@import com.stratio.khermes.helpers.faker.generators._
        |
        |@(generator: CategoryGenerator)
        |@defining(generator) { case(gen) =>
        |{
        | "acquirer": "@gen.runNext(List(
        |  CategoryFormat("VISA", "0.1"),
        |  CategoryFormat("MC", "0.9")
        |  ))"
        |}
        |}
      """.stripMargin

    val result = cleanContent(
      TwirlHelper
        .template[(CategoryGenerator) => Txt](template, "templateTest")
        .static(CategoryGenerator())
        .toString())

    List(read[ToCheck](result).acquirer) should contain oneOf ("VISA", "MC")
  }

  /**
    * Test for Gaussian based generator
    * @author: Emiliano Martinez
    */
  it should "generate a random value from the normal distribution given mu and sigma" in {
    val template =
      """
        |@(faker: Faker)
        |@(faker.Gaussian.runNext(5.0, 1.5))
      """.stripMargin

    val khermes = new Faker("EN")

    val beWithinTolerance = be >= 0.0 and be <= 3.5

    val result = cleanContent(
      TwirlHelper
        .template[(Faker) => Txt](template, "templateTest")
        .static(khermes)
        .toString())

    Math.abs(((result.toFloat - 5.0)/1.5)) should beWithinTolerance
  }

  it should "generate a CSV valid file" in {

   val template =
     """
       |@(faker: Faker)Element1,Element2
       |@for(p <- 0 to 2) {@(faker.Gaussian.runNext(5.0, 1.5)),@(faker.Gaussian.runNext(5.0, 1.5))
       |}
       |""".stripMargin

    val khermes = new Faker("EN")

    val result =
      TwirlHelper
        .template[(Faker) => Txt](template, "templateTest")
        .static(khermes)
        .toString()

    result.split("\n")(0).trim shouldBe "Element1,Element2"
    result.split("\n")(1).trim.split(',').length shouldBe 2
  }

  it should "generate a JSON valid file" in {

    import org.json4s._
    import org.json4s.native.JsonMethods._

    val template =
      """
        |@(faker: Faker)[@for(p <- 0 to 1) {{"Element1":@(faker.Gaussian.runNext(5.0, 1.5)),"Element2":@(faker.Gaussian.runNext(5.0, 1.5))}@(if(p < 1) "," else "")}]
        |""".stripMargin

    val khermes = new Faker("EN")

    val result =
      TwirlHelper
        .template[(Faker) => Txt](template, "templateTest")
        .static(khermes)
        .toString()

    val unmarshalled = parse(result)
    unmarshalled shouldBe a [JValue]
  }


}
