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

import com.stratio.khermes.commons.constants.AppConstants
import com.stratio.khermes.commons.exceptions.KhermesException
import com.stratio.khermes.commons.implicits.AppSerializer
import com.stratio.khermes.helpers.faker.FakerGenerator
import com.typesafe.scalalogging.LazyLogging

case class MusicGenerator(locale: String) extends FakerGenerator
  with AppSerializer
  with LazyLogging {

  override def name: String = "music"

  lazy val musicModel = locale match {
    case AppConstants.DefaultLocale => {
      val resources = getResources(name)
        .map(parse[Seq[MusicModel]](name, _))
      if (parseErrors[Seq[MusicModel]](resources).nonEmpty) logger.warn(s"${parseErrors[Seq[MusicModel]](resources)}")
      resources
    }
    case localeValue => Seq(parse[Seq[MusicModel]](name, s"$localeValue.json"))
  }


  def getMusic(maybeResources: Seq[Either[String, Seq[MusicModel]]]): Seq[MusicModel] =
    maybeResources.filter(_.isRight).flatMap(_.right.get)

  def playedSong: MusicModel =
    randomElementFromAList[MusicModel](getMusic(musicModel)).getOrElse(
      throw new KhermesException(s"Error loading locate /locales/$name/$locale.json"))
}

case class MusicModel(song: String, artist: String, album: String, genre: String)
