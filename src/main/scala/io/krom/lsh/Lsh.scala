package io.krom.lsh

import breeze.linalg.{DenseMatrix, DenseVector}
import breeze.stats.distributions.Gaussian
import io.krom.lsh.DistanceFunction.euclideanDistance

import scala.collection.immutable.HashMap
import scala.collection.mutable.{HashSet, PriorityQueue}


class Lsh(tables: IndexedSeq[LshTable], projections: IndexedSeq[DenseMatrix[Double]]) {

  def store(point: DenseVector[Double], label: String) {
    for ((key, i) <- calculateHashes(point).zipWithIndex) {
      tables(i).put(key, label, point)
    }
  }

  def query(point: DenseVector[Double],
            maxItems: Int = 25,
            distanceFunction: (DenseVector[Double],DenseVector[Double]) => Double = euclideanDistance):
            IndexedSeq[(String, Double)] = {

    def isLabelNew(label: String, labelSet: HashSet[String]): Boolean = {
      if (labelSet.contains(label)) {
        false
      } else {
        labelSet += label
        true
      }
    }

    val labelSet = new HashSet[String]()
    val results = for {
      (key, i) <- calculateHashes(point).zipWithIndex
        items = tables(i).get(key)
      (label, _, otherPoint) <- items  if isLabelNew(label, labelSet)
    } yield (label, distanceFunction(point, otherPoint))

    val heap = new PriorityQueue[(String, Double)]()(Ordering.by(_._2))
    heap ++= results
    heap.take(maxItems).toIndexedSeq
  }

  def update(point: DenseVector[Double], label: String) {
    for ((key, i) <- calculateHashes(point).zipWithIndex) {
      tables(i).update(key, label, point)
    }
  }

  private def calculateHashes(point: DenseVector[Double]): IndexedSeq[String] = {
    for {
      projection <- projections
    } yield (projection * point).map((x: Double) => if (x > 0.0) "1" else "0").toArray.mkString
  }
}

object Lsh {
  def apply(numBits: Int, numDimensions: Int, numTables: Int, prefix: Option[String] = None,
            projectionsFilename: Option[String] = None, storageConfig: Option[HashMap[String, String]] = None): Lsh = {

    val tables = storageConfig match {
      case None => InMemoryLshTable.createTables (numTables, prefix)
      case Some(config) => RedisLshTable.createTables(numTables, config, prefix)
    }
    val projections = initializeProjections(numBits, numDimensions, numTables, loadProjectionsData(projectionsFilename))
    new Lsh(tables, projections)
  }

  def initializeProjections(numBits: Int, numDimensions: Int, numTables: Int,
                            projectionsData: Option[IndexedSeq[DenseMatrix[Double]]] = None): IndexedSeq[DenseMatrix[Double]] = {
    projectionsData match {
      case None => {
        val g = Gaussian(0.0, 1.0)
        for {
          _ <- 1 to numTables
        } yield new DenseMatrix(numBits, numDimensions, g.sample(numBits * numDimensions).toArray)
      }
      case Some(matrices) => {
        for {
          matrix <- matrices
          if matrix.rows != numBits && matrix.cols != numDimensions
        } throw new java.io.IOException(s"Provided matrix has ${matrix.rows} rows and ${matrix.cols} instead of $numBits and $numDimensions")
        matrices
      }
    }
  }

  def loadProjectionsData(projectionsFilename: Option[String]): Option[IndexedSeq[DenseMatrix[Double]]] = projectionsFilename match {
    case None => None
    case Some(fn) => {
      val inputStream = new java.io.ObjectInputStream(new java.io.FileInputStream(fn))
      val projections = inputStream.readObject.asInstanceOf[IndexedSeq[DenseMatrix[Double]]]
      inputStream.close
      Some(projections)
    }
  }

  def generateRandomProjections(numBits: Int, numDimensions: Int, numTables: Int, projectionsFilename: String): Unit = {
    val projectionsData = initializeProjections(numBits, numDimensions, numTables)
    val outputStream = new java.io.ObjectOutputStream(new java.io.FileOutputStream(projectionsFilename))
    outputStream.writeObject(projectionsData)
    outputStream.close()
  }

}

