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

import com.stratio.hermes.constants.HermesConstants
import com.stratio.hermes.exceptions.HermesException
import com.stratio.hermes.helpers.ParserHelper._
import com.stratio.hermes.helpers.RandomHelper
import com.stratio.hermes.helpers.ResourcesHelper._
import com.stratio.hermes.implicits.HermesSerializer
import com.stratio.hermes.models.MusicModel
import com.stratio.hermes.utils.{HermesLogging, HermesUnit}

case class MusicGenerator(locale: String) extends HermesUnit
  with HermesSerializer
  with HermesLogging {

  override def unitName: String = "music"

  lazy val musicModel = locale match {
    case HermesConstants.DefaultLocale => {
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
      throw new HermesException(s"Error loading locate /locales/$unitName/$locale.json"))
}
