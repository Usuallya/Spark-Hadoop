import org.apache.spark.{SparkConf,SparkContext}
object SparkOperateHBase{
  def main(args:Array[String]): Unit ={
    val sc = new SparkContext(new SparkConf().setAppName("TOP Value"))
    val file1 = sc.textFile("file1.txt")
    val file2 = sc.textFile("file2.txt")
    //设置日志级别
    sc.setLogLevel("ERROR")
    //首先处理缺失值
    val result = file1.filter(line=>(line.trim().length>0)&&(line.split(",").length==4)).map(_.split(",")(2))
      .map(x=>(x.toInt,"")).sortByKey(false).map(x=>x._1).take(5).foreach(println)
  }
}