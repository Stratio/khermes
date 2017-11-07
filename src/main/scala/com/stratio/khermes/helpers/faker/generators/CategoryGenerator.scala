package com.stratio.khermes.helpers.faker.generators

import com.stratio.khermes.commons.exceptions.KhermesException

import scala.math.BigDecimal.RoundingMode
import scala.util.Random

/**
  * Created by Emiliano Martínez on 14/10/17.
  */
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
      throw new KhermesException(s"Bad weight input values for sum ${BigDecimal(in.map(el => el.weight.toFloat).sum).setScale(2, RoundingMode.HALF_UP).toInt} and values ${in.map(el => el.value)}")
    }
  }
}
