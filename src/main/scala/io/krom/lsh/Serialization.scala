package io.krom.lsh

import breeze.linalg.DenseVector
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.{ArrayNode, DoubleNode, ObjectNode}
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.collection.JavaConverters._

trait Serialization {

  def serialize(lsh: LshEntry): Array[Byte]

  def deserialize(data: Array[Byte]): LshEntry

}

trait JsonSerialization extends Serialization {

  List().asJava
  private lazy val MAPPER: ObjectMapper = {
    val m = new ObjectMapper()
    m.registerModule(DefaultScalaModule)
    m.registerModule(new JavaTimeModule)
  }

  override def serialize(lsh: LshEntry): Array[Byte] = {
    MAPPER.writeValueAsString(lsh).getBytes
  }

  override def deserialize(input: Array[Byte]): LshEntry = {
    val json = MAPPER.readValue[ObjectNode](input, classOf[ObjectNode])
    val hash = json.get("hash").textValue()
    val label = json.get("label").textValue()
    // custom jackson serialization because `breeze.linalg.DenseVector` is specialized
    val point: DenseVector[Double] = {
      val node: ArrayNode =
        json.get("point").withArray[ArrayNode]("data$mcD$sp")
      val vector =
        node
          .elements()
          .asScala
          .flatMap {
            case node: DoubleNode => Some(node.doubleValue())
            case other =>
              throw new IllegalStateException(
                s"non numeric value found in Array[ Double ] - ${other.toString}"
              )
          }
          .toArray
      DenseVector(vector)

    }

    LshEntry(hash = hash, label = label, point = point)
  }

}
