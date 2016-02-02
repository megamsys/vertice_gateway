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
import scala.concurrent._
import scala.concurrent.duration.Duration
import io.megam.common._
import io.megam.common.amqp._
import io.megam.common.amqp.request._
import io.megam.common.amqp.response._
import io.megam.common.concurrent._
import play.api.Logger

/**
 * @author rajthilak
 *
 */

trait MessageContext {


  def topic: String

  def nsqClient = {
    play.api.Logger.debug("%-20s -->[%s]".format("NSQ:", app.MConfig.nsqurl))
    new NSQClient(app.MConfig.nsqurl, topic)
  }

  protected def execute(ampq_request: AMQPRequest, duration: Duration = io.megam.common.concurrent.duration) = {
    import io.megam.common.concurrent.SequentialExecutionContext
    val responseFuture: Future[ValidationNel[Throwable, AMQPResponse]] = ampq_request.apply
    responseFuture.block(duration)
  }
}
