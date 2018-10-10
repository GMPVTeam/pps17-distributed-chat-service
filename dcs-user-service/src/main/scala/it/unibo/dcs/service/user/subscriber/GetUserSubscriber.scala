package it.unibo.dcs.service.user.subscriber

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.core.http.HttpServerResponse
import it.unibo.dcs.service.user.model.User
import it.unibo.dcs.service.user.model.exception.UserNotFoundException
import rx.lang.scala.Subscriber

import it.unibo.dcs.service.user.request.Implicits._
import Implicits.httpResponseStatusToJsonObject

final class GetUserSubscriber(private[this] val response: HttpServerResponse) extends Subscriber[User] {
  override def onNext(user: User): Unit = {
    val userJsonObject: JsonObject = user
    response.end(userJsonObject.toString)
  }

  override def onCompleted(): Unit = ()

  override def onError(error: Throwable): Unit = error match {
    case UserNotFoundException(username) =>
      val statusJson: JsonObject = HttpResponseStatus.NOT_FOUND
      val extras = new JsonObject().put("username", username)
      response.setStatusCode(HttpResponseStatus.NOT_FOUND.code())
      response.write(new JsonObject().put("status", statusJson).put("extras", extras).toString)
  }

}

