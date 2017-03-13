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

package com.stratio.khermes.utils.generators

import com.stratio.khermes.exceptions.KhermesException
import com.stratio.khermes.helpers.ParserHelper
import com.stratio.khermes.utils.Khermes
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class GeoGeneratorTest extends FlatSpec
  with Matchers {


  it should "generate valid locations: ES and US locales" in {

    val khermesES = Khermes("ES")
    khermesES.Geo.geoModelList(khermesES.Geo.geoModel) should contain(khermesES.Geo.geolocation)

    val khermesUS = Khermes("US")
    khermesUS.Geo.geoModelList(khermesUS.Geo.geoModel) should contain(khermesUS.Geo.geolocation)
  }

  it should "raise a NoSuchElementException when the locale is empty" in {
    val khermes = Khermes("XX")
    an[KhermesException] should be thrownBy khermes.Geo.geolocation
  }

  it should "when you do not specify the locale try to use all the locales" in {
    val khermes = Khermes()
    khermes.Geo.geoModelList(khermes.Geo.geoModel) should contain(khermes.Geo.geolocation)
  }

  it should "raise an exception when it gets a geolocation that not exists" in {
    val khermesFR = Khermes("FR")
    an[KhermesException] should be thrownBy khermesFR.Geo.geolocation
    an[KhermesException] should be thrownBy khermesFR.Geo.city()
    an[KhermesException] should be thrownBy khermesFR.Geo.geolocationWithoutCity()
  }

  it should "generate a random city" in {
    val khermes = Khermes()
    khermes.Geo.cityList(khermes.Geo.geoModel) should contain(khermes.Geo.city)
  }

  it should "generate a random geolocation without city" in {
    val khermes = Khermes()
    khermes.Geo.geoWithoutCityList(khermes.Geo.geoModel) should contain(khermes.Geo.geolocationWithoutCity())
  }

  it should "raise an exception when it gets a geolocation that is corrupted" in {
    val khermesYY = Khermes("YY")
    ParserHelper.parseErrors(khermesYY.Geo.geoModel).length should be(1)
    an[KhermesException] should be thrownBy khermesYY.Geo.geolocation
  }

  it should "raise an exception when it gets a file with at least one record corrupted" in {
    val khermes = Khermes()
    ParserHelper.parseErrors(khermes.Geo.geoModel).length should be(2)
  }

}
