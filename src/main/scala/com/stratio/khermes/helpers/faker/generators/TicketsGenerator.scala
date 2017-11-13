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
import org.json4s.native.Serialization._

import scala.util.{Failure, Success, Try}

case class TicketsGenerator(locale: String) extends FakerGenerator with AppSerializer with LazyLogging {

  override def name: String = "tickets"

  val resourcesFiles = Seq("tickets.json")

  lazy val ticketModel = {
    locale match {
      case AppConstants.DefaultLocale | "EN" | "ES" =>
        val resources = resourcesFiles.map(parse[Seq[Ticket]](name, _))
        if (parseErrors[Seq[Ticket]](resources).nonEmpty) {
          logger.warn(s"${parseErrors[Seq[Ticket]](resources)}")
        }
        resources
      case localeValue =>
        logger.info(s"Tickets from resource ($localeValue) .... ")
        Seq(parse[Seq[Ticket]](name, s"$localeValue.json"))

    }
  }.filter(_.isRight).flatMap(_.right.get.map(ticketToTicketModel))

  def generateTicket: TicketModel =
    Try {
      randomElementFromAList[TicketModel](ticketModel).get
    } match {
      case Success(ticket) =>
        ticket
      case Failure(e) =>
        throw new KhermesException(s"Error loading locate /locales/$name/$locale with exception: ${e.getLocalizedMessage}")
    }

  def ticketToTicketModel(ticket: Ticket): TicketModel = {
    TicketModel(
      Option(ticket._id.$oid).getOrElse(""),
      ticket.idSale,
      Option(write(ticket.dateFlat)).getOrElse("{}"),
      Option(ticket.date.$date).getOrElse(""),
      Option(ticket.cashier).getOrElse(""),
      Option(ticket.clientId).getOrElse(""),
      Option(ticket.clientName).getOrElse(""),
      Option(ticket.storeId).getOrElse(""),
      ticket.totalSale,
      ticket.lines.map(item => write(item)),
      ticket.pays.map(concept => write(concept))
    )
  }
}

case class TicketModel(
                        id: String, idSale: String, dateFlat: String, date: String, cashier: String,
                        clientId: String, clientName: String, storeId: String, totalSale: Double,
                        lines: Array[String], pays: Array[String]
                      )

case class Ticket(
                   _id: TicketId, idSale: String, date: TicketDate, dateFlat: DateFlat, cashier: String,
                   clientId: String, clientName: String, storeId: String, totalSale: Double,
                   lines: Array[ItemTicket], pays: Array[Concepts]
                 )

case class TicketId($oid: String)

case class TicketDate($date: String)

case class DateFlat(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int)

case class ItemTicket(
                       lineId: Long, itemId: String, itemName: String, groupId: String, groupName: String,
                       offerId: String, aecoc: String, tax: Double, quantity: Double, unitCost: Double,
                       unitPrice: Double, totalLine: Double
                     )

case class Concepts(idPay: Int, namePay: String, dueDate: DueDate, totalPay: Double)

case class DueDate($numberLong: String)
