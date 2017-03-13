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

import com.stratio.khermes.exceptions.KHermesException
import com.stratio.khermes.utils.KHermes
import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EmailGeneratorTest extends FlatSpec
  with Matchers {

  "KHermes" should "generate valid email address from a full name" in {
    val khermes = KHermes("EN")
    val address = khermes.Email.address("  John Doe")
    val domain: String = address.split("@")(1)

    address should startWith("jdoe")
    khermes.Email.domains(khermes.Email.emailModel) should contain(domain)

  }


  "KHermes" should "fail when no domain exists" in {
    val khermes = KHermes("XX")
    an[KHermesException] should be thrownBy khermes.Email.address("sample name")
  }

  "KHermes" should "fail when name is invalid" in {
    val khermes = KHermes("EN")
    an[KHermesException] should be thrownBy khermes.Email.address(" ")
  }

  "KHermes" should "generate valid email using all locates" in {
    val khermes = KHermes()
    val address = khermes.Email.address("  John Doe")
    val domain: String = address.split("@")(1)

    address should startWith("jdoe")
    khermes.Email.domains(khermes.Email.emailModel) should contain(domain)
  }



}
