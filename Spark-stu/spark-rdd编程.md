# RDD编程
## RDD的创建
- spark采用textFile方式来从文件系统（不仅仅是本地文件系统，也可以是HDFS，或者远程）中加载数据，创建RDD
这种方法把文件的URI作为参数，这个URI可以是本地文件系统的地址，或者HDFS的地址，或者是Amazon S3的地址等
```
//从本地加载
val lines = sc.textFIle("file:///usr/local/spark/mycode/rdd/word.txt")
//从HDFS加载，注意这里的端口号是随HDFS的设置走的，下面三条等价（hdfs中为每个用户创建了用户目录，而且默认当前目录就是这里）
val line = sc.textFile("hdfs://localhost:9000/user/hadoop/word.txt")
val line = sc.textFile("/user/hadoop/word.txt")
val line = sc.textFile("word.txt")

```

- 并行集合和数组的方式来创建
调用sparkContext的parallelize方法，在Driver中一个已经存在的集合（数组）上创建。
```
val array = Array(1,2,3,4,5)
val rdd = sc.parallelize(array)

val list = List(1,2,3,4,5)
val rdd = sc.parallelize(list)
```
## RDD的操作
### 转换操作
- filter(func)：（筛选出满足函数func的元素，并返回一个新的数据集
- map(func):将每个元素传递到func中，然后将结果返回为一个新的数据集
- flatMap(func):将每个元素传递到func中，但每个输入元素都可以映射为0到多个输出结果
- groupByKey():应用于(k,v)键值对的数据集时，返回一个新的(k,Iterable)形式的数据集。
- reduceByKey(func):对键值对(k,v)，返回一个新的键值对，其中每个值是将每个key传递到函数func中进行聚合。
### 行动操作
- count():返回数据集中的元素个数
- collect():以数组形式返回数据集中（散布在各个worknode上的）所有元素
- first()返回数据集中第一元素
- take(n)以数组形式返回数据集中前n个元素
- reduce(func)通过func聚合数据集中的元素
- foreach(func)将数据集中的每个元素传递到函数func中运行

### 惰性机制
只要没有遇到行动类型操作，spark不会进行真正的计算（包括读取数据这样的动作都不会真的去执行）。
## RDD持久化
由于惰性机制的存在，每次遇到行动按操作，spark都会从头开始执行计算，这对于迭代计算或者多次行动的操作而言，代价很大，因为迭代计算本身经常重复使用同一组数据，所以考虑把中间结果持久化了，那么后续遇到这个结果就不用计算了。   
```
val list = List('hadoop','spark','hive')
val rdd = sc.parallelize(list)
//行动操作，从头计算
//println(rdd.count())
//行动操作，从头计算——浪费资源
//println(rdd.collect().mkString(","))

//可以使用persist来讲一个rdd标记为持久化——注意是标记为//持久化，遇到一个行动操作后才会真的持久化。

//persist(MEMORY_ONLY)表示将rdd作为反序列化对象存储//在jvm中，如果内存不足，就要按照lru原则替换缓存中的内容

//persist(MEMORY_AND_DISK)表示将RDD作为反序列化对象//存储在JVM中，如果内存不足，放在硬盘

//cache方法，等价于persist(MEMORY_ONLY)
rdd.cache()
//这里没有遇到行动操作，所以缓存还不会真正执行
//这里触发从头到尾计算，才会真的持久化
println(rdd.count())
//这里再次触发的行动操作就不需要触发从头到尾的计算，只需要重复使用上面缓存中的RDD
println(rdd.collect.mkString())

//unpersist可以手动的把持久化的rdd从缓存中移除


```
## RDD的分区
### 为什么要分区？
一般来说，RDD作为弹性分布式数据集，通常很大，适合分成很多个分区，分别保存在不同节点上。分区可以：
1、 增加并行度（多个分区可能在不同机器上，并行执行）
2、减少通信开销

