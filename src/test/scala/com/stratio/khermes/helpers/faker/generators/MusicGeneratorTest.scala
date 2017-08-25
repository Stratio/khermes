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
class MusicGeneratorTest extends FlatSpec
  with Matchers {


  "A Khermes" should "should generate valid music: with EN and ES locales" in {
    val khermesEN = Faker("EN")
    khermesEN.Music.getMusic(khermesEN.Music.musicModel) should contain(khermesEN.Music.playedSong)

    val khermesES = Faker("ES")
    khermesES.Music.getMusic(khermesES.Music.musicModel) should contain(khermesES.Music.playedSong)
  }

  it should "raise a NoSuchElementException when the music locale is empty" in {
    val khermes = Faker("XX")
    an[KhermesException] should be thrownBy khermes.Music.playedSong
  }

  it should "when you do not specify any locale try to use all the locales" in {
    val khermes = Faker()
    khermes.Music.getMusic(khermes.Music.musicModel) should contain(khermes.Music.playedSong)
  }

  it should "raise an exception when it gets a song that not exists" in {
    val khermesFR = Faker("FR")
    an[KhermesException] should be thrownBy khermesFR.Music.playedSong
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
