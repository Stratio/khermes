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

import com.stratio.hermes.helpers.ResourcesHelper.getResource
import com.stratio.hermes.implicits.HermesSerializer
import org.json4s.native.Serialization.read

import scala.util.{Failure, Success, Try}

object ParserHelper extends HermesSerializer {

  def parse[T](unitName: String, locale: String)(implicit m: Manifest[T]): Either[String, T] = Try(
    read[T](getResource(unitName, locale))) match {
    case Success(model) => Right(model)
    case Failure(e) => Left(s"${e.getMessage}")
  }

  def parseErrors[T](maybeResources: Seq[Either[String, T]]): Seq[String] = {
    maybeResources.filter(_.isLeft).map(_.left.get)
  }
}
