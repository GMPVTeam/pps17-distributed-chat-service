package it.unibo.dcs.service.webapp.verticles.handler.impl.subscribers

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.core.Context
import io.vertx.scala.core.http.HttpServerResponse
import it.unibo.dcs.commons.Address
import it.unibo.dcs.commons.VertxWebHelper.Implicits.RichHttpServerResponse
import it.unibo.dcs.exceptions._
import it.unibo.dcs.service.webapp.interaction.Results.Implicits._
import it.unibo.dcs.service.webapp.interaction.Results.RoomCreationResult
import rx.lang.scala.Subscriber


final class RoomCreationSubscriber(protected override val response: HttpServerResponse,
                                   private[this] val address: Address) extends Subscriber[RoomCreationResult]
  with ErrorSubscriber {

  override def onNext(result: RoomCreationResult): Unit = {
    response.setStatus(HttpResponseStatus.CREATED).end()
    address.publish[JsonObject](result)
  }

}

object RoomCreationSubscriber {

  def apply(response: HttpServerResponse, address: Address): RoomCreationSubscriber = new RoomCreationSubscriber(response, address)

}
