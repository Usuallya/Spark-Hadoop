# 实时计算、流处理和传统数据处理的区别

静态数据和流数据

批量计算和实时计算


三大实时服务：
- 数据实时采集
- 实时计算
- 实时查询分析

#Spark Steam的设计
Spark Steraming可以支持多种输入数据源。包括kafka，flume，hdfs和tcp socket。可以输出成HDFS，database和dashboards   
基本原理：Spark Stream的基本原理是将实时输入数据流以时间片“秒级”为单位进行划分，然后经过Spark引擎以类似批处理的方式处理每个时间片数据。   
Spark Streaming最主要的抽象是DStream，离散化数据流，表示源源不断的数据流。**它不是一种专门的数据结构，而只是普通RDD按照时间片做的切分后的源源不断的序列**   
注意，Spark Streaming虽然也是流计算框架，但是比起Storm，并不能实现毫秒级的响应。它是真正的流处理。而Spark Streaming是用多个小的批处理模仿实时处理，一般是100ms级别的响应。   
`Spark Streaming的批处理特性决定了它可以同时兼容批量和实时数据处理，适用于一些需要历史数据和实时数据联合分析的特定应用场合。`   
## Spark Streaming工作原理
- 在Spark Streaming中，会有一个组件Receiver，作为一个长期运行的task，跑在一个Executor上。
- 每个Receiver都会负责一个InputDStream(比如从文件中读取数据的文件流、套接字流、还有从Kafka中读取输入)
- Spark Streaming通过input DStream与外部数据源进行连接，读取相关数据
   

**创建Spark Streaming程序的基本步骤：**
1. 创建输入DStream，定义输入员。
2. 对DStream应用流计算
3. 用streamingContext.start()来开始接受数据（整个流程将会源源不断执行）
4. 通过streamingContext.awaitTermination()方法来等待处理结束
5. 可以通过streamingContext.stop()来手动结束流计算进行

### 创建Streaming Context对象作为Spark Streaming程序的主入口
```
import org.apache.spark.streaming._
//这里sc是Spark Context，在spark shell中
val ssc=new StreamingContext(sc,Seconds(1))

//代码中创建streamContext
val conf = new SParkConf().setAPpName("testDStream").setMaster()
val ssc = new StreamingContext(conf,Seconds(1))
```

## 输入源
### 基本输入源
1. 文件流
   这种流会对某个目录下的文件进行实时监控，只要有新文件，就会把文件内容捕捉到。代码演示：
   ```
    imoprt org.apache.spark.streaming._
    val ssc = new SteramContext(sc)
    //抓取过来之后，lines相当于就是一个又一个rdd，就按照rdd正常处理就行
    val lines = ssc.textFileStream("file:///")
    val words = lines.flatMap(_.split(" ))
    val wordCounts = words.map(x=>(x,1)).reduceByKey(_+_)
    //这里print之后会打印出来运行时刻和处理内容
    wordsCounts.print()
    //下面是StreamContext的操作
    ssc.start()
    ssc.awaitTermination()
   ```
2. 套接字流
   Spark Steraming可以通过Socket端口监听并接受数据
   ```
   //设置日志等级
    StreamingExamples.setStreamingLogLevels()
    val ssc = new StreamingCOntext(sparkConf,Seconds(1))
    //设置主机名、端口号（转为整形）以及存储级别
    val lines = ssc.socketTextStream(args(0),args(1).toInt,StorageLevel.MEMORY_AND_DISK_SER)
    val words = lines.flatMap(_.split(" "))
    val wordsCounts = words.map().reduceByKey()
    //启动后，将会不断监听端口
    ssc.start()
    ssc.awaitTermination()
   ```
   执行程序时，可以启动nc程序,linux下`nc -lk 9999`，然后在这个窗口中随意输入一些单词，就会被我们的程序监听到，每隔一秒就会输出这一秒内的词频统计信息。`问题：如果这一秒没输入完会怎么办？ 试验一下`   
   也可以不使用nc，而是自己写一个程序产生socket数据源。
   ```
    imoprt java.io.printwriter
    import java.net.serverSocket
    import scala.io.source
    object DataSourceSocket{
        //定义一个生成随机数的函数
        def index(length:Int)={
            val rdm=new java.util.Random
            rdm.nextInt(length)
        }
        def main(args:Array{String}){
            if(args.length!=3){
                System.err
                System.exit(1)
            }
            val fileName = args(0)
            val lines = Source.fromFIle(fileName).getLines.toList
            val rowCount = lines.length
            val listener = new ServerSocket(args(1).toInt)
            while(true){
                val socket = listener.accept()
                new Thread(){
                    override def run={
                        println("Got client connected from:"+socket.getInetAddress)
                        val out = new PirntWriter(socket.getOutputStream(),true)
                        while(true){
                            //线程sleep多久，就是间隔多久发送数据
                            Thread.sleep(args(2).toLong)
                            val content = lines(index(rowCount))
                            println(content)
                            out.write(content+"\n")
                            out.flush()
                        }
                        socket.close()
                    }.start()
                }
            }
        }
    }
   ```