### 分区的原则
RDD分区的一个分区原则是使得整个分区的个数尽可能等于集群中的CPU核心数目（总数目）
对于不同的spark部署模式，都可以通过设置spark.default.parallelism这个参数来设置默认的分区数目。   
对于三种部署模式，有：   
- 本地模式默认是本地机器的CPU数目，如果设置了Local【N】，则默认是N
- Apache Mesos：默认分区为8
- Standalone或者Yarn：是在集群中所有CPU核心数目总和和2中选取较大值作为默认值。
### 如何手动创建分区
1. 在调用textFile和Parallelize方法的时候，手动指定分区个数。`sc.textFile(path,partitionNum`
2. 通过转换操作得到新的RDD时，直接调用repartition方法（强行划分）即可。这种方式常用于RDD转换中的数据量变更之后。
3. 创建分区类，spark提供了自带的哈希分区和区域分区，能够满足大多数场景需求。但同时也支持自定义分区方式。注意，spark分区函数只能处理(key,value)类型的RDD。
```
//首先定义一个类，继承自org.apache.spark.Partitioner类
class MyPartitioner(numParts:Int) extends Partitioner{
    //需要覆盖下面三个方法：
    //返回创建出来的分区数
    override def numPartitions:Int = numParts
    //返回给定键的分区编号（0~numPartitions-1)
    override def getPartition(key:Any):Int = {
        key.toString.toInt%numParts
    }
}
//假设现在有需求：把key-value的值按照key的尾号写入10个不同文件中，那么：
object TestPartitioner{
    def main(args:Array[String]){
        val conf = new SParkCOnf()
        val sc = new SParkCOntext(conf)
        //原始数据是5个分区
        val data=sc.parallelize(1 to 10,5)
        //使用自定义分区类来分区
        data.map((_,1)).partitionBy(new MyPartitioner(10)).map(_._1).saveAsTextFile("file:///usr/local/spark/mycode/rdd/partitioner")
    }
}

```
## pair RDD的创建
### 创建方式1：从文件中加载
使用Map函数来实现。
```
val lines = sc.textFile("")
//这就生成了pairRDD
val pairRDD = lines.flatMap(line=>line.split("")).map(word=>(word,1))
pairRDD.foreach(println)
```
### 创建方式2：通过并行集合创建RDD
```
val list = List("Hadoop","Spark","Hive")
val rdd = sc.parallelize(list)
val pairRDD = rdd.map(word=>(word,1))
```

### 常用Pair RDD操作
- reduceByKey(func)：根据key来执行reduce操作的函数，使用func函数合并具有相同key的value，例如，reduceByKey((a,b)=>a+b)这样的用法，这个用法等同于(reduceByKey(_+_))是对两个具有相同key的pair，将他们的value相加，然后返回这个pair
- groupByKey():对具有相同key的value进行分组。例如("spark",1),("hadoop",1),("spark",2) 经过groupByKey后，会得到("spark",(1,2)),("hadoop",1)
- reduceByKey和groupByKey的区别：reduce用于对每个key对应的多个value进行merge（聚合）操作，而且merge操作可以通过func进行自定义。而groupByKey也是对每个key对应的value进行操作，但是只生成一个value-list，而且它本身不能包含func，需要先由他生成一个rdd，然后再使用map操作。   
当采用reduceByKey时，spark可以在每个`分区`（可能是不同机器结点）内部先操作一下，在移动数据做最终运算之前，先将待输出数据与一个共用的Key结合。这样就节省了很多的通信量。
```
val words = Array("one","two","three")
val wordPairsRDD=sc.parallelize(words).map(word=>(word,1))
val wordCountsWithReduce = wordPairsRDD.reduceByKey(_+_)
//两者结果等价，但是运算过程是完全不同的。
//这里t的语法：t是指rdd中的元素，构建新的键值对，t._1指代key，t._2指代//value，由于value是iterable类型，所以可以调用sum去求和。
val wordCountsWithGroup=wordPairsRDD.groupByKey().map(t=>t._1,t._2.sum)
```
- keys、values、sortByKey、sortBy：将pair RDD中的key，value单独抽取出来作为新的RDD.sortByKey的功能是返回一个根据键排序的RDD（默认是升序），sortBy则是根据value进行排序的，它需要手动传入待排序的元素。
经典使用：
```
//按照key-value的value进行排序后输出，相当于sortBy，两个map分别在不同阶段对元素进行反转，是一个很灵活的运用。
d3.map((_._2,_._1)).sortByKey(false).map((_._2,_._1)).foreach(println)
```
- mapValues和join：mapValues(func)是对键值对RDD中的每个value都应用一个函数，但是key不会变化。join(rdd)表示内连接，对于给定的两个输入数据集(k,v1)和(k,v2)，只有在两个数据集中都存在的key才会被输出，最终得到一个(k,(v1,v2))，注意，join的参数是一个rdd。这个join对于模拟关系数据库的自然连接很方便。
### 综合实例
给定键值对("spark",2),("hadoop",5),("spark"5),"hadoop"4)求各key的value均值
```
val tRDD = sc.parallelize(Array(("spark",2),("hadoop",5),("spark",5),("hadoop",4)))
//两次利用mapValues变换value
tRDD.mapValues(x=>(x,1)).reduceByKey(x,y=>(x._1+y._1,x._2+y._2)).mapValues(a,b=>(a/b)).collect
```

## 共享变量
Spark中的两个重要抽象——RDD和共享变量   
当Spark在集群的多个不同节点的多个任务上并行运行一个函数时，它会把函数中涉及到的每个变量在每个任务上都生成一个副本。但是有时候需要在多个任务之间共享变量。为了解决这个需求，spark提供了两种共享变量：`广播变量`和`累加器`   
共享变量主要是用来解决数据传输开销的。   
广播变量用来把变量在所有节点的内存之间进行共享，而累加器则支持在所有不同节点之间进行累加计算。

