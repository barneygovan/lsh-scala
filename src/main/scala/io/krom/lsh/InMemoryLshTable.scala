package io.krom.lsh

import breeze.linalg.DenseVector

import collection.mutable.HashMap
import collection.mutable.HashSet
import scala.collection.mutable

class InMemoryLshTable(prefix: Option[String] = None) extends LshTable(prefix) {

  private val index = new HashMap[String, HashSet[String]]()
  private val table = new HashMap[String, (String, String, DenseVector[Double])]()


  override def put(hash: String, label: String, point: DenseVector[Double]) = {
    val key = createKey(hash)
    val value = (label, key, point)


    if (!index.keySet.contains(key)) index(key) = new HashSet[String]()
    index(key) += label
    table(label) = value
  }

  override def update(hash: String, label: String, point: DenseVector[Double]) = {
    val key = createKey(hash)
    val (_, oldKey, _) = table(label)
    val newValue = (label, key, point)

    table(label) = newValue
    if (key != oldKey) {
      index(oldKey) -= label
      if (!index.keySet.contains(key)) index(key) = new mutable.HashSet[String]()
      index(key) += label
    }
  }

  override def get(hash: String): List[(String, String, DenseVector[Double])] = {
    val key = createKey(hash)

    val items = if (index.keySet.contains(key)) index(key) else new HashSet()

    (for {
      item <- items
    } yield table(item)).toList
  }
}

object InMemoryLshTable {
  def createTables(numTables: Int, prefix: Option[String] = None): IndexedSeq[LshTable] = {
    for {
      _ <- 1 to numTables
    } yield new InMemoryLshTable(prefix)
  }
}