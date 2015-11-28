# lsh-scala
[![Build Status](https://api.travis-ci.org/barneygovan/lsh-scala.png?branch=master)](https://travis-ci.org/barneygovan/lsh-scala)
[![Coverage Status](https://coveralls.io/repos/barneygovan/lsh-scala/badge.svg?branch=master&service=github)](https://coveralls.io/github/barneygovan/lsh-scala?branch=master)

A Locality-Sensitive Hashing Library for Scala with optional Redis storage.

* [Overview](#overview)
* [Requirements](#requirements)
* [Installation](#installation)
* [Usage](#usage)
* [Contributing](#contributing)
* [Changelog](#changelog)
* [License](#license)

## Overview

[Locality-sensitive hashing](https://en.wikipedia.org/wiki/Locality-sensitive_hashing) (LSH) is a method for bucketing similar items together.  It works using a hashing function 
that, unlike cryptographic hashes, maps similar input vectors to the same output hash.  The input vectors are domain
specific and can be raw data or processed feature vectors.

Locality-sensitive hashing has been applied to a number of different problems, including audio/visual fingerprinting, 
approximate nearest neighbours (ANN), image near-duplicate detection, and object similarity.

There are several ways to define the hash function for LSH.  This library uses the random projection method as described
in [Andoni and Indyk (2006)](http://web.mit.edu/andoni/www/papers/cSquared.pdf).

This library also makes use of the [Breeze](https://github.com/scalanlp/breeze) numerical processing library.  Breeze
will make use of highly-optimized native BLAS libraries if they are available on your system.

## Requirements

* Scala 2.11+
* [Breeze](https://github.com/scalanlp/breeze)
* Redis and [scala-redis](https://github.com/debasishg/scala-redis) if you wish to use Redis-backed storage
* [Jacks](https://github.com/wg/jacks) for JSON serialization to/from Redis

For the test suites you will also need:

* scalatest 2.2+
* [embedded-redis](https://github.com/kstyrc/embedded-redis)

## Installation

Add to `Build.scala` or `build.sbt`

```scala
libraryDependencies ++= Seq(
  "io.krom" % "lsh-scala_2.11" % "0.1"
)
```

Clone the repository and use in your projects.

TODO: Maven and sbt setup

## Usage

To create a `Lsh` object you need to supply the dimensions of the hash and the number of sets of projection tables you
want to use.

* numBits - the number of bits in the hash space.  This determines the number of buckets you will have: 2^numBits.  The
higher this number, the more buckets.  More buckets means a smaller chance of getting a globally optimal solution, but
it also means better performance
* numDimensions - the length of the input vectors.  This needs to match your domain data you want to index.
* numTables - the number of sets of projections you want to use.  Tables use different random projections to reduce the
chance of missing nearby points in other buckets.  The more tables, the lower chance of getting a very inaccurate result, 
but the performance becomes worse.

### In-Memory Storage

The default storage is in-memory.  Simply create the `Lsh` object with no storage config.

```scala
import io.krom.lsh.Lsh

val numBits = 16
val numDimensions = 200
val numTables = 5

val lsh = Lsh(numBits, numDimensions, numTables)
```

### Redis-Backed Storage

Redis-backed storage requires a Redis server running somewhere that is accessible.  This storage choice uses Redis 
databases for the different tables, therefore make sure you don't set numTables to greater than the number of databases
set up on your Redis server.  For some Redis servers, this could be as low as 16.  This isn't usually a restriction as
a numTables value of between 3 and 5 is usually enough.

First, ensure Redis is installed and running on your system:

**MacOS X:**

```bash
> brew install redis
> redis-server
```

To create the `Lsh` object:

```scala
import io.krom.lsh.Lsh

val numBits = 16
val numDimensions = 200
val numTables = 5
val redisConfig = HashMap( "host" -> "localhost", "port" -> "6379")

val lsh = Lsh(numBits, numDimensions, numTables, storageConfig = Some(redisConfig))
```

### Indexing, Updating and Querying

**Indexing:**

Indexing hashes the data and stores the label and data in the backing storage (in-memory or Redis).

```scala
import breeze.linalg.DenseVector

val data = DenseVector(0.1, 0.2)
val label = "datapoint"

lsh.store(data, label)
```

**Updating:**

Updating will replace the existing entry with the same label.

```scala
val updatedData = DenseVector(0.3,0.4)

lsh.update(updatedData, label)
```

**Querying:**

Querying returns the top most similar items to the item specified. By default, it uses Euclidean distance and returns a 
maximum of 25 items.  Currently, it will also return the item specified, if it's in the index.

```scala
import io.krom.lsh.DistanceFunction._

val data = DenseVector(0.1, 0.3)

// returns up to 25 items; uses Euclidean
val items1 = lsh.query(data)

// returns up to 50 items; uses Euclidean
val items2 = lsh.query(data, maxItems = 50)

// returns up to 25 items; uses Cosine distance
val items3 = lsh.query(data, distanceFunction = cosineDistance)
```

`query()` accepts any function that maps two `DenseVector[Double]` inputs to a single `Double` output, so you can define
your own distance measures and use them.

### Prefixes

Prefixes allows you to store different indexes in the same storage.  This is mostly useful for when you are using Redis-backed
storage, where you may be sharing a Redis server for a number of indexes.

It also allows you to keep different kinds of objects separate, which allows for greater performance in instances where
you are only looking for specific kinds of objects in an otherwise heterogeneous index.

To use a prefix, simply specify the prefix in the constructor:

```scala
import io.krom.lsh.Lsh

val numBits = 16
val numDimensions = 200
val numTables = 5
val prefix = "myIndex"
val redisConfig = HashMap( "host" -> "localhost", "port" -> "6379")

val lsh = Lsh(numBits, numDimensions, numTables, prefix = Some(prefix), storageConfig = Some(redisConfig))
```


### Using Pre-Calculated Projections

This library can use pre-calculated random projections.  This enables use with previously indexed objects (for example, 
stored in a separate Redis server), and also allows multiple indexing/querying processes to share a single backing store
if they all load the same random projections.

To pre-calculate the random projections:


```scala
import io.krom.lsh.Lsh

val numBits = 16
val numDimensions = 200
val numTables = 5

Lsh.generateRandomProjections(numBits, numDimensions, numTables, "random_projections.dat")
```

To use the pre-calculated random projections, specify the filename when creating the `Lsh` object:

```scala
import io.krom.lsh.Lsh

val numBits = 16
val numDimensions = 200
val numTables = 5
val prefix = "myIndex"
val projectionsFilename = "random_projections.dat"

val lsh = Lsh(numBits, numDimensions, numTables, projectionsFilename = Some(projectionsFilename))
```

## Contributing

Contributions of all kinds are always welcome.

New features and bug fixes should be submitted via pull-request.  The preferred way is to fork the repository and submit 
the PR from there.  All submissions should include tests

Any bug reports or feature requests (or comments or promises of gifts) should be submitted via the 
[GitHub issues page](https://github.com/barneygovan/lsh-scala/issues).  No request is too big or too small.

## Changelog

All bug fixes and new features for each version are described in the [lsh-scala Changelog](CHANGELOG.md)

## License

lsh-scala is released under the [Apache 2.0 License](LICENSE)