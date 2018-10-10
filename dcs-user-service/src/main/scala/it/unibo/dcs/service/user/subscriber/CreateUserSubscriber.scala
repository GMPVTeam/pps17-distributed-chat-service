package it.unibo.dcs.service.user.subscriber

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.core.http.HttpServerResponse
import it.unibo.dcs.service.user.model.User
import it.unibo.dcs.service.user.model.exception.UserNotFoundException
import rx.lang.scala.Subscriber

import it.unibo.dcs.service.user.request.Implicits._

import Implicits.httpResponseStatusToJsonObject

final class CreateUserSubscriber(private[this] val response: HttpServerResponse) extends Subscriber[User] {

  override def onNext(user: User): Unit = {
    val userJsonObject: JsonObject = userToJsonObject(user)
    response.end(userJsonObject.toString)
  }

  override def onCompleted(): Unit = ()

  override def onError(error: Throwable): Unit = error match {
    case UserNotFoundException(username) =>
      val statusJson: JsonObject = HttpResponseStatus.BAD_REQUEST
      val extras = new JsonObject().put("username", username)
      response.setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
      response.write(new JsonObject().put("status", statusJson).put("extras", extras).toString)
  }

}
