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
