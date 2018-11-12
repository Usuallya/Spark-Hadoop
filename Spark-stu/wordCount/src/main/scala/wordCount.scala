import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

object WordCount{
    def main(args:Array[String]){
        //textFile支持读取本地文件和HDFS中的文件，执行后会成为一个RDD
        val input = "file:///usr/local/spark/mycode/wordcount/word.txt"
        //设置配置，appname会显示在网页监控器上，还可以在本程序设置执行方式（这里是本地执行，2线程）
        val conf = new SparkConf().setAppName("WordCount").setMaster("local[2]")
        val sc = new SparkContext(conf)
        val textFile = sc.textFile(input)

        //这一句就完成了mapreduce的全过程，先按行读入，以空格分隔后得到单词列表，然后用map返回键值对，最后用reduceByKey计算词频。
        val wordCount = textFile.flatMap(line=>line.split(" ")).map(word=>(word,1)).reduceByKey((a,b)=>a+b)
        //collect的作用：如果不用，只会打印driver所在节点的数据结果，用collect可以收集所有结点的。
        wordCount.collect()
        wordCount.foreach(println)
    }
}