package io.krom.lsh

import breeze.linalg.DenseVector
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class NewInMemoryLshTableSpec extends AnyFunSpec with Matchers {

    describe( "put without prefix" ) {
        it( "should return the value just added" ) {
            val entry = LshEntry( "testhashkey", "point1", DenseVector( 0.1, 0.2 ) )

            val table = new NewInMemoryLshTable()

            table.put( entry )
            table.get( entry.hash ).length shouldBe 1
            table.get( entry.hash ).head shouldBe entry
        }

        it( "should return multiple results when more than one value is added" ) {
            val table = new NewInMemoryLshTable()

            val entry1 = LshEntry( "testhashkey", "point1", DenseVector( 0.1, 0.2 ) )
            val entry2 = LshEntry( "testhashkey", "point2", DenseVector( 0.3, 0.4 ) )

            table.put( entry1 )
            table.put( entry2 )

            table.get( entry1.hash ).length should equal( 2 )
            val data = table.get( entry1.hash ).sortBy( _.hash )
            data( 0 ) shouldBe ( entry1 )
            data( 1 ) shouldBe ( entry2 )
        }
    }

    describe( "put with prefix" ) {
        it( "should return the value just added" ) {
            val testPrefix = "testprefix"

            val table = new NewInMemoryLshTable( Some( testPrefix ) )

            val input = LshEntry( "testhashkey", "point1", DenseVector( 0.1, 0.2 ) )
            table.put( input )

            val expected = input.copy( hash = s"${testPrefix}:${input.hash}" )

            table.get( input.hash ).length should equal( 1 )
            table.get( input.hash ).head should equal( expected )
        }
        it( "should return multiple results when more than one value is added" ) {
            val testPrefix = "testPrefix"
            val testKey = "testhashkey"
            val table = new NewInMemoryLshTable( Some( testPrefix ) )

            val entry1 = LshEntry( testKey, "point1", DenseVector( 0.1, 0.2 ) )
            val entry2 = LshEntry( testKey, "point2", DenseVector( 0.3, 0.4 ) )
            table.put( entry1 )
            table.put( entry2 )

            table.get( entry1.hash ).length should equal( 2 )

            val expected1 = entry1.copy( hash = s"${testPrefix}:${testKey}" )
            val expected2 = entry2.copy( hash = s"${testPrefix}:${testKey}" )
            val data = table.get( testKey ).sortBy( _.hash )
            data should contain( expected1 )
            data should contain( expected2 )
        }
    }

    describe( "update" ) {
        it( "should change the value previously stored" ) {
            val testPrefix = "testprefix"
            val table = new NewInMemoryLshTable( Some( testPrefix ) )

            val input = LshEntry( "testkey1", "testData", DenseVector( 0.1, 0.2 ) )
            table.put( input )
            val expectedPut = input.copy( hash = s"${testPrefix}:${input.hash}" )
            val putResults = table.get( input.hash )
            putResults.length should equal( 1 )
            putResults.head should equal( expectedPut )

            val update = LshEntry( "testkey2", input.label, DenseVector( 0.3, 0.3 ) )
            table.update( update )

            table.get( input.hash ).length shouldBe 0 // make sure the original value has been updated

            val expectedUpdate = update.copy( hash = s"${testPrefix}:${update.hash}" )
            val updateResults = table.get( update.hash )
            updateResults.length shouldBe 1
            updateResults.head shouldBe expectedUpdate
        }
    }
}

