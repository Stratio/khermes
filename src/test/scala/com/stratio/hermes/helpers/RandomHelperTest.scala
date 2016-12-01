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

import com.stratio.hermes.utils.HermesLogging
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class RandomHelperTest  extends FlatSpec with Matchers with HermesLogging {

  "A RandomHelper" should "generates an element from a generic list" in {
    RandomHelper.randomElementFromAList(List(1,2,3)) should contain oneOf (1,2,3)
    RandomHelper.randomElementFromAList(List("a","b","c")) should contain oneOf ("a","b","c")
  }

  it should "return nothing when a generic list is empty" in {
    RandomHelper.randomElementFromAList(List.empty) should be (None)
  }
}
