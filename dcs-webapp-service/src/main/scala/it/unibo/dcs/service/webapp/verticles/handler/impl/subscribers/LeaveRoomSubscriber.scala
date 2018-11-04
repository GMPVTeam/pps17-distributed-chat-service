package it.unibo.dcs.service.webapp.verticles.handler.impl.subscribers

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.core.http.HttpServerResponse
import it.unibo.dcs.commons.Publisher
import it.unibo.dcs.exceptions.ErrorSubscriber
import it.unibo.dcs.service.webapp.interaction.Results.RoomLeaveResult
import it.unibo.dcs.service.webapp.interaction.Results.Implicits._
import it.unibo.dcs.commons.VertxWebHelper.Implicits._
import rx.lang.scala.Subscriber

final class LeaveRoomSubscriber(protected override val response: HttpServerResponse,
                               private[this] val publisher: Publisher)
  extends Subscriber[RoomLeaveResult] with ErrorSubscriber {

  override def onNext(result: RoomLeaveResult): Unit = {
    val res: JsonObject = result
    response.setStatus(HttpResponseStatus.NO_CONTENT).end(res.encode())
    publisher.publish(res)
  }
}

object LeaveRoomSubscriber {
  def apply(response: HttpServerResponse, publisher: Publisher): LeaveRoomSubscriber =
    new LeaveRoomSubscriber(response, publisher)
}
