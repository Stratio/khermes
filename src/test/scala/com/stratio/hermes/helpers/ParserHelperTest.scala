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

import com.stratio.hermes.exceptions.HermesException
import com.stratio.hermes.helpers.ParserHelper._
import com.stratio.hermes.models.MusicModel
import com.stratio.hermes.utils.Hermes
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
    val hermesYY = Hermes("YY")
    parseErrors(hermesYY.Music.musicModel).length should be(1)
    an[HermesException] should be thrownBy hermesYY.Music.playedSong
  }

  it should "raise an exception when it gets a file with at least one song corrupted" in {
    val hermes = Hermes()
    parseErrors(hermes.Music.musicModel).length should be(2)
  }
}
