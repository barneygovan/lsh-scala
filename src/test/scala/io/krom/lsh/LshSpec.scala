package io.krom.lsh

import breeze.linalg.{DenseMatrix, DenseVector}
import org.scalatest.FunSpec
import org.scalatest.Matchers._

import DistanceFunction._

class LshSpec extends FunSpec {

  describe("initializing projections") {
    it("should load from a file if filename is specified") {
      val tmpFile = java.io.File.createTempFile("initProjections_", "testProjections.json")
      val tmpFilePath = tmpFile.getPath

      val data = IndexedSeq( DenseMatrix( (1.0, 2.0), (3.0, 4.0), (5.0, 6.0) ),
                             DenseMatrix( (7.0, 8.0), (9.0, 10.0), (11.0,12.0) ))
      val outputStream = new java.io.ObjectOutputStream(new java.io.FileOutputStream(tmpFile))
      outputStream.writeObject(data)
      outputStream.close()

      val fileData = Lsh.loadProjectionsData(Some(tmpFilePath))

      fileData should not be (None)
      fileData.get should equal (data)

      val numBits = 3
      val numDimensions = 2
      val numTables = 2
      val projections = Lsh.initializeProjections(numBits, numDimensions, numTables, fileData)

      projections.length should equal(numTables)
      for (projection <- projections) {
        projection.rows should equal(numBits)
        projection.cols should equal(numDimensions)
      }

    }
    it("should fail if data in file does not match dimensions") {
      val tmpFile = java.io.File.createTempFile("initProjections_", "testProjections.json")
      val tmpFilePath = tmpFile.getPath

      val data = IndexedSeq( DenseMatrix( (1.0, 2.0), (3.0, 4.0), (5.0, 6.0) ),
        DenseMatrix( (7.0, 8.0), (9.0, 10.0), (11.0,12.0) ))
      val outputStream = new java.io.ObjectOutputStream(new java.io.FileOutputStream(tmpFile))
      outputStream.writeObject(data)
      outputStream.close

      val fileData = Lsh.loadProjectionsData(Some(tmpFilePath))

      fileData should not be (None)
      fileData.get should equal (data)

      val numBits = 4
      val numDimensions = 5
      val numTables = 6
      intercept[java.io.IOException] {
        val projections = Lsh.initializeProjections(numBits, numDimensions, numTables, fileData)
      }
    }
    it("should create projections if no filename is specified") {
      val filename = None
      val fileData = Lsh.loadProjectionsData(filename)

      fileData should be (None)

      val numBits = 1
      val numDimensions = 2
      val numTables = 3
      val projections = Lsh.initializeProjections(numBits, numDimensions, numTables, fileData)

      projections.length should equal(numTables)
      for (projection <- projections) {
        projection.rows should equal(numBits)
        projection.cols should equal(numDimensions)
      }
    }
  }
  describe("store and query the same point") {
    it("should return the stored value with score of 1.0") {
      val numBits = 1
      val numDimensions = 2
      val numTables = 3
      val prefix = "prefix1"

      val lsh = Lsh(numBits, numDimensions, numTables, Some(prefix))

      val testPoint1 = DenseVector[Double](0.1, 0.2)
      val testLabel1 = "testData1"

      lsh.store(testPoint1, testLabel1)

      val data = lsh.query(testPoint1, 1, distanceFunction = Euclidean)
      data.length should equal(1)
      data(0) should equal(testLabel1, 1.0)
    }
  }
  describe("store and query with maxItems") {
    it("should return only up to the number of items stored") {
      val numBits = 1
      val numDimensions = 2
      val numTables = 3
      val prefix = "a"

      val lsh = Lsh(numBits, numDimensions, numTables, Some(prefix))

      val testPoint = DenseVector(0.1, 0.2)
      val testLabel1 = "b"
      val testLabel2 = "c"
      val testLabel3 = "d"

      lsh.store(testPoint, testLabel1)
      lsh.store(testPoint, testLabel2)
      lsh.store(testPoint, testLabel3)

      val expectedResult = IndexedSeq((testLabel1,1.0), (testLabel2,1.0), (testLabel3,1.0))
      val data = lsh.query(testPoint, maxItems=5).sortBy(_._1)
      data.length should equal(3)
      data should equal (expectedResult)
    }
    it("should return no more than the requested number of items") {
      val numBits = 1
      val numDimensions = 2
      val numTables = 3
      val prefix = "a"

      val lsh = Lsh(numBits, numDimensions, numTables, Some(prefix))

      val testPoint = DenseVector(0.1, 0.2)
      val testLabel1 = "b"
      val testLabel2 = "c"
      val testLabel3 = "d"

      lsh.store(testPoint, testLabel1)
      lsh.store(testPoint, testLabel2)
      lsh.store(testPoint, testLabel3)

      val data = lsh.query(testPoint, maxItems=2)
      data.length should equal(2)
      data(0) should not equal (data(1))
    }
  }
  describe("update and query") {
    it("should return the updated value with score of 1.0") {
      val numBits = 1
      val numDimensions = 2
      val numTables = 3
      val prefix = "prefix12"

      val lsh = Lsh(numBits, numDimensions, numTables, Some(prefix))

      val testPoint = DenseVector(0.1,0.2)
      val testLabel = "testData1"
      val testUpdatedPoint = DenseVector(0.3,0.4)

      val expectedResult = IndexedSeq((testLabel, 1.0))

      lsh.store(testPoint, testLabel)
      lsh.update(testUpdatedPoint, testLabel)

      lsh.query(testUpdatedPoint).length should equal(1)
      lsh.query(testUpdatedPoint) should equal(expectedResult)
    }
    it("should not return the stored value") {
      val numTables = 3
      val prefix = "prefix12"

      val lshTables = InMemoryLshTable.createTables(numTables, Some(prefix))
      val lshProjections = IndexedSeq( DenseMatrix( (1.0, 2.0, 3.0, 4.0, 5.0), (1.0, 2.0, 3.0, 4.0, 5.0),
                                                    (1.0, 2.0, 3.0, 4.0, 5.0), (1.0, 2.0, 3.0, 4.0, 5.0) ),
                                       DenseMatrix( (1.0, 2.0, 3.0, 4.0, 5.0), (1.0, 2.0, 3.0, 4.0, 5.0),
                                                    (1.0, 2.0, 3.0, 4.0, 5.0), (1.0, 2.0, 3.0, 4.0, 5.0) ),
                                       DenseMatrix( (1.0, 2.0, 3.0, 4.0, 5.0), (1.0, 2.0, 3.0, 4.0, 5.0),
                                                    (1.0, 2.0, 3.0, 4.0, 5.0), (1.0, 2.0, 3.0, 4.0, 5.0) ) )

      val lsh = new Lsh(lshTables, lshProjections)

      val testPoint = DenseVector(0.1,0.2,0.3,0.4,0.5)
      val testLabel = "testData1"
      val testUpdatedPoint = DenseVector(-0.1,-0.2,-0.3,-0.4,-0.5)

      val expectedResult = IndexedSeq((testLabel, 1.0))

      lsh.store(testPoint, testLabel)
      lsh.update(testUpdatedPoint, testLabel)

      lsh.query(testPoint).length should equal(0)
    }
  }
}
