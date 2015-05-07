import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.feature.IDF
import org.apache.spark.rdd.RDD

object TfIdfSample{
  def main(args: Array[String]) {
    val file = "/home/ubuntu/work/spark/spark-samples/ml-samples/data/README.md"
    val conf = new SparkConf().setAppName("TfIdfSample")
    val sc = new SparkContext(conf)
    val documents: RDD[Seq[String]] = sc.textFile(file).map(_.split(" ").toSeq)
    print("Documents Size:" + documents.count)
    val hashingTF = new HashingTF()
    val tf = hashingTF.transform(documents)
    for(tf_ <- tf) {
      println(s"$tf_")
    }
    tf.cache()
    val idf = new IDF().fit(tf)
    val tfidf = idf.transform(tf)
    println("tfidf size : " + tfidf.count)
    for(tfidf_ <- tfidf) {
      println(s"$tfidf_")
    }
  }
}