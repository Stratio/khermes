package com.stratio.hermes.runners

import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class HermesRunnerTest extends FlatSpec with Matchers {

  "A HermesRunner" should "return a hello projectName message" in {

    HermesRunner.helloWorld("Hermes") should be ("Hello Hermes!")
  }
}
