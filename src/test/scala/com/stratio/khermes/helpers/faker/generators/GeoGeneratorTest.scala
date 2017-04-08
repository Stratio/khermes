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

package com.stratio.khermes.helpers.faker.generators

import com.stratio.khermes.commons.exceptions.KhermesException
import com.stratio.khermes.helpers.faker.{FakerGenerator, Faker}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class GeoGeneratorTest extends FlatSpec with FakerGenerator with Matchers {

  override def name: String = "test"

  it should "generate valid locations: ES and US locales" in {

    val khermesES = Faker("ES")
    khermesES.Geo.geoModelList(khermesES.Geo.geoModel) should contain(khermesES.Geo.geolocation)

    val khermesUS = Faker("US")
    khermesUS.Geo.geoModelList(khermesUS.Geo.geoModel) should contain(khermesUS.Geo.geolocation)
  }

  it should "raise a NoSuchElementException when the locale is empty" in {
    val khermes = Faker("XX")
    an[KhermesException] should be thrownBy khermes.Geo.geolocation
  }

  it should "when you do not specify the locale try to use all the locales" in {
    val khermes = Faker()
    khermes.Geo.geoModelList(khermes.Geo.geoModel) should contain(khermes.Geo.geolocation)
  }

  it should "raise an exception when it gets a geolocation that not exists" in {
    val khermesFR = Faker("FR")
    an[KhermesException] should be thrownBy khermesFR.Geo.geolocation
    an[KhermesException] should be thrownBy khermesFR.Geo.city()
    an[KhermesException] should be thrownBy khermesFR.Geo.geolocationWithoutCity()
  }

  it should "generate a random city" in {
    val khermes = Faker()
    khermes.Geo.cityList(khermes.Geo.geoModel) should contain(khermes.Geo.city)
  }

  it should "generate a random geolocation without city" in {
    val khermes = Faker()
    khermes.Geo.geoWithoutCityList(khermes.Geo.geoModel) should contain(khermes.Geo.geolocationWithoutCity())
  }

  it should "raise an exception when it gets a geolocation that is corrupted" in {
    val khermesYY = Faker("YY")
    parseErrors(khermesYY.Geo.geoModel).length should be(1)
    an[KhermesException] should be thrownBy khermesYY.Geo.geolocation
  }

  it should "raise an exception when it gets a file with at least one record corrupted" in {
    val khermes = Faker()
    parseErrors(khermes.Geo.geoModel).length should be(2)
  }
}
