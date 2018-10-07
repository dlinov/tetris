package com.github.nikalaikina.tetris

import com.github.nikalaikina.tetris.Matrix.{Coordinate, Tetrominoe}
import Constants._
import scala.util.Random

case class Matrix(
  current: Tetrominoe = Tetrominoe(),
  ground: Set[Coordinate] = Set.empty,
  score: Int = 0
) {
  lazy val cells: Set[Matrix.Coordinate] = ground ++ current.cells
  def show = {
    (0 until rows).map { i =>
      (0 until columns).map { j =>
        cells.contains((i, j))
      }
    }
  }

  lazy val isSolid: Boolean = {
    val wholeGround: Set[Coordinate] = Matrix.fakeGround ++ ground
    current.inc.cells.exists(wholeGround.contains)
  }

  lazy val down: Matrix = {
    if (isSolid) {
      val remove = cells.groupBy(_.row).filter(_._2.size == columns).keys
      val newGround = remove.foldLeft(cells) {
        case (acc, row) =>
          acc.filter(_.row != row).map { c =>
            if (c.row < row) c.inc else c
          }
      }
      Matrix(Tetrominoe(), newGround, score + remove.size)
    } else {
      Matrix(current.inc, ground, score)
    }
  }
  def move(k: Key) = if (k == Down) {
    down
  } else {
    val moved = current.move(k)
    if (moved.isCorrect && !moved.cells.exists(ground.contains)) {
      Matrix(moved, ground, score)
    } else {
      this
    }
  }
}

object Matrix {

  val fakeGround: Set[Coordinate] = (0 until 10).map(Coordinate(rows - 1, _)).toSet

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

  def transpose[T](matrix: Vector[Vector[T]]): Vector[Vector[T]] = {
    matrix.head.indices.map(i => matrix.map(_(i))).toVector
  }

  def rotate[T](matrix: Vector[Vector[T]]): Vector[Vector[T]] = {
    transpose(matrix).map(_.reverse)
  }

  def rotate[T](matrix: Vector[Vector[T]], n: Int): Vector[Vector[T]] = {
    if (n % 4 == 0) {
      matrix
    } else {
      rotate(rotate(matrix), n - 1)
    }
  }

  case class Tetrominoe(figure: Int, c: Coordinate, turn: Int) {
    lazy val t = rotate(figures(figure), turn)
    def cells: Vector[Coordinate] = for {
      (row, i) <- t.zipWithIndex
      (is, j) <- row.zipWithIndex
      if is
    } yield Coordinate(i + c.row, j + c.column)

    def inc = copy(c = c.inc)

    def move(k: Key) = k match {
      case Left  => copy(c = c.copy(column = c.column - 1))
      case Right => copy(c = c.copy(column = c.column + 1))
      case Up    => copy(turn = turn + 1)
    }

    def isCorrect: Boolean = cells.forall(_.isCorrect)
  }

  object Tetrominoe {
    def apply(): Tetrominoe = Tetrominoe(Random.nextInt(figures.size), Coordinate(0, 4), 0)
  }

  implicit def toCoordinate(t: (Int, Int)): Coordinate = Coordinate(t._1, t._2)

  case class Coordinate(row: Int, column: Int) {
    def inc = copy(row = row + 1)

    def isCorrect: Boolean = {
      column >= 0 && column < columns && row <= rows
    }
  }
}

object Constants {
  val rows = 20
  val columns = 10
}
