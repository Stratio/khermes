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
class EmailGeneratorTest extends FlatSpec
  with Matchers {

  "Hermes" should "generate valid email address from a full name" in {
    val hermes = Hermes("EN")
    val address = hermes.Email.address("  John Doe")
    val domain: String = address.split("@")(1)

    address should startWith("jdoe")
    hermes.Email.domains(hermes.Email.emailModel) should contain(domain)

  }


  "Hermes" should "fail when no domain exists" in {
    val hermes = Hermes("XX")
    an[HermesException] should be thrownBy hermes.Email.address("sample name")
  }

  "Hermes" should "fail when name is invalid" in {
    val hermes = Hermes("EN")
    an[HermesException] should be thrownBy hermes.Email.address(" ")
  }

  "hermes" should "generate valid email using all locates" in {
    val hermes = Hermes()
    val address = hermes.Email.address("  John Doe")
    val domain: String = address.split("@")(1)

    address should startWith("jdoe")
    hermes.Email.domains(hermes.Email.emailModel) should contain(domain)
  }



}
