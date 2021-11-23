package io.krom.lsh

import com.redis.RedisClient

object RedisLshTable {
  def createTables(
      numTables: Int,
      redisConf: Map[String, String],
      prefix: Option[String] = None
  ): IndexedSeq[LshTable] = {
    val redisHost =
      if (redisConf.contains("host")) redisConf("host") else "localhost"
    val redisPort =
      if (redisConf.contains("port")) Integer.parseInt(redisConf("port"))
      else 6379
    val serializer =
      if (redisConf.contains("serializer")) redisConf("serializer") else "json"

    (0 to numTables).map(i => {
      serializer match {
        case "json" =>
          new RedisLshTable(new RedisClient(redisHost, redisPort, i))
            with JsonSerialization
        case other =>
          throw new IllegalArgumentException(
            s"${other} is not a supported serialization format"
          ) //TODO - would like to add support for java, protobuf, etc...
      }
    })
  }

}

abstract class RedisLshTable(
    redisdb: RedisClient,
    val prefix: Option[String] = None
) extends LshTable
    with Serialization {

  override def put(entry: LshEntry): Unit = {
    val key = createKey(entry.hash)
    redisdb.pipeline { pipe =>
      pipe.sadd(key, entry.label)
      pipe.set(entry.label, serialize(entry))
    }
  }

  override def update(entry: LshEntry): Unit = {
    val key = createKey(entry.hash)

    val oldEntry = redisdb.get(entry.label) match {
      case None       => return
      case Some(data) => deserialize(data.getBytes)
    }
    val oldKey = oldEntry.hash

    redisdb.pipeline { pipe =>
      pipe.set(entry.label, serialize(entry))
      if (key != oldKey) pipe.srem(oldKey, entry.label)
      pipe.sadd(key, entry.label)
    }
  }

  override def get(hash: String): List[LshEntry] = {
    import com.redis.serialization.Parse.Implicits.parseByteArray
    val key = createKey(hash)
    val items = redisdb.smembers(key)

    val itemDetails = redisdb.pipeline { pipe =>
      for {
        item <- items.get
        if item.isDefined
      } pipe.get(item.get)
    }

    itemDetails.get
      .flatMap {
        case Some(data: Array[Byte]) => Some(deserialize(data))
        case Some(other) =>
          throw new UnsupportedOperationException(
            s"unable to serialize data of ${other.getClass.getCanonicalName}"
          )
        case None => None
      }
  }
}
