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
import com.stratio.khermes.helpers.faker.Faker
import com.stratio.khermes.helpers.twirl.TwirlHelper.CompilationError
import com.typesafe.scalalogging.LazyLogging
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import play.twirl.api.Txt

@RunWith(classOf[JUnitRunner])
class TwirlHelperTest extends FlatSpec
  with Matchers
  with BeforeAndAfter
  with LazyLogging {

  implicit val config = com.stratio.khermes.commons.implicits.AppImplicits.config

  before {
    Khermes.createPaths
  }

  "A TwirlHelper" should "compile a simple template without object injection" in {
    val template = "Hello world"
    val result = cleanContent(TwirlHelper.template[() => Txt](template, "templateTest").static().toString())
    result should be("Hello world")
  }

  it should "compile a template and inject a string" in {
    val template =
      """
        |@(name: String)
        |Hello @name
      """.stripMargin
    val result = cleanContent(TwirlHelper.template[(String) => Txt](template, "templateTest").static("Neo").toString())
    result should be("Hello Neo")
  }

  it should "compile a template and inject an khermes helper" in {
    val template =
      """
        |@(faker: Faker)
        |Hello @(faker.Name.firstName)
      """.stripMargin

    val khermes = new Faker("EN")

    val result = cleanContent(
      TwirlHelper.template[(Faker) => Txt](template, "templateTest").static(khermes).toString())
    result should fullyMatch regex """Hello [a-zA-Z]+"""
  }

  it should "throw an error when the template is wrong" in {
    val template = "Hello @(error)"
    //scalastyle:off
    the[CompilationError] thrownBy (TwirlHelper.template[() => Txt]
      (template, "templateTest").static().toString()) should have('line (1), 'column (8))
    //scalastyle:on
  }

  /**
   * Cleans the content deleting return carriages an not necessary spaces.
   * @param content with the original content.
   * @return a sanitized content.
   */
  def cleanContent(content: String): String = content.replace("\n", "").replaceAll("  +", "")
}
