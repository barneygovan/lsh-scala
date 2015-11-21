package io.krom.lsh

import breeze.linalg.DenseVector
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class InMemoryLshTableSpec extends FunSpec {

  describe("put without prefix") {
    it("should return the value just added") {

      val testPoint1 = DenseVector(0.1, 0.2)
      val testLabel1 = "point1"

      val testKey = "testhashkey"

      val table = new InMemoryLshTable()

      table.put(testKey, testLabel1, testPoint1)
      table.get(testKey).length should equal (1)
      table.get(testKey)(0) should equal (testLabel1, testKey, testPoint1)
    }
    it("should return multiple results when more than one value is added") {

      val testPoint1 = DenseVector(0.1, 0.2)
      val testLabel1 = "point1"
      val testPoint2 = DenseVector(0.3, 0.4)
      val testLabel2 = "point2"
      val testKey = "testhashkey"

      val table = new InMemoryLshTable()

      table.put(testKey, testLabel1, testPoint1)
      table.put(testKey, testLabel2, testPoint2)
      table.get(testKey).length should equal (2)
      val data = table.get(testKey).sortBy(_._1)
      data(0) should equal (testLabel1, testKey, testPoint1)
      data(1) should equal (testLabel2, testKey, testPoint2)
    }
  }

  describe("put with prefix") {
    it("should return the value just added") {

      val testPoint1 = DenseVector(0.1, 0.2)
      val testLabel1 = "point1"

      val testKey = "testhashkey"
      val testPrefix = "testprefix"

      val table = new InMemoryLshTable(Some(testPrefix))

      table.put(testKey, testLabel1, testPoint1)
      table.get(testKey).length should equal (1)
      table.get(testKey)(0) should equal (testLabel1, testPrefix + ":" + testKey, testPoint1)
    }
    it("should return multiple results when more than one value is added") {

      val testPoint1 = DenseVector(0.1, 0.2)
      val testLabel1 = "point1"
      val testPoint2 = DenseVector(0.3, 0.4)
      val testLabel2 = "point2"
      val testKey = "testhashkey"
      val testPrefix = "testPrefix"

      val table = new InMemoryLshTable(Some(testPrefix))

      table.put(testKey, testLabel1, testPoint1)
      table.put(testKey, testLabel2, testPoint2)
      table.get(testKey).length should equal (2)
      val data = table.get(testKey).sortBy(_._1)
      data(0) should equal (testLabel1, testPrefix + ":" + testKey, testPoint1)
      data(1) should equal (testLabel2, testPrefix + ":" + testKey, testPoint2)
    }
  }

  describe("update") {
    it("should change the value previously stored") {

      val testPoint = DenseVector(0.1, 0.2)
      val testUpdatedPoint = DenseVector(0.3, 0.4)
      val testKey1 = "testkey1"
      val testKey2 = "testkey2"
      val testPrefix = "testPrefix"
      val testLabel = "testData"

      val table = new InMemoryLshTable(Some(testPrefix))

      table.put(testKey1, testLabel, testPoint)
      table.get(testKey1).length should equal (1)
      table.get(testKey1)(0) should equal (testLabel, testPrefix + ":" + testKey1, testPoint)

      table.update(testKey2, testLabel, testUpdatedPoint)
      table.get(testKey1).length should equal (0)
      table.get(testKey2).length should equal (1)
      table.get(testKey2)(0) should equal (testLabel, testPrefix + ":" + testKey2, testUpdatedPoint)
    }
  }
}

