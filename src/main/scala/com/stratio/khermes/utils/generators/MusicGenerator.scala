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

package com.stratio.khermes.utils.generators

import com.stratio.khermes.constants.KHermesConstants
import com.stratio.khermes.exceptions.KHermesException
import com.stratio.khermes.helpers.ParserHelper._
import com.stratio.khermes.helpers.RandomHelper
import com.stratio.khermes.helpers.ResourcesHelper._
import com.stratio.khermes.implicits.KHermesSerializer
import com.stratio.khermes.models.MusicModel
import com.stratio.khermes.utils.{KHermesLogging, KHermesUnit}

case class MusicGenerator(locale: String) extends KHermesUnit
  with KHermesSerializer
  with KHermesLogging {

  override def unitName: String = "music"

  lazy val musicModel = locale match {
    case KHermesConstants.DefaultLocale => {
      val resources = getResources(unitName)
        .map(parse[Seq[MusicModel]](unitName, _))
      if (parseErrors[Seq[MusicModel]](resources).nonEmpty) log.warn(s"${parseErrors[Seq[MusicModel]](resources)}")
      resources
    }
    case localeValue => Seq(parse[Seq[MusicModel]](unitName, s"$localeValue.json"))
  }


  def getMusic(maybeResources: Seq[Either[String, Seq[MusicModel]]]): Seq[MusicModel] =
    maybeResources.filter(_.isRight).flatMap(_.right.get)

  def playedSong: MusicModel =
    RandomHelper.randomElementFromAList[MusicModel](getMusic(musicModel)).getOrElse(
      throw new KHermesException(s"Error loading locate /locales/$unitName/$locale.json"))
}
