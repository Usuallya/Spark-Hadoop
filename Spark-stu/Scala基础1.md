# Scala语言基础

# 基本语法
## 声明值和变量
- 不可变变量：val 声明时必须初始化，以后不能再被赋值
- 可变变量：var 声明的时候也需要初始化，但是以后还可以再次赋值
  ```
        //scala具有类型自动推断系统
        val myStr = "Hello World"
        myStr:String =Hello world

        //或者可以指定类型String
        val myStr2:String="Hello World"
        myStr2:String Hello World

        //也可以指定String的来源（Scala中的String直接来自JAVA）
        val myStr3 :java.lang.String = "Hello World"

  ```

scala程序执行时默认导入了java.lang包里的所有东西。

### tips
如何在Scala解释器中输入多行代码？
在等号的位置换行（Scala判断该表达式没写完），就会出现一个竖线。

## 基本数据类型
- Scala的数据类型包括：Byte,Char,Short,Int,Long,Float,Double,Boolean
和JAVA不同的是，这些类型都是类，并且都是包scala的成员，比如scala.Int

- 字面量：Literal   
```
    //123,3,14,true都是字面量
    var i =123
    var i=3.14
    val i=true
```

- 操作符：+,-,*,/,%等，这些操作符都是方法。也相当于 a+b 等价于 a operator+ b 等价于 a.operator+(b)
-  ```
    val sum2 = (5).+(3)
    sum:Int=8
    ```
    和JAVA中不同，没有++，--操作符，所以递增和递减操作需要使用+1-1

- 富包装类：对于基本数据类型，除了上述基本操作符之外，Scala还提供了许多常用的运算，封装在对应的富包装类中，例如int对应的富包装类是RichINt类   
- Range：在执行for循环时，可以用Range来得到数值序列，Range支持不同的数据类型，包括Int，Float，等等。类似于Python中的Range,基本语法 1 to 5或者 1.to(5)
- 控制台输入输出语句：使用以read为前缀的方法来读，包括readInt，readDouble等等，以及readLine。这些函数都属于scala.io.StdIn的方法，使用前必须导入，向控制台打印输出信息就是用print和println，此外，还支持C语言风格的printf函数。输出函数不用显式导入，他们在predef对象中定义，所有Scala程序默认引用它们。
  ```
  var str = readLine("Please input:")
  println(str)
  ```
- 读写文件：scala使用java.io.printwriter来写入文件,使用Scala.io.Source的getLines方法实现对文件中所有行的读取
```
import java.io.PrintWriter
//注意，这个output相当于是保存在当前scala解释器的当前目录下的output.txt
var out = new PrintWriter("output.txt")
//使用printwriter对象的println方法写入文件
for(i<-i to 5) out.println(i)
out.close()
```

```
import scala.io.Source
val inputFile = Source.fromFile("output.txt)
//这里getLines返回的实际上是一个迭代器
val lines = inputFile.getLines()
for(line<-lines) println(line)
```

- 异常处理：Scala不支持Java中“受检查的异常”，所有异常都被当做“不受检的异常”，也就是运行时异常。仍然采用try-catch结构来捕获异常，也支持finally

## 控制结构
- if条件表达式： if()括号语法，else if else 有一点不同，scala中if表达式的值是可以赋值给其他变量的
```
    val x=6
    a=if(x>0){
        println()
    } else{
        println()
    }
    print(a)
```
- while循环和do while循环
- for循环，语法：for(变量<-表达式) 语句块，其中变量<-表达式的部分被称为生成器
for循环有一个称为“守卫的表达式”，可以过滤一些满足指定条件的结果。多重for循环的语法：for(变量1<-表达式1 守卫1;变量n<-表达式n 守卫2)
```
    //只输出1到5中所有的的偶数
    for(i<- 1 to 5 if i%2==0) println(i)
```

- for推导式：在每次执行的时候创造一个值，然后将包含了所有产生值的集合作为for循环表达式的结果返回，集合的类型由生成器中的集合类型确定。语法：for(变量<-表达式) yield{语句块} ，常用于加工某个集合
```
    val r = for(i<- 1 to 5) yield{println(i); i}
    //这里的i的值将会被保存到前面r那里
    //r is vector<2,4>
```

