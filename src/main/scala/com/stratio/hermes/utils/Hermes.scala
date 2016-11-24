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

package com.stratio.hermes.utils

import com.stratio.hermes.constants.HermesConstants
import com.stratio.hermes.helpers.RandomHelper
import com.stratio.hermes.implicits.HermesSerializer
import com.stratio.hermes.models.NameModel
import org.json4s._
import org.json4s.native.Serialization.read

/**
 * Hermes util used for to generate random values.
 */
case class Hermes(locale: String = HermesConstants.ConstantDefaultLocale) extends HermesSerializer {

  /**
   * Generates random names.
   */
  object Name extends HermesUnit {

    override def unitName() = "name"

    lazy val nameModel =
      read[NameModel] (getClass.getResourceAsStream(s"/locales/$unitName/$locale.json"))

    /**
     * Example: "Bruce Wayne".
     * @return a full name.
     */
    def name() : String = s"$firstName $lastName"

    /**
     * Example: "Bruce Lex".
     * @return a midle name.
     */
    def midleName() : String = s"$firstName $firstName"

    /**
     * Example: "Bruce".
     * @return a first name.
     */
    def firstName() : String =
      RandomHelper.randomElementFromAList[String](nameModel.firstNames).getOrElse(throw new NoSuchElementException)

    /**
     * Example: "Wayne".
     * @return a last name.
     */
    def lastName() : String =
      RandomHelper.randomElementFromAList[String](nameModel.lastNames).getOrElse(throw new NoSuchElementException)
  }
}
