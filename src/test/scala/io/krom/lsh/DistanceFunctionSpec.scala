package io.krom.lsh

import breeze.linalg.DenseVector
import io.krom.lsh.DistanceFunction._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class DistanceFunctionSpec extends AnyFunSpec with Matchers {
    describe( "calculating Euclidean distance score" ) {
        it( "should equal 1 over 1 plus the square root of the sum of the squares of the sides" ) {
            val point1 = DenseVector[ Double ]( 1.0, 0.0 )
            val point2 = DenseVector[ Double ]( 0.0, 1.0 )
            val point3 = DenseVector[ Double ]( 3.0, 0.0 )

            euclideanDistance( point1, point1 ) should equal( 1.0 )
            euclideanDistance( point1, point2 ) should equal( 1.0 / ( 1.0 + Math.sqrt( 2.0 ) ) )
            euclideanDistance( point1, point3 ) should equal( 1.0 / ( 1.0 + Math.sqrt( 4.0 ) ) )
            euclideanDistance( point2, point3 ) should equal( 1.0 / ( 1.0 + Math.sqrt( 10.0 ) ) )
        }
    }
    describe( "calculating Cosine distance score" ) {
        it( "should equal 1 minus the cosine of the angle between the vectors" ) {
            val point1 = DenseVector( 1.0, 0.0 )
            val point2 = DenseVector( 0.0, 1.0 )
            val point3 = DenseVector( 3.0, 0.0 )

            val point4 = DenseVector( 2.0, 3.0 )
            val point5 = DenseVector( 1.0, 1.5 )
            val point6 = DenseVector( 6.0, 9.0 )

            cosineDistance( point1, point1 ) should equal( 1.0 )
            cosineDistance( point1, point2 ) should equal( 0.0 )
            cosineDistance( point2, point1 ) should equal( 0.0 )
            cosineDistance( point1, point3 ) should equal( 1.0 )
            cosineDistance( point4, point5 ) should equal( 1.0 )
            cosineDistance( point4, point6 ) should equal( 1.0 )

            val point7 = DenseVector( -1.0, 0.0 )
            val point8 = DenseVector( 0.0, -1.0 )

            cosineDistance( point1, point7 ) should equal( 1.0 )
            cosineDistance( point1, point8 ) should equal( 0.0 )
            cosineDistance( point7, point8 ) should equal( 0.0 )

            val point9 = DenseVector( 0.0, 0.0 )
            cosineDistance( point9, point1 ).isNaN should be( true )
        }
    }
}
