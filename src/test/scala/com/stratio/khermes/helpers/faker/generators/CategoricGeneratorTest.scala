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
package com.stratio.khermes.helpers.faker.generators

import com.stratio.khermes.commons.exceptions.KhermesException
import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CategoricGeneratorTest extends FlatSpec
  with Matchers {

  it should "return a category from a category list" in {
    val categoricGene = CategoryGenerator()
    val categoricList = List(CategoryFormat("VISA", "1"))
    categoricGene.runNext(categoricList) should be("VISA")
  }

  it should "throw an exception when the sum of probabilities are less tha one" in {
    val categoricGene = CategoryGenerator()
    val categoricList = List(CategoryFormat("VISA", "0.5"))
    an[KhermesException] should be thrownBy categoricGene.runNext(categoricList)
  }

}
