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


  it should "return always the same value of a list if have a weight of 1" in {
//    randomElementFromAList(Seq(0, 1, 2), Map(0 -> 1.0)) shouldBe Some(0)

  }

}
