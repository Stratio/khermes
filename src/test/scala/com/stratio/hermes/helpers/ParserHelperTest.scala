
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
    parse[Seq[MusicModel]]("music", "EN.json").right.value shouldBe a[Seq[MusicModel]]
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
