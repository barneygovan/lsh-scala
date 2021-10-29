package io.krom.lsh

import breeze.linalg.DenseVector
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class NewInMemoryLshTableSpec extends AnyFunSpec with Matchers {

    describe( "put without prefix" ) {
        it( "should return the value just added" ) {

            val testPoint1 = DenseVector( 0.1, 0.2 )
            val testLabel1 = "point1"

            val testKey = "testhashkey"

            val entry = LshEntry( "testhashkey", "point1", DenseVector( 0.1, 0.2 ) )

            val table = new NewInMemoryLshTable()

            table.put( entry )
            table.get( entry.hash ).length should equal( 1 )
            table.get( entry.hash )( 0 ) should equal( entry )
        }
        it( "should return multiple results when more than one value is added" ) {
            val table = new NewInMemoryLshTable()

            val entry1 = LshEntry( "testhashkey", "point1", DenseVector( 0.1, 0.2 ) )
            val entry2 = LshEntry( "testhashkey", "point2", DenseVector( 0.3, 0.4 ) )

            table.put( entry1 )
            table.put( entry2 )

            table.get( entry1.hash ).length should equal( 2 )
            val data = table.get( entry1.hash ).sortBy( _.hash )
            data( 0 ) should equal( entry1 )
            data( 1 ) should equal( entry2 )
        }
    }

    describe( "put with prefix" ) {
        it( "should return the value just added" ) {

            val testPoint1 = DenseVector( 0.1, 0.2 )
            val testLabel1 = "point1"

            val testKey = "testhashkey"
            val testPrefix = "testprefix"

            val table = new NewInMemoryLshTable( Some( testPrefix ) )

            table.put( LshEntry( testKey, testLabel1, testPoint1 ) )

            table.get( testKey ).length should equal( 1 )
            table.get( testKey )( 0 ) should equal( testLabel1, testPrefix + ":" + testKey, testPoint1 )
        }
        it( "should return multiple results when more than one value is added" ) {

            val testPoint1 = DenseVector( 0.1, 0.2 )
            val testLabel1 = "point1"
            val testPoint2 = DenseVector( 0.3, 0.4 )
            val testLabel2 = "point2"
            val testKey = "testhashkey"
            val testPrefix = "testPrefix"

            val table = new NewInMemoryLshTable( Some( testPrefix ) )

            table.put( LshEntry( testKey, testLabel1, testPoint1 ) )
            table.put( LshEntry( testKey, testLabel2, testPoint2 ) )
            table.get( testKey ).length should equal( 2 )
            val data = table.get( testKey ).sortBy( _.hash )
            data( 0 ) should equal( testLabel1, testPrefix + ":" + testKey, testPoint1 )
            data( 1 ) should equal( testLabel2, testPrefix + ":" + testKey, testPoint2 )
        }
    }

    describe( "update" ) {
        it( "should change the value previously stored" ) {

            val testPoint = DenseVector( 0.1, 0.2 )
            val testUpdatedPoint = DenseVector( 0.3, 0.4 )
            val testKey1 = "testkey1"
            val testKey2 = "testkey2"
            val testPrefix = "testPrefix"
            val testLabel = "testData"

            val table = new NewInMemoryLshTable( Some( testPrefix ) )

            table.put( LshEntry( testKey1, testLabel, testPoint ) )
            table.get( testKey1 ).length should equal( 1 )
            table.get( testKey1 )( 0 ) should equal( testLabel, testPrefix + ":" + testKey1, testPoint )

            table.update( LshEntry( testKey2, testLabel, testUpdatedPoint ) )
            table.get( testKey1 ).length should equal( 0 )
            table.get( testKey2 ).length should equal( 1 )
            table.get( testKey2 )( 0 ) should equal( testLabel, testPrefix + ":" + testKey2, testUpdatedPoint )
        }
    }
}

