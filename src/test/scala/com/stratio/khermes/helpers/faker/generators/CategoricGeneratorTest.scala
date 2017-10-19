package com.stratio.khermes.helpers.faker.generators

import com.stratio.khermes.commons.exceptions.KhermesException
import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.junit.JUnitRunner

/**
  * Created by e049627 on 17/10/17.
  */
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
