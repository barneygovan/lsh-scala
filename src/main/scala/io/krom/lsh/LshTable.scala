package io.krom.lsh

import breeze.linalg.DenseVector

abstract class LshTable(prefix: Option[String] = None) {

  def put(hash: String, label: String, point: DenseVector[Double])
  def get(hash: String): List[(String, String, DenseVector[Double])]
  def update(hash: String, label: String, point: DenseVector[Double])

  protected def createKey(hash: String): String = {
    prefix match {
      case None => hash
      case Some(p) => p + ":" + hash
    }
  }
}
