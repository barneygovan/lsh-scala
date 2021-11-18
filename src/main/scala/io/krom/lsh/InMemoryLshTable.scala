package io.krom.lsh

import scala.collection.mutable
import scala.collection.mutable.{HashMap, HashSet}

object InMemoryLshTable {
  def createTables(
      numTables: Int,
      prefix: Option[String] = None
  ): IndexedSeq[NewLshTable] = {
    for {
      _ <- 1 to numTables
    } yield new InMemoryLshTable(prefix)
  }
}

class InMemoryLshTable(override protected val prefix: Option[String] = None)
    extends NewLshTable {

  private val index = new HashMap[String, HashSet[String]]()
  private val table = new HashMap[String, LshEntry]()

  override def put(entry: LshEntry): Unit = {
    val value = entry.copy(hash = createKey(entry.hash))

    if (!index.keySet.contains(value.hash))
      index(value.hash) = new HashSet[String]()
    index(value.hash) += entry.label
    table(value.label) = value
  }

  override def update(entry: LshEntry): Unit = {
    val oldValue = table(entry.label)
    val newValue = entry.copy(hash = createKey(entry.hash))

    table(entry.label) = newValue
    if (newValue.hash != oldValue.hash) {
      index(oldValue.hash) -= entry.label
      if (!index.keySet.contains(newValue.hash))
        index(newValue.hash) = new mutable.HashSet[String]()
      index(newValue.hash) += entry.label
    }
  }

  override def get(hash: String): List[LshEntry] = {
    val key = createKey(hash)
    val items = if (index.keySet.contains(key)) index(key) else new HashSet()
    (for {
      item <- items
    } yield table(item)).toList
  }
}
