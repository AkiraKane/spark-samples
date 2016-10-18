/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sparksamples.ml.clustering.kmeans

// scalastyle:off println

// $example on$
import org.apache.spark.SparkConf
import org.apache.spark.ml.clustering.KMeans
// $example off$
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.sql.SparkSession

/**
 */
object MovieLensKMeans {


  def main(args: Array[String]): Unit = {

    val spConfig = (new SparkConf).setMaster("local[1]").setAppName("SparkApp").
      set("spark.driver.allowMultipleContexts", "true")

    val spark = SparkSession
      .builder()
      .appName("Spark SQL Example")
      .config(spConfig)
      .getOrCreate()

    val datasetUsers = spark.read.format("libsvm").load(
      "./OUTPUT/11_10_2016_10_28_56/movie_lens_users_libsvm/part-00000")
    datasetUsers.show(3)

    val kmeans = new KMeans().setK(5).setSeed(1L)

    val modelUsers = kmeans.fit(datasetUsers)

    // Evaluate clustering by computing Within Set Sum of Squared Errors.
    val WSSSEUsers = modelUsers.computeCost(datasetUsers)
    println(s"Users :  Within Set Sum of Squared Errors = $WSSSEUsers")

    // Shows the result.
    println("Users : Cluster Centers: ")
    modelUsers.clusterCenters.foreach(println)

    val datasetItems = spark.read.format("libsvm").load(
      "./OUTPUT/11_10_2016_10_28_56/movie_lens_users_libsvm/part-00000")
    datasetItems.show(3)

    val kmeansItems = new KMeans().setK(5).setSeed(1L)

    val modelItems = kmeansItems.fit(datasetItems)

    // Evaluate clustering by computing Within Set Sum of Squared Errors.
    val WSSSEItems = modelItems.computeCost(datasetItems)
    println(s"Items :  Within Set Sum of Squared Errors = $WSSSEItems")

    // Shows the result.
    println("Items - Cluster Centers: ")
    modelUsers.clusterCenters.foreach(println)

    spark.stop()
  }

  def loadInLibSVMFormat(line: String, noOfFeatures : Int) : LabeledPoint = {
    val items = line.split(' ')
    val label = items.head.toDouble
    val (indices, values) = items.tail.filter(_.nonEmpty).map { item =>
      val indexAndValue = item.split(':')
      val index = indexAndValue(0).toInt - 1 // Convert 1-based indices to 0-based.
    val value = indexAndValue(1).toDouble
      (index, value)
    }.unzip

    // check if indices are one-based and in ascending order
    var previous = -1
    var i = 0
    val indicesLength = indices.length
    while (i < indicesLength) {
      val current = indices(i)
      require(current > previous, "indices should be one-based and in ascending order" )
      previous = current
      i += 1
    }

    (label, indices.toArray, values.toArray)

    import org.apache.spark.mllib.linalg.Vectors
    val d = noOfFeatures
    LabeledPoint(label, Vectors.sparse(d, indices, values))
  }
}
