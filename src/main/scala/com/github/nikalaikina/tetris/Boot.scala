package com.github.nikalaikina.tetris

import cats.syntax.functor._
import monix.execution.Cancelable
import monix.reactive._
import monix.reactive.observers.Subscriber

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.sys.process._

object Boot extends App {
  import monix.execution.Scheduler.Implicits.global

  def clear() = "clear".!

  val downObservable = Observable.interval(1 second).as(Tick)

  val keyboardListener = Observable.create(OverflowStrategy.DropOld(10)) { f: Subscriber.Sync[KeyPressed] =>
    val keyboardListener = new KeyboardListener(key => f.onNext(KeyPressed(key)))
    Cancelable(() => keyboardListener.dispose())
  }

  val merged: Observable[Unit] = Observable.merge(downObservable, keyboardListener)
    .scan(Matrix()) {
      case (m, Tick) =>
        m.down
      case (m, KeyPressed(key)) =>
        m.move(key)
    } map { x =>
      clear()
      x.show.flatMap(x => Seq(x, x)).map(_.map(if (_) "###" else "   ").mkString("")).foreach(println)
      println(x.score)
    }


  Await.result(merged.runAsyncGetLast, 5 minutes)
}

sealed trait Action
case object Tick extends Action
case class KeyPressed(k: Key) extends Action
