package com.github.nikalaikina.tetris.http

import cats.syntax.show._
import cats.syntax.functor._
import cats.implicits.catsStdShowForInt
import com.github.nikalaikina.tetris._
import monix.execution.Cancelable
import monix.reactive._
import monix.reactive.observers.Subscriber
import monix.execution.Scheduler.Implicits.global
import org.scalajs.dom
import org.scalajs.dom.ext.KeyCode

import scala.concurrent.duration._
import scala.scalajs.js.Any._
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("GameApp")
object GameApp {
  private val canvasNodeName = "tetris"
  private val scoreNodeName = "score"
  private val sqSize = 20
  private val keyMap = Map(
    KeyCode.Up → Up,
    KeyCode.Down → Down,
    KeyCode.Left → Left,
    KeyCode.Right → Right)

  @JSExport
  def main(): Unit = {
    val downObservable = Observable.interval(1 second).as(Matrix.Tick)
    val canvas = dom.document.getElementById(canvasNodeName).asInstanceOf[dom.raw.HTMLCanvasElement]
    val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    val scoreSpan = dom.document.getElementById(scoreNodeName).asInstanceOf[dom.raw.HTMLSpanElement]
    val prevOnKeyDown = dom.window.onkeydown
    val keyboardListener = Observable.create(OverflowStrategy.DropOld(10)) { f: Subscriber.Sync[Matrix.KeyPressed] ⇒
      dom.window.onkeydown = { e: dom.KeyboardEvent ⇒
        keyMap.get(e.keyCode).foreach { k ⇒
          f.onNext(Matrix.KeyPressed(k))
          e.preventDefault()
        }
      }
      Cancelable(() ⇒ {
        dom.window.onkeydown = prevOnKeyDown
      })
    }

    val merged: Observable[Unit] = Observable.merge(downObservable, keyboardListener)
      .scan(Matrix()) {
        case (m, Matrix.Tick) ⇒
          m.down
        case (m, Matrix.KeyPressed(key)) ⇒
          m.move(key)
      } map { x ⇒
        drawMatrix(ctx, x, canvas.width, canvas.height)
        scoreSpan.textContent = x.score.show
      }

    // it's a CancelableFuture, so there is a way to stop/restart the game
    merged.runAsyncGetLast
  }

  def drawMatrix(ctx: dom.CanvasRenderingContext2D, matrix: Matrix, width: Int, height: Int): Unit = {
    ctx.fillStyle = Color.Yellow
    ctx.fillRect(0, 0, width, height)
    ctx.strokeStyle = Color.Black
    matrix.show.zipWithIndex.foreach {
      case (row, i) ⇒
        row.zipWithIndex.foreach {
          case (col, j) ⇒
            ctx.fillStyle = if (col) Color.Blue else Color.Black
            ctx.fillRect(j * sqSize, i * sqSize, sqSize, sqSize)
            ctx.strokeRect(j * sqSize, i * sqSize, sqSize, sqSize)
        }
    }
  }
}
