/**
 * © 2017 Stratio Big Data Inc., Sucursal en España.
 *
 * This software is licensed under the Apache 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the terms of the License for more details.
 *
 * SPDX-License-Identifier:  Apache-2.0.
 */
package com.stratio.khermes.helpers.faker.generators

import com.stratio.khermes.commons.exceptions.KhermesException
import com.stratio.khermes.helpers.faker.Faker
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class EmailGeneratorTest extends FlatSpec
  with Matchers {

  "Khermes" should "generate valid email address from a full name" in {
    val khermes = Faker("EN")
    val address = khermes.Email.address("  John Doe")
    val domain: String = address.split("@")(1)

    address should startWith("jdoe")
    khermes.Email.domains(khermes.Email.emailModel) should contain(domain)

  }


  "Khermes" should "fail when no domain exists" in {
    val khermes = Faker("XX")
    an[KhermesException] should be thrownBy khermes.Email.address("sample name")
  }

  "Khermes" should "fail when name is invalid" in {
    val khermes = Faker("EN")
    an[KhermesException] should be thrownBy khermes.Email.address(" ")
  }

  "Khermes" should "generate valid email using all locates" in {
    val khermes = Faker()
    val address = khermes.Email.address("  John Doe")
    val domain: String = address.split("@")(1)

    address should startWith("jdoe")
    khermes.Email.domains(khermes.Email.emailModel) should contain(domain)
  }



}
