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

package com.stratio.khermes.helpers.faker

import java.io.{File, InputStream}

import com.stratio.khermes.commons.constants.AppConstants
import com.stratio.khermes.commons.exceptions.KhermesException
import com.stratio.khermes.commons.implicits.AppSerializer
import com.stratio.khermes.helpers.faker.generators._
import org.jliszka.probabilitymonad.Distribution
import org.json4s.native.Serialization._

import scala.util.{Failure, Random, Success, Try}

/**
 * Khermes util used for to generate random values.
 */
case class Faker(locale: String = AppConstants.DefaultLocale, strategy: Map[String, Double] = AppConstants.DefaultStrategy) extends AppSerializer {

  object Name extends NameGenerator(locale, strategy)

  object Number extends NumberGenerator

  object Geo extends GeoGenerator(locale)

  object Datetime extends DatetimeGenerator

  object Music extends MusicGenerator(locale)

  object Email extends EmailGenerator(locale)

}

trait FakerGenerator extends AppSerializer {

  def name: String

  /**
   * Returns a random element from a list.
   * @param list initial list
   * @tparam T with the type of the list
   * @return a random element of the list or None if the list is empty.
   */
  def randomElementFromAList[T](list: Seq[T]): Option[T] =
    if (list.nonEmpty) Option(list(Random.nextInt((list.size - 1) + 1))) else None

  def randomElementFromAListWithWeight[T](list: Seq[T], weight: Map[T, Double]): Option[T] = {

    if (list.nonEmpty) {
      val l: Seq[(T, Double)] = listWithWeight(list, weight)
      val distribution: Distribution[T] = Distribution.discrete(l: _*)
      Option(distribution.sample(100).head)
    } else {
      None
    }
  }

  def listWithWeight[T](list: Seq[T], weight: Map[T, Double]): Seq[(T, Double)] = {
    list.map(x => if (weight.contains(x)) {
      x -> weight(x)
    }
    else {
      x -> ((1 - weight.values.sum) / (list.size - weight.size))
    }).toSeq
  }


  def repeatElementsInList[T](list: Seq[(T, Double)]): Seq[T] = {
    list.flatMap(x =>
      Seq.fill((x._2 * 1000).toInt)(x._1)
    )
  }

  def getResources(name: String): Seq[String] = Try(
    new File(getClass.getResource(s"/locales/$name").getFile)) match {
    case Success(resources) => resources.list().toSeq
    case Failure(_) => throw new KhermesException(s"Error loading invalid name /locales/$name")
  }
//  def getResources(name: String, weight: Map[String, Double]): Seq[String] = Try(
//    new File(getClass.getResource(s"/locales/$name").getFile)) match {
//    case Success(resources) => resources.list().toSeq.flatMap(x => x.flatMap(e =>repeatElementsInList(listWithWeight(x, weight)) ))
//    case Failure(_) => throw new KhermesException(s"Error loading invalid name /locales/$name")
//  }

  def parse[T](unitName: String, locale: String)(implicit m: Manifest[T]): Either[String, T] = Try(
    read[T](getResource(unitName, locale))) match {
    case Success(model) => Right(model)
    case Failure(e) => Left(s"${e.getMessage}")
  }
  def parse[T](unitName: String, locale: String, weight: Map[String, Double])(implicit m: Manifest[T]): Either[String, T] = Try(
    read[T](getResource(unitName, locale))) match {
    case Success(model) => Right(model)
    case Failure(e) => Left(s"${e.getMessage}")
  }

  def parseErrors[T](maybeResources: Seq[Either[String, T]]): Seq[String] = {
    maybeResources.filter(_.isLeft).map(_.left.get)
  }

  def getResource(name: String, file: String): InputStream = Option(
    getClass.getResourceAsStream(s"/locales/$name/$file")).getOrElse(
    throw new KhermesException(s"Error loading invalid resource /locales/$name/$file"))
}
