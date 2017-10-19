package com.stratio.khermes.helpers.faker.generators

import breeze.stats.distributions.Gaussian

/**
  * Created by Emiliano Martínez on 14/10/17.
  */
case class GaussianDistGenerator() {
  /**
    * It uses Breeze for generating random float numbers using a Gaussian distribution
    * @param mu mean
    * @param sigma standard deviation
    * @return random value
    */
  def runNext(mu: Double, sigma: Double) : Double = {
    Gaussian.distribution(mu, sigma).get()
  }
}
