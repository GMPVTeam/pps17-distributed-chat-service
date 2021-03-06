package it.unibo.dcs.service.webapp.verticles.handler.impl.subscribers

import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.core.http.HttpServerResponse
import it.unibo.dcs.commons.Publisher
import it.unibo.dcs.commons.VertxWebHelper.Implicits.RichHttpServerResponse
import it.unibo.dcs.exceptions.ErrorSubscriber
import it.unibo.dcs.service.webapp.interaction.Results.Implicits.loginResultToJsonObject
import it.unibo.dcs.service.webapp.interaction.Results.LoginResult
import rx.lang.scala.Subscriber

import scala.language.postfixOps

final class LoginUserSubscriber(protected override val response: HttpServerResponse,
                                private[this] val publisher: Publisher) extends Subscriber[LoginResult]
  with ErrorSubscriber {

  override def onNext(result: LoginResult): Unit = {
    val json: JsonObject = result
    response endWith json
    publisher publish[JsonObject] json
  }

}

object LoginUserSubscriber {

  def apply(response: HttpServerResponse, publisher: Publisher): LoginUserSubscriber =
    new LoginUserSubscriber(response, publisher)

}
