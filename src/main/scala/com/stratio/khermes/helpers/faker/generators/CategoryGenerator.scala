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

import scala.math.BigDecimal.RoundingMode
import scala.util.Random

case class CategoryFormat(value: String, weight: String)

case class CategoryGenerator() {

  /**
    * Function which generates a random value from a list whose elements are categorized
    * @param in list of possible values with their appearence probabilities
    * @return value created
    */
  def runNext(in: List[CategoryFormat]): String = {
    if (BigDecimal(in.map(el => el.weight.toFloat).sum).setScale(2, RoundingMode.HALF_UP).toInt == 1) {
      val buffer = in.foldLeft(List[String]())((acc, cat) =>
        List.fill((cat.weight.toFloat * 100).toInt)(cat.value) ++ acc)
      /**
       * TODO: Persist these structures, i.e in one actor if it was neccesary for performance reasons.
       */
      buffer(Random.nextInt(buffer.size - 1))
    } else {
      val e1 = BigDecimal(in.map(el => el.weight.toFloat).sum).setScale(2, RoundingMode.HALF_UP).toInt
      val e2 = in.map(el => el.value)
      throw new KhermesException(s"Bad weight input values for sum ${e1} and values ${e2}")
    }
  }
}