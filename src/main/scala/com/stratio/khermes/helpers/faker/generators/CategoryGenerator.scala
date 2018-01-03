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


case class StringFake() {

  def run(): String = {

    val company = CategoryGenerator().runNext(List(CategoryFormat("TELCEL", "0.82"), CategoryFormat("MOVISTAR", "0.08"), CategoryFormat("IUSACELL", "0.07"), CategoryFormat("NEXTEL", "0.01"), CategoryFormat("0", "0.01"), CategoryFormat("UNEFON", "0.01")))
    val state = CategoryGenerator().runNext(List(CategoryFormat("ZAPOPAN JAL", "0.7"), CategoryFormat("HUIXQUILUCAN", "0.05"), CategoryFormat("GUADALAJARA", "0.05"), CategoryFormat("CHIHUAHUA", "0.1"), CategoryFormat("OTROS", "0.1")))

    def hire(company: String, state: String): Boolean = {
      val b = if (company == "TELCEL" && state == "ZAPOPAN JAL") true
      else if (company == "MOVISTAR" && state == "GUADALAJARA") true
      else if (company == "IUSACELL" && (state == "GUADALAJARA" || state == "HUIXQUILUCAN")) true
      else false
      b
    }

    val companyStr =
      s""""customerPhoneCompany":"${company}"""".stripMargin

      val stateStr = s""""transactionState": "${state}""""
      val hireStr = s""""hire":${hire(company, state)}"""

      val h = """"hire""""+ ":" + hire(company, state)

      s"""${companyStr},${h},${stateStr}"""
    }

}