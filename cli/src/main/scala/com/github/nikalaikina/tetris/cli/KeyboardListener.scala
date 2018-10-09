package com.github.nikalaikina.tetris.cli

import java.awt.event.{KeyEvent, KeyListener}

import com.github.nikalaikina.tetris._
import javax.swing.JFrame

class KeyboardListener[T](push: Key => T) extends JFrame("") with KeyListener {

  addKeyListener(this)
  setVisible(true)

  override def keyPressed(e: KeyEvent): Unit = e.getKeyCode match {
    case KeyEvent.VK_RIGHT => push(Right)
    case KeyEvent.VK_LEFT  => push(Left)
    case KeyEvent.VK_UP    => push(Up)
    case KeyEvent.VK_DOWN  => push(Down)
    case _  =>
  }

  override def keyReleased(e: KeyEvent): Unit = { }
  override def keyTyped(e: KeyEvent): Unit = { }

}