3. RDD队列流
   可以使用queueStream创建基于RDD队列的Dstream，下面我们创建一个新的代码文件，功能是每隔一秒创建一个RDD，每隔两秒对数据进行处理。   
   ```
   object QueueStream{
       def main(args:ASrra[String]){
           val ssc = new StreamContext(seconds(2))
           //创建队列
           val rdd Queue = new scala.collection.mutable.SynchronizedQueue(RDD[Int])()
           //指定数据源是queue，每次分析的都是新加入队列的数据
           val queueStream = ssc.queueStream(rddQueue)
           val mappedStream = queueStream.map(r=>(r%10,1))
           val reducedStream = mappedStream.reduceByKey(_+_)
           reducedStream.print()
           ssc.start()

           //下面要给队列里放数据
           for(i<-1 to 10){
               //新生成的rdd加入队列中去
               rddQueue+=ssc.sparkContext.makeRDD(1 to 100,2)
               Thread.sleep(1000)
           }
           ssc.stop()
       }
   }
   ```
### 高级数据源
1. Kafka
   Kafka是一种高吞吐量的分布式发布订阅消息系统，用户通过Kafka系统可以发布大量的消息，同事也能订阅消费消息。可以同时满足在线实时处理和批量离线处理的需求。在大数据生态中，一般作为数据交换枢纽使用。   
   Kafka集群包含一个或者多个服务器，这种服务器称为broker。   
   每条发布到Kafka集群的消息都有一个类别，成为Topic。   
   Partition，是物理上的概念，每一个Topic包含一个或者多个Partition   
   Producer是负责发布消息到Kafka Broker   
   Consumer是消息的消费者，向Kafka broker读取消息的客户端，每一个Consumer都属于一个Consumer Group

   注意，kafka的jar包有多种类型，如果使用spark streaming，需要导入相关的jar包。
   ```
    编写kafka程序使用Kafka数据源
    def main(args:Array[String]){
        val Array(brokers,topic,messagePerSec,wordsPerMessage)=args
        val props = new HashMap[String,Object]()
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,brokers)
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.stringserializer")
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.stringserializer")
        val producer = new KafkaProducer[String,String](props)

        while(true){
            (1 to messagesPerSec.toInt).foreach{
                MessageNum=>
                val str=(1 to wordsPerMessage.toInt).map(x=>scala.util.Random.nextInt(10).toString).mkString(" ")
                print(str)
                println()
                val message = new ProducerRecord[String,String](topic,null,str)
                producer.send(message)
            }
            Thread.sleep(1000)
        }
    }
   ```
2. Flume

## DStream无状态转换操作
所谓无状态转换操作，就是采用无状态转换，每次统计都只统计当前批次信息，和之前批次无关，不会进行累计。
- map：对源Dstream的每个元素，采用func，得到新的DStream
- flatMap
- filter
- repartition
- reduce(func)：利用func聚集源DStream中每个RDD的元素，返回一个包含单元素RDD的新DStream
- count
- union:返回一个新的DStream包含源DStream和其他DStream元素
- countByValue
- join
- transform
- cogroup
- reduceByKey

## DStream有状态转换操作
1. 滑动窗口转换操作
   事先设定一个滑动窗口长度和时间间隔，让窗口按照指定时间间隔在源DStream上滑动。每次窗口停放的位置上，都会有一部分DStream被框入窗口内，形成一个小段DStream，然后启动对这个小段DStream的计算。一些窗口转换操作：

   window：基于源DStream产生窗口化的批数据，计算得到一个新的DStream


   countByWindow：返回流中元素的一个滑动窗口数

   reduceByWIndow：返回一个单元素流（也就是reduce窗口中的rdd）

   reduceByKeyAndWindow
   
2. UpdateStateByKey操作