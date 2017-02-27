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

package com.stratio.hermes.helpers

import java.io.InputStream

import com.stratio.hermes.exceptions.HermesException
import com.stratio.hermes.helpers.ParserHelper._
import com.stratio.hermes.helpers.ResourcesHelper._
import com.stratio.hermes.utils.Hermes
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}


@RunWith(classOf[JUnitRunner])
class ResourcesHelperTest extends FlatSpec
  with Matchers {

  "getResources" should "return a valid seq of files when path exists" in {
    getResources("music") should not be empty
  }

  "getResources" should "return an exception when path does not exist" in {
    intercept[HermesException] {
      getResources("no-valid")
    }
  }

  "getResource" should "return a valid inputStream " in {
    getResource("music", "EN.json") shouldBe a[InputStream]
  }

  "getResource" should "return an exception when the resource does not exist" in {
    intercept[HermesException] {
      getResource("no-valid-name", "EN.json")
    }
  }
}
