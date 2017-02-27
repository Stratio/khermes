/*
 * Copyright (C) 2016 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.hermes.utils.generators

import com.stratio.hermes.exceptions.HermesException
import com.stratio.hermes.utils.Hermes
import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class GeoGeneratorTest extends FlatSpec
  with Matchers {


  it should "generate valid locations: ES and US locales" in {

    val hermesES = Hermes("ES")
    hermesES.Geo.geoModelList(hermesES.Geo.geoModel) should contain(hermesES.Geo.geolocation)

    val hermesUS = Hermes("US")
    hermesUS.Geo.geoModelList(hermesUS.Geo.geoModel) should contain(hermesUS.Geo.geolocation)
  }

  it should "raise a NoSuchElementException when the locale is empty" in {
    val hermes = Hermes("XX")
    an[HermesException] should be thrownBy hermes.Geo.geolocation
  }

  it should "when you do not specify the locale try to use all the locales" in {
    val hermes = Hermes()
    hermes.Geo.geoModelList(hermes.Geo.geoModel) should contain(hermes.Geo.geolocation)
  }

  it should "raise an exception when it gets a geolocation that not exists" in {
    val hermesFR = Hermes("FR")
    an[HermesException] should be thrownBy hermesFR.Geo.geolocation
  }

}
