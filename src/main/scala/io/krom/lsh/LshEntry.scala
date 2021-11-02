package io.krom.lsh

import breeze.linalg.DenseVector

case class LshEntry( hash : String,
                     label : String,
                     point : DenseVector[ Double ] )
