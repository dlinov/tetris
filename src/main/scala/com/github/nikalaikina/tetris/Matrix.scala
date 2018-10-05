package com.github.nikalaikina.tetris

import cats.data.State
import com.github.nikalaikina.tetris.Matrix.{Coordinate, Tetrominoe, figures}

import scala.util.Random


case class Matrix(
  tetrominoes: Vector[Tetrominoe] = Vector.empty
) {
  lazy val current = tetrominoes.headOption
  lazy val tail = tetrominoes.tail
  lazy val ground: Set[Coordinate] = Matrix(tail).cells ++ (0 until 10).map(Coordinate(20, _))
  lazy val cells: Set[Matrix.Coordinate] = tetrominoes.flatMap(_.cells).toSet
  def show = {
    (0 until 20).map { i =>
      (0 until 10).map { j =>
        cells.contains((i, j))
      }
    }
  }

  lazy val isSolid: Boolean = {
    current.forall(_.inc.cells.exists(ground.contains))
  }

  lazy val down: Matrix = {
    if (isSolid) {
      val next = Random.nextInt(figures.size)
      val newT = Tetrominoe(next, Coordinate(4, 0))
      Matrix(newT +: tetrominoes)
    } else {
      Matrix(current.map(_.inc).toVector ++ tail)
    }
  }
  def move(k: Key) = if (k == Down) {
    down
  } else {
    Matrix(current.map(_.move(k)).toVector ++ tail)
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



  implicit def toCoordinate(t: (Int, Int)): Coordinate = Coordinate(t._1, t._2)

  case class Coordinate(x: Int, y: Int) {
    def inc = Coordinate(x, y + 1)
  }
}

