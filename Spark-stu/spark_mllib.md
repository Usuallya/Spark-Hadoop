# MLlib

## 机器学习工作流
也就是ML Pipeline
dataFrame是数据集，以dataFrame的形式组织。Transformer是转换器，是一种可以将DataFrame进行转换的算法。比如一个模型就是一个Transformer，它可以将一个不包含预测标签的测试数据集DataFrame打上标签，转化成另一个包含预测标签的DataFrame。技术上，transformer的方法是transform，它通过附加列来实现转换。   
estimator是评估器，是学习算法或者训练集上的训练方法的概念抽象。它在工作流中的作用是操作DataFrame数据并生成一个Transformer，它实现了一个方法fit，接受DataFrame并产生一个转换器。   
Parameter是用来设置T或者E的参数   
PipeLine也就是工作流或者管道，它将多个工作流阶段（也就是转换器和评估器）连接在一起，形成工作流并或的输出。
```
//构建Pipeline工作流，首先要定义各个阶段的PipelineStage（两个器）
val pepeline = new Pipeline().setStages(Array(stage1,stage2,stage3))
//有序的组织这些pipelinestage，就创建了Pipeline
//然后将训练数据集作为输入参数，调用Pipeline的fit方法来进行训练。这个调用会返回一个PipelineModel类的实例，然后就可以用来做预测测试数据的标签。
```

## 构建一个机器学习工作流
任务：查找出所有包含spark的句子，既将包含spark的句子的标签设为1，没有的句子设为0   
```
//需要使用sparkSession对象
val spark = SparkSession().builder().master("local").appName().getOrCreate()

//定义训练数据
val training = spark.createDataFrame(Seq(
(0L,"a,b,c,d,e spark",1.0),
(1L,"b d",0.0),
(2L,"spark f g h",1.0),
(3L,"hadoop mapreduce",0.0)
)).toDF("id","text","label")

//定义PipeLine各个阶段（stage），首先是一个转换器，tokenizer
val tokenizer = new Tokenizer().setInputCol("text").setOutputCol("words")

//定义哈希转换器，以上面的输出为输入，新生成一个features列来保存结果。
val hashingTF = new HashingTF().setNumFeatures(1000).setInputCol(tokenizer.getOutputCol).setOutputCol("features")

//定义评估器逻辑斯缔算法
val lr  = new LogisticRegression().setMaxIter(10).setRegParam(0.01)

//开始构建Pipeline，这时的Pipeline本质上是一个评估器，还不能用于预测
val pipeline = new Pipeline().setStages(Array(tokenizer,hashingTF,lr))
//运行fit方法，然后它将产生一个PipelineModel，这是一个Transformer,可以用于测试数据
val model = pipeline.fit(training)

//构建测试数据集
val test = spark.createDataFrame(Seq(
    (4L,"spark i j k"),
    (5L,"l m n"),
    (6L,"spark a"),
    (7L,"apache hadoop")
)).toDF("id","text")

//调用之前训练好的工作流
model.transform(test).select("id","text","probability","prediction").collect().foreach{
    case Row(id:Long,text:String,prob:Vector,prediction:Double)=>println(s"($id,$text)-->prob=$prob,prediction=$prediction")
}
```
### 例子1：TF-IDF词频统计特征提取

### 例子2：logistic regression
使用iris数据集，[下载地址](dblab.xmu.edu.cn/blog/wp-content/uploads/2017/03/iris.txt)
使用这个数据集，利用二项逻辑斯底回归作二分类分析。   