- 广播变量：允许程序开发人员在每个机器（的excutor中）上缓存一个只读的变量，而不是为机器上每个任务都生成一个副本。spark的行动操作会跨越多个阶段，对于每个阶段内的所有任务都需要的公共数据，spark都会自动进行广播（广播到excutor上）。
```
//可以通过sparkCOntext.broadcast(v)来从一个普通变量v中创建一个广播变量，这实际上是对普通变量v的一个`包装器`，通过调用value方法可以获得这个广播变量的值
val broadcastVar = sc.broadcast(Array(1,2,3))
broadcastVar.value
//广播变量一旦创建，所有函数中就都应该使用broadVar的值，而不是原变量v，而且原变量v就不应该再改变了

//累加器是仅仅被相关操作累加的变量，通常可以被用来实现计数器和求和。有两种不同的累加器：longAccumulator和doubleAccumulator，任何结点都可以使用add()来累加，但是只有任务控制节点可以使用value方法来读取累加器的值
val accum = sc.longAccumulator("my accumulator")//累加器名字
sc.parallelize(Array(1,2,3,4)).foreach(x=>accum.add(x))
print(accum.value)
```

## 文件系统数据读写
### 本地文件系统的数据读写
#### 文件读取代码：   
`val textFile = sc.textFile("file:///usr/local/……")`，注意，由于惰性机制，这代码写完仍然不会加载，除非使用行动操作，才开始加载。正因为这样，很多时候加载文件的很多异常只有等到行动操作时才会报告。   
#### 文件回写代码：   
`textFile.saveAsTextFile("file:///usr/local/writeback.txt")`   
注意，这里其实并不会生成writeback.txt文件，而是生成writeback.txt目录，然后在里面生成part-0000和_success文件，这个part-0000就是第一个分区的文件，如果又想加载这里的文件，只需要`textFile = sc.textFile()`写到目录即可。它会加载其下所有分区文件。   
#### HDFS的数据读写
```
//端口号是自己配置的，user/hadoop是用户目录，等价于"word.txt"
val textFile = sc.textFile("hdfs://localhost:9000/user/hadoop/word.txt")
//行动操作，马上执行加载文件
textFile.first()

//回写
val textFile = sc.textFile("word.txt")
//回写，这里生成的也是一个目录
textFile.saveAsTextFile("writeback.txt")

```

#### JSON文件的数据读写
Spark提供了一个JSON样例数据文件，放在examples/src/main/resources/people.json，我们以读写这个文件为例：
```
val jsonStr=sc.textFile("..../people.json")
//println会打印json字符串对
jsonStr.foreach(println)

//json的解析
//scala中有一个自带的json库，//util.parsing.json.JSON，可以实现解析
JSON.parseFull(jsonString:String)
//解析成功会返回一个Some(map:Map[String,Any])，失败则返回none
```

## 读写HBase中的数据
HBase是BigTable的开源实现。
HBase中时间戳的概念是因为HDFS的不可修改特性，每次HBase的修改只能是生成一个新值，然后将结果连接到新值上，旧的值并不删除，而是以时间戳的形式提供访问。   

首先将HBase的jar文件拷到spark中的jars文件夹下，然后可以在shell中使用Hbase。在编程时则可以直接用编译器引入这些jar包。   
要让spark读取HBase，需要使用SparkContext提供的newAPIHadoopRDD这个API，将表的内容加载成RDD。
随后，可以对rdd使用count来统计读取到的行数量。使用foreach对RDD进行遍历读取：
```
val hBaseConf = HBaseConfiguration.create()
val sc = new SparkContext(new SparkConf())
hBaseConf.set()
sc.newAPIHadoopRDD(hBaseConf)

stuRDD.foreach({case(_,result)=>
val key = Bytes.toString(result.getRow)
//查询参数的传递，以及数据额返回都是Bytes格式的
val name = Bytes.toString(result.getValue("info".getBytes,"name".getBytes))
val gender = Bytes.toString(result.getValue("info".getBytes,"gender".getBytes))
})


//写入HBase
sc.hadoopConfiguration.set()
val job = new Job(sc.hadoopConfiguration)
job.setOutputKeyClass(classOf)
job.setOutputValueClass()
job.setOutputFormatClass()

//构建记录
val indataRDD=sc.makeRDD(Array("3,Rongcheng,M,26","4,Guanhua,M,27"))
val rdd = indataRDD.map(_.split(',')).map{arr=>{
    val put = new Put(Bytes.toBytes(arr(0)))
    //写入info:name列的值
    put.add(Bytes.toBytes("info"),Bytes.toBytes("name"),Bytes.toBytes(arr(1)))
}}
```