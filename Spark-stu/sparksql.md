# Spark SQL
是Spark的HIve，是对Spark Core的上层封装。出现历程：   
首先是Hive这个数据仓库，然后出现shark，也就是Hive on spark，它依然使用Hive SQL,只是把物理执行层面换成了Spark。shark存在的两个问题：
1. 执行计划优化完全依赖于Hive，不方便添加新的优化策略
2. 因为Spark是线程级并行，而MapReduce是进程级并行。因此spark在兼容Hive的实现上存在线程安全问题。

2014年 shark停止开发。创造了新的sparkSQL。这是一个新的分支，以前的Hive on Spark依然在开发。   
SparkSQL依然兼容Hive，但是只是依赖HiveQL解析，Hive元数据，从HQL被解析成抽象语法树开始，就全部由SparkSQL接管了。SparkSQL的执行计划生成和优化都由Catalyst负责。   
在spark2.0之前，提供了 SparkCOntext和HIveContext来分别提供对普通数据源和Hive的支持，在2.0之后，统一用SparkSession来实现所有功能。
## RDD和DataFrame
DataFrame是SparkSQL使用的数据抽象，它能够提供对于结构化数据的处理能力。

## 创建DataFrame
### 从格式化文件创建DataFrame
Json文件、csv文件可以直接使用sparkSession的方法，而文本文件则需要先加载为RDD，然后转为DataFrame
```
val spark = SparkSession.builder().getOrCreate()
//这个包支持RDD隐式转换为DataFrame以及之后的SQL操作
import spark.implicits._
val df = spark.read.json("file:///usr/local/spark/people.json")
//读取json文件后，自动转为dataFrame结构
df.show()
```
### DataFrame的保存
对应上面的df.read
```
df.write.json("people.json")
df.write.json("people.csv")
//注意，这里也是存为一个people.csv目录，真正的文件类似于之前文件读写，也是在目录下的一个额外的csv文件。
//如果想要存成一个文本文件，需要：
df.rdd.saveAsTextFIle("file:///usr/local/spark/mycode/sql/newpeople.txt")
```
### Dataframe常用操作
```
df.printSchema()
df.select(df("name"),df("age")+1).show()
df.filter
//分组统计
df.groupBy("age").count().show()
df.sort(df("age").desc).show()
```
## 从RDD转换得到DataFrame
Spark提供了两种方式来实现从RDD转为DataFrame，第一种是利用反射机制来推断包含特定类型对象的RDD模式，适用于对已知数据结构的RDD转换。第二种方式是使用编程接口构造一个模式，并将其应用在已知的RDD上。
- 利用反射机制推断RDD模式
  ```
  //首先需要定义一个case class，只有case class才能被spark隐式转换为//dataframe
  //导入包：
  import org.apache.spark.sql.catalyst.encoders.expressionencoder
  import org.apache.spark.sql.encoder
  import spark.implicits._

  case class Person(name:String,age:Long)
  val peopleDF=spark.sparkContext.textFile("file:///people.txt")
  .map(_.split(",")).
  //利用case类，构建Person对象
  map(attributes=>Person(attributes(0),attributes(1).trim.toInt)).
  //对当前RDD调用toDF,直接转为DataFrame。这个转换是通过反射机制完成的。
  toDF()

  //接下来要想对这个dataFrame进行查询使用的时候，必须先注册为临时表
  peopleDF.createOrReplaceTempView("people")
  //最终生成一个dataFrame
  val personsDataFrame = spark.sql("select name,age from people where age>20")
  personsDataFrame.map(t=>"Name:"+t(0)+","+"Age"+t(1)).show()

  ```
- 使用编程方式定义RDD模式
  刚才这种方式需要能够事先定义case class，但是如果无法提前定义，那么就需要用到编程方式来定义RDD模式
  ```
  //引入数据库模式
  import org.apache.spark.sql.types._
  import org.apache.spark.sql.Row
  //生成RDD
  val peopleRDD = spark.sparkContext.textFile("file:///usr/people.txt")
  
  //step1 定义一个模式字符串
  val schemaString = "name age"
  //step 2根据模式字符串生成模式
  val fields = schemaString.split(" ").map(fieldName=>StructField(fieldName,StringType,nullable=true))
  //step 3这里把模式信息传给schema，用它来描述模式信息，包含了name和age两个字段
  val schema = StructType(fields)
  //step 4接下来要把模式和数据挂接，为此要先定义一个row对象
  val rowRDD=peopleRDD.map(_.split(",")).map(attributes=>Row(attributes(0),attributes(1).trim)) 
  //这就相当于把“表头”和“表内容”合并起来，得到了一张表
  val peopleDF=spark.createDataFrame(rowRDD,schema)
  //要想查询，依然需要创建临时表，然后再使用sql方法
  ```
## 通过JDBC连接数据库
要在spark-shell中编写使用Mysql的程序，首先需要引入mysql-java驱动包，导入spark/jars目录，然后再启动spark-shell时手动包含。   
读取mysql数据库中的数据：
```
//通过option方法设置连接参数,由LOAD得到dataframe
val jdbcDF=spark.read.format("jdbc").opotion("url","jdbc:mysql://localhost:3306/spark").option("driver","com.mysql.jdbc.Driver").option("dbtable","student").option("user","root").option("password","hadoop").load()

jdbcDF.show()


//向mysql数据库中写入数据

//创建schema和row
…………………………
val studentDF=spark.createDataFrame(rowRDD,schema)

//创建变量，保存JDBC连接参数
val prop=new Properties()
prop.put("user","root")
prop.put("password","hadoop")
prop.put("driver","com.driver")

studentDF.write.mode("append").jdbc
("jdbc:mysql://localhost:3306/spark","spark.student",prop)
//spark.student指spark库的student表
```





