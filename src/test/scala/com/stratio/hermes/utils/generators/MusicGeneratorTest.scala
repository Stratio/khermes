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
import com.stratio.hermes.models.MusicModel
import com.stratio.hermes.utils.Hermes
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class MusicGeneratorTest extends FlatSpec
  with Matchers {


  "A Hermes" should "should generate valid music: with EN and ES locales" in {
    val hermesEN = Hermes("EN")
    hermesEN.Music.getMusic(hermesEN.Music.musicModel) should contain(hermesEN.Music.playedSong)

    val hermesES = Hermes("ES")
    hermesES.Music.getMusic(hermesES.Music.musicModel) should contain(hermesES.Music.playedSong)
  }

  it should "raise a NoSuchElementException when the music locale is empty" in {
    val hermes = Hermes("XX")
    an[HermesException] should be thrownBy hermes.Music.playedSong
  }

  it should "when you do not specify any locale try to use all the locales" in {
    val hermes = Hermes()
    hermes.Music.getMusic(hermes.Music.musicModel) should contain(hermes.Music.playedSong)
  }

  it should "raise an exception when it gets a song that not exists" in {
    val hermesFR = Hermes("FR")
    an[HermesException] should be thrownBy hermesFR.Music.playedSong
  }

  "getMusic" should "return a seq with one music model" in {
    val generator = MusicGenerator("EN")
    generator.getMusic(Seq(Left("error"), Right(Seq(MusicModel("song", "artist", "album", "genre"))))) should be(
      Seq(MusicModel("song", "artist", "album", "genre")))
  }

  "getMusic" should "return empty seq when no music model exists" in {
    val generator = MusicGenerator("EN")
    generator.getMusic(Seq(Left("error"))) should be(Seq())
  }
}
