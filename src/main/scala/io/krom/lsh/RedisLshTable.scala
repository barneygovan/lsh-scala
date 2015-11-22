package io.krom.lsh

import breeze.linalg.DenseVector
import com.lambdaworks.jacks.JacksMapper
import com.redis.RedisClient

import scala.collection.immutable.HashMap

class RedisLshTable(redisdb: RedisClient, prefix: Option[String] = None) extends LshTable(prefix) {

  override def put(hash: String, label: String, point: DenseVector[Double]): Unit = {
    val key = createKey(hash)
    val value = (label, key, point.toArray)

    redisdb.pipeline { pipe =>
      pipe.sadd(key, label)
      pipe.set(label, JacksMapper.writeValueAsString(value))
    }
  }

  override def update(hash: String, label: String, point: DenseVector[Double]): Unit = {
    val key = createKey(hash)

    val item = redisdb.get(label) match {
      case None => return
      case Some(x:String) => JacksMapper.readValue[(String, String, Array[Double])](x)
    }
    val oldKey = item._2

    val value = (label, key, point.toArray)

    redisdb.pipeline { pipe =>
      pipe.set(label, JacksMapper.writeValueAsString(value))
      if (key != oldKey) pipe.srem(oldKey, label)
      pipe.sadd(key, label)
    }
  }

  override def get(hash: String): List[(String, String, DenseVector[Double])] = {
    val key = createKey(hash)
    val items = redisdb.smembers(key)

    val itemDetails = redisdb.pipeline { pipe =>
      for {
        item <- items.get
        if item.isDefined
      } pipe.get(item.get)
    }

    for {
      item <- itemDetails.get
      newItem = item match {
        case Some(x:String) => Some(JacksMapper.readValue[(String, String, Array[Double])](x))
        case None => None
      }
      if newItem.isDefined
    } yield ( newItem.get._1, newItem.get._2, DenseVector(newItem.get._3) )
  }
}

object RedisLshTable {
  def createTables(numTables: Int, redisConf: HashMap[String, String], prefix: Option[String] = None): IndexedSeq[LshTable] = {
    val redisHost = if (redisConf.contains("host")) redisConf("host") else "localhost"
    val redisPort = if (redisConf.contains("port")) Integer.parseInt(redisConf("port")) else 6379
    val redisDb = if (redisConf.contains("db")) Integer.parseInt(redisConf("db")) else 0
    for {
      _ <- 1 to numTables
    } yield new RedisLshTable(new RedisClient(redisHost, redisPort, redisDb), prefix)
  }
}