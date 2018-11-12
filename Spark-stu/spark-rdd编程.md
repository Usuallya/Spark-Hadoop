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



