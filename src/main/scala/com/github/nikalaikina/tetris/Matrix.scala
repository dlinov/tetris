package com.github.nikalaikina.tetris

import com.github.nikalaikina.tetris.Matrix.{Coordinate, Tetrominoe}

import scala.util.Random


case class Matrix(
  current: Tetrominoe = Tetrominoe(),
  ground: Set[Coordinate] = (0 until 10).map(Coordinate(20, _)).toSet
) {
  lazy val cells: Set[Matrix.Coordinate] = ground ++ current.cells
  def show = {
    (0 until 20).map { i =>
      (0 until 10).map { j =>
        cells.contains((i, j))
      }
    }
  }

  lazy val isSolid: Boolean = {
    current.inc.cells.exists(ground.contains)
  }

  lazy val down: Matrix = {
    if (isSolid) {
      Matrix(Tetrominoe(), ground ++ current.cells)
    } else {
      Matrix(current.inc, ground)
    }
  }
  def move(k: Key) = if (k == Down) {
    down
  } else {
    Matrix(current.move(k), ground)
  }
}

object Matrix {

  val figures: Vector[Vector[Vector[Boolean]]] = {
    val o = false
    val X = true
    Vector(
      Vector(Vector(X, X, X, X)),

      Vector(Vector(o, X, o),
             Vector(X, X, X)),

      Vector(Vector(X, X, X),
             Vector(X, o, o)),

      Vector(Vector(X, o, o),
             Vector(X, X, X)),

      Vector(Vector(X, X),
             Vector(X, X)),

      Vector(Vector(X, X, o),
             Vector(o, X, X)),

      Vector(Vector(o, X, X),
             Vector(X, X, o))
    )
  }

  case class Tetrominoe(figure: Int, c: Coordinate) {
    import c._
    lazy val t = figures(figure)
    def cells: Vector[Coordinate] = for {
      (row, i) <- t.zipWithIndex
      (is, j) <- row.zipWithIndex
      if is
    } yield Coordinate(i + y, j + x)
    def inc = Tetrominoe(figure, c.inc)

    def move(k: Key) = k match {
      case Left  => Tetrominoe(figure, (x - 1, y))
      case Right => Tetrominoe(figure, (x + 1, y))
      case Up    => this // TODO: turn
    }
  }

  object Tetrominoe {
    def apply(): Tetrominoe = Tetrominoe(Random.nextInt(figures.size), Coordinate(4, 0))
  }

  implicit def toCoordinate(t: (Int, Int)): Coordinate = Coordinate(t._1, t._2)

  case class Coordinate(x: Int, y: Int) {
    def inc = Coordinate(x, y + 1)
  }
}

