package io.krom.lsh

trait LshTable {

  protected val prefix: Option[String]

  def put(entry: LshEntry): Unit

  def update(entry: LshEntry)

  def get(hash: String): List[LshEntry]

  protected def createKey(hash: String): String = {
    prefix match {
      case None    => hash
      case Some(p) => p + ":" + hash
    }
  }

}
