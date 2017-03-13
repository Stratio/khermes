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

import com.stratio.khermes.exceptions.KHermesException
import com.stratio.khermes.helpers.ParserHelper._
import com.stratio.khermes.models.MusicModel
import com.stratio.khermes.utils.KHermes
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{EitherValues, FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class ParserHelperTest extends FlatSpec
  with Matchers
  with EitherValues {

  "parser" should "return Left when a error occurs during parse process" in {
    parse("no-valid-name", "ES").left.value should be("Error loading invalid resource /locales/no-valid-name/ES")
  }

  "parser" should "return Right when no errors occur during parse process" in {
    parse[Seq[MusicModel]]("music", "EN.json") should be('right)
    parse[Seq[MusicModel]]("music", "EN.json").right.value shouldBe a[Seq[_]]
  }

  it should "raise an exception when it gets a song that is corrupted" in {
    val khermesYY = KHermes("YY")
    parseErrors(khermesYY.Music.musicModel).length should be(1)
    an[KHermesException] should be thrownBy khermesYY.Music.playedSong
  }

  it should "raise an exception when it gets a file with at least one song corrupted" in {
    val khermes = KHermes()
    parseErrors(khermes.Music.musicModel).length should be(2)
  }
}