## 数据结构
### 容器
- Scala提供了丰富的容器库，包括list，array，set,map等。可以分为有序和无序，可变和不可变容器，他们分别在三个包里，collection（其中包含的都是高级抽象类或者特质）、collection.mutable、collection.immutable，所有容器的公共基类是Traversable（定义了foreach方法）。
- 列表：是一种共享相同类型的``不可变``但``可重复``的对象序列，一旦被定义，他的值就不能改变,因此声明List时必须初始化
    ```
    var strList = List("bigData","hadoop","spark")
    strList head tail
    head返回头部元素，tail返回除了头以外的列表
    //构造列表常用方法是通过在已有列表前面增加，这里相当于构造了一个新的list赋值给了otherList，与strList完全不同：
    val otherList = "Apache"::strList
    //Scala还定义了一个空列表对象Nil，借助它我们可以将多个元素用::串起来初始化一个列表
    val initList = 1::2::3::Nil 等效于
    val initList = List(1,2,3)
    ```
- 集合：`不重复`元素的容器。集合是以哈希的方式对元素的值进行组织的，允许快速的找到某个元素。 集合分为可变集合和不可变集合两种。缺省创建的是不可变集合。
    ```
    var mySet= Set("Hadoop","Spark")
    //这里虽然set不可变，但是myset可变，加上scala之后，相当于得到了一个新的set
    myset +="Scala"
    //要声明一个可变集合，需要提前引入mutable.set
    import scala.collection.mutable.set
    val myMutableSet = set("database","bigdata")
    //注意：此时虽然myMutableSet不可变，但是set本身可变，因此这里的+，是给原来的集合本身加了一个项
    myMutableSet +="Cloud Computing"
    ```
- 映射：键值对容器，键是唯一的，但是值不一定是唯一的，分为可变映射和不可变映射两种。
  ```
  val university = Map("XMU"->"Xiamen University","THU"->"TshingHUaUniversity")
  println(university("XMU"))
  //这样使用，如果给定的键不存在，就会抛出异常，为此我们可以先使用contains方法来查看是否存在
  val xmu = if(university.contains("XMU")) university("XMU") else 0 
  println(xmu)
  //如果是可变映射，就可以改变值，也可以增加新元素
  university("XMU")="FU ZHOU"
  university2=("TJU"->"Tian jin")

  //循环遍历映射：
    for((k,v)<-映射)
        语句块
    for((k,v)<-university)
        print(k,v)
    for(k<-university.keys)
        print(k)
    for(v<-university.values)
        println(v)
  ```
### 迭代器
迭代器不是一种集合，但是提供了访问集合的一种方法。包括两个基本操作：next和hasNext。
```
val iter = Iterator("Hadoop","Spark","Scala")
while(iter.hasNext){
    println(iter.next())
}

val iter = IterRator("Hadoop","Spark")
//直接用for循环也可以访问
for(elem<-iter)
    println(elem)
```
容器类有公共父类Iterable，因此可以实现迭代器功能。Iterable有两个方法返回迭代器：
grouped和sliding，但是他们返回的不是单个元素，而是元容器的全部子序列。grouped返回固定个数元素的增量分块，sliding方法生成一个滑动窗口

- 定长数组：数组是一种可变，可索引的，元素具有相同类型的数据集合。Scala提供了参数化类型的通用数组类Array[T]
  ```
    //声明一个长度为3，int类型的数组
    val intValueArr = new Array[Int](3)
    //scala会自动推断
    val intValueArr = Array(12,45,33)
    intValueArr(0)=12

    //多维数组
    val myMatrix = Array.ofDim[Int](3,4)
    myMatrix(0)(1)
  ```
- 变长数组：ArrayBuffer类型的可变长数组，在scala.collection.mutable中。
  ```
    import scala.collection.mutable.ArrayBuffer
    val aMutableArr = ArrayBuffer(10,30,20)
    //方便的增加元素
    aMutableArr+=40
    aMutableArr.insert(2,60,40)
    //把第一个40的元素删掉
    aMutableArr-=40
  ```
- 元组：不同类型的值的聚集，列表中各个元素必须是相同类型，而元组可以包含不同类型的元素。