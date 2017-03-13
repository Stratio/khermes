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

package com.stratio.khermes.helpers

import java.io.{File, InputStream}

import com.stratio.khermes.exceptions.KhermesException
import com.stratio.khermes.implicits.KhermesSerializer

import scala.util.{Failure, Success, Try}

object ResourcesHelper extends KhermesSerializer {

  def getResources(name: String): Seq[String] = Try(
    new File(getClass.getResource(s"/locales/$name").getFile)) match {
    case Success(resources) => resources.list().toSeq
    case Failure(_) => throw new KhermesException(s"Error loading invalid name /locales/$name")
  }

  def getResource(name: String, file: String): InputStream = Option(
    getClass.getResourceAsStream(s"/locales/$name/$file")).getOrElse(
    throw new KhermesException(s"Error loading invalid resource /locales/$name/$file"))
}
