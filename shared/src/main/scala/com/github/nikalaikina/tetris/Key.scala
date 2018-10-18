package com.github.nikalaikina.tetris

sealed trait Key

case object Left  extends Key

case object Right extends Key

case object Up    extends Key

case object Down  extends Key
