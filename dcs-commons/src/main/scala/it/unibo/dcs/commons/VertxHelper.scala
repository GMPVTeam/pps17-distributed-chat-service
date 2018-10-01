package it.unibo.dcs.commons

import io.vertx.core.{AsyncResult, Future, Handler}
import it.unibo.dcs.commons.RxHelper.Implicits.RxObservable
import it.unibo.dcs.commons.VertxHelper.Implicits._
import rx.lang.scala.Observable

object VertxHelper {

  def toObservable[T](action: (AsyncResult[T] => Unit) => Unit): Observable[T] = toObservable[T, T](identity)(action)

  def toObservable[J, S](converter: J => S)(action: (AsyncResult[J] => Unit) => Unit): Observable[S] = {
    val javaObservable = io.vertx.rx.java.RxHelper.observableFuture[J]()
    action(javaObservable.toHandler())
    RxObservable[J](javaObservable).map[S](converter)
  }

  object Implicits {

    implicit def handlerToFunction[T](handler: Handler[T]): Function[T, Unit] = handler.handle

    implicit def functionToHandler[T](handler: Function[T, Any]): Handler[T] = (event: T) => handler(event)

    implicit def toSucceededFuture[T] (result: T): AsyncResult[T] = Future.succeededFuture(result)

    implicit def toFailedFuture[T] (causeFailure: Throwable): AsyncResult[T] = Future.failedFuture(causeFailure)

  }

}
