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
package com.stratio.khermes.helpers

import java.io.InputStream

import com.stratio.khermes.commons.exceptions.KhermesException
import com.stratio.khermes.helpers.faker.generators.MusicModel
import com.stratio.khermes.helpers.faker.{Faker, FakerGenerator}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{EitherValues, FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class FakerGeneratorTest extends FlatSpec with FakerGenerator with Matchers with EitherValues {

  override def name: String = "test"

  "FakeGenerator" should "return Left when a error occurs during parse process" in {
    parse("no-valid-name", "ES").left.value should be("Error loading invalid resource /locales/no-valid-name/ES")
  }

  it should "return Right when no errors occur during parse process" in {
    parse[Seq[MusicModel]]("music", "EN.json") should be('right)
    parse[Seq[MusicModel]]("music", "EN.json").right.value shouldBe a[Seq[_]]
  }

  it should "raise an exception when it gets a song that is corrupted" in {
    val khermesYY = Faker("YY")
    parseErrors(khermesYY.Music.musicModel).length should be(1)
    an[KhermesException] should be thrownBy khermesYY.Music.playedSong
  }

  it should "raise an exception when it gets a file with at least one song corrupted" in {
    val khermes = Faker()
    parseErrors(khermes.Music.musicModel).length should be(2)
  }

  it should "generates an element from a generic list" in {
    randomElementFromAList(List(1, 2, 3)) should contain oneOf(1, 2, 3)
    randomElementFromAList(List("a", "b", "c")) should contain oneOf("a", "b", "c")
  }

  it should "return nothing when a generic list is empty" in {
    randomElementFromAList(List.empty) should be(None)
  }

  it should "return a valid seq of files when path exists" in {
    getResources("music") should not be empty
  }

  it should "return an exception when path does not exist" in {
    intercept[KhermesException] {
      getResources("no-valid")
    }
  }

  it should "return a valid inputStream " in {
    getResource("music", "EN.json") shouldBe a[InputStream]
  }

  it should "return an exception when the resource does not exist" in {
    intercept[KhermesException] {
      getResource("no-valid-name", "EN.json")
    }
  }

}
