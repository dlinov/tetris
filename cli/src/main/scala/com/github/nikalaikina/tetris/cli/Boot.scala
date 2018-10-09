package com.github.nikalaikina.tetris.cli

import cats.syntax.functor._
import com.github.nikalaikina.tetris._
import monix.execution.Cancelable
import monix.reactive._
import monix.reactive.observers.Subscriber

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.sys.process._

object Boot extends App {
  import monix.execution.Scheduler.Implicits.global

  def clear() = "clear".!

  val downObservable = Observable.interval(1 second).as(Matrix.Tick)

  val keyboardListener = Observable.create(OverflowStrategy.DropOld(10)) { f: Subscriber.Sync[Matrix.KeyPressed] =>
    val keyboardListener = new KeyboardListener(key => f.onNext(Matrix.KeyPressed(key)))
    Cancelable(() => keyboardListener.dispose())
  }

  val merged: Observable[Unit] = Observable.merge(downObservable, keyboardListener)
    .scan(Matrix()) {
      case (m, Matrix.Tick) =>
        m.down
      case (m, Matrix.KeyPressed(key)) =>
        m.move(key)
    } map { x =>
      clear()
      x.show.flatMap(x => Seq(x, x)).map(_.map(if (_) "###" else "   ").mkString("")).foreach(println)
      println(x.score)
    }


  Await.result(merged.runAsyncGetLast, 5 minutes)
}
