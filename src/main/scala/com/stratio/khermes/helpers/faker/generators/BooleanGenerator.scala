package com.stratio.khermes.helpers.faker.generators

import scala.util.Random

/**
  * Created by e049627 on 15/11/17.
  */
class BooleanGenerator {

  /*
  As simple as this
   */
  def random: Boolean = {
    Random.nextBoolean()
  }
}
