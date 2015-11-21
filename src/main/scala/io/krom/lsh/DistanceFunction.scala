package io.krom.lsh

import breeze.linalg.DenseVector
import breeze.numerics.sqrt

object DistanceFunction extends Enumeration {
  type DistanceFunction = Value
  val Euclidean, Cosine = Value

  def euclideanDistance(pointX: DenseVector[Double], pointY: DenseVector[Double]): Double = {
    val diff = pointX - pointY
    1.0 / (sqrt(diff dot diff) + 1.0)
  }

  def cosineDistance(pointX: DenseVector[Double], pointY: DenseVector[Double]): Double = {
    (pointX dot pointY) / Math.pow((pointX dot pointX) * (pointY dot pointY), 0.5)
  }

}