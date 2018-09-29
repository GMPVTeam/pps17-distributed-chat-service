package it.unibo.dcs.commons

import io.vertx.core.{AsyncResult, Handler}
import it.unibo.dcs.commons.VertxHelper.Implicits._
import RxHelper.Implicits.RxObservable
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

  }

}
