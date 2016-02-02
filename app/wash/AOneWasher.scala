/*
** Copyright [2013-2016] [Megam Systems]
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
** http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/
package wash

import scalaz._
import Scalaz._
import app.MConfig
import io.megam.common._
import io.megam.common.amqp._
import io.megam.common.amqp.request._
import io.megam.common.amqp.response._
import models.base.RequestResult
import play.api.Logger
import controllers.Constants._

/**
 * @author rajthilak
 *
 */
case class AOneWasher(pq: PQd) extends MessageContext {

  def topic = (pq.Tpk.getOrElse(""))

  val msg = Messages(pq.messages.toList)

  def wash(): ValidationNel[Throwable, AMQPResponse] = {
    play.api.Logger.debug("%-20s -->[%s]".format("Washing:[" + topic + "]", msg))
    execute(nsqClient.publish(msg))
  }
}

case class PQd(reqres: models.base.RequestResult) {

  val nsqcontainers = play.api.Play.application(play.api.Play.current).configuration.getString("nsq.topic.containers")
  val nsqvms = play.api.Play.application(play.api.Play.current).configuration.getString("nsq.topic.vms")

  val DQACTIONS = Array[String](CREATE, DELETE)

  def Tpk: Option[String] = {
    if (reqres.cattype.equalsIgnoreCase(CATTYPE_DOCKER)) {
      nsqcontainers
    }else if (reqres.cattype.equalsIgnoreCase(CATTYPE_TORPEDO)) {
      nsqvms
   }else if (DQACTIONS.contains(reqres.action)) {
      nsqvms
    } else if (reqres.name.trim.length > 0) {
      reqres.name.some
    } else none
  }

  val messages = reqres.toMap

}

object PQd {
  def empty: PQd = new PQd(models.base.RequestResult("", "", "", "", "", "", ""))
}
