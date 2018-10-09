package com.github.nikalaikina.tetris.http



object Color {
  def rgb(r: Int, g: Int, b: Int) = s"rgb($r, $g, $b)"

  val White: String = rgb(255, 255, 255)
  val Red: String = rgb(255, 0, 0)
  val Green: String = rgb(0, 255, 0)
  val Blue: String = rgb(0, 0, 255)
  val Cyan: String = rgb(0, 255, 255)
  val Magenta: String = rgb(255, 0, 255)
  val Yellow: String = rgb(255, 255, 0)
  val Black: String = rgb(0, 0, 0)
  val all: Seq[String] = Seq(
    White,
    Red,
    Green,
    Blue,
    Cyan,
    Magenta,
    Yellow,
    Black
  )
}
