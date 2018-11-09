# Scala面向对象基础

## 类
### 类的定义和创建对象
```
    class Counter{
        //访问控制符类似于JAVA
        private var value=0
        //这里方法名是increment，冒号后表示返回类型，Unit表示无返回值，类似于void。=号后面包含了该方法要具体执行的操作语句
        def increment():Unit={
            //操作成员变量
            value+=1
        }
        //方法含有参数时的定义，参数名:参数类型
        def increment(step:Int):Unit={
            value+=step
        }
        //这里方法体里写个value，表示返回的就是value
        def current():Int={value}
        //有时候大括号里只有一行语句，那么可以直接去掉大括号
        //也可以去掉返回类型和等号，表示无返回值
        def Increment():Unit=value+=1

    }

    val myCounter = new Counter() //括号是可以省略的 new Counter
    myCounter.increment() //或者 myCounter.increment
```
类的编译和执行：   
新建一个TestCounter.scala文件，将上述类放入该文件。
在Linux的shell命令提示符下，使用scala命令执行这个代码文件，但是这种方法仅限于我们没有在后面定义object时可用，一旦定义了object，这么写就不行了。必须进行编译，生成相应java类。    
也可以进入scala解释器之后，通过:load命令，加载testCount.scala文件，就会自动执行程序。
### 什么时候可以采用scalac编译？
只有代码放在object结构，声明封装在对象中，有main函数时才能用scalac去编译，否则无法编译成jvm字节码：
```
class Counter{}
object MyCounter{
    def main(args:Array[String]){
        val myCounter = new Counter
        myCounter.increment()
        println(myCounter.current)
    }
}
```
scalac TestCounterJVM.scala
含有object的scala文件编译通过之后，需要使用scala -classpath . MyCounter去执行（这里的.表示当前目录下），而不能使用文件名称TestCounterJVM

### getter和setter方法
对于上述代码，如果将成员变量的访问控制符private去掉，就能在外部访问，scala不像java中一样，提供getter和setter方法
scala中定义getter的方法：   
```
class Counter{ 
//假设我们希望访问的那个变量名依然叫value，我们先把变量名字改为PrivateValue
private privateValue=0
//然后定义value函数，这是一个省略参数表和返回值的函数，这就是getter方法
def value = privateValue   
//定义setter的方法：   
def value_=(newValue:Int){
    if(newValue>0)
        privateValue=newValue
}
//注意，成员函数也使用value，而不是privateValue
def increment(step:Int):Unit={
    value+=step
}

}

//在main函数中，就可以直接这样用：
myConter.value=3
print(myCounter.value)
```

### scala构造器
Scala构造器包含一个主构造器和多个（0或多个）辅助构造器，辅助构造器的名称为this，每个辅助构造器都必须调用一个此前已经定义的辅助构造器或者主构造器
```
class Counter{
    private var value=0
    private var name=""
    private var mode = 1
    //第一个辅助构造器
    def this(name:String){
        this()//调用主构造器
        this.name=name
    }
    //第二辅助构造器
    def this(name:String,mode:Int){
        this(name)
        this.mode=mode
    }
}

object MyCounter{
    def main(args:Array[String]){
        val myCounter1=new COunter // 调用了主构造器,这种情况下，成员变量进行默认初始化
        val myCounter=new COunter("Runner") //调用了第一个辅助构造器
        val myCounter3=new Counter("Timer",2)

    }
}
```
Scala的主构造器和JAVA有明显不同，Scala的朱构造器是整个类体，在类名称后面罗列出构造器所需的所有参数，这些参数直接被编译成字段，字段的值就是创建对象时传入的参数的值。
```
//主构造器的写法：直接写到类定义的括号里，这些参数直接被编译成字段（成员变量），不用再在类体内部写一遍，而且创建对象时必须传入参数初始化。
class Counter(val name:String,val mode:Int){
    private var value=0
    def increment(step:Int):Unit={}
    ……
}
```

## 对象
### 单例对象
Scala没有提供JAVA那样静态方法或者静态变量，可以使用object关键字实现单例对象，具备和JAVA静态方法同样的功能。使用object定义的“类”不需要实例化就可以运行。
```
object Person{
    //单例对象中的变量，相当于静态变量
    private var lastId=0
    def newPersonId()={
        lastId+=1
        lastId
    }
}

//使用时：类似于类的静态方法
Person.newPersonId()
```

### 伴生对象
如果一个类需要同时包含实例方法和静态方法，那么在scala中可以通过伴生对象来实现。   
`当单例对象与某个类具有相同的名称时，他就被称为这个类的伴生对象。`   
**类和他的伴生对象必须存在于同一文件中，而且可以相互访问私有成员。**
```
//伴生类
class Person{
    //可以直接使用它的伴生对象的私有成员，即便这个newPersonId是私有方法
    private val id=Person.newPersonId()
    private var name=""
    def this(name:String){
        this()
        this.name=name
    }
}
//伴生对象
object Person{
    ……
}

```
事实上，Scala源代码经过编译之后会变成JVM字节码，上述class和object在jvm层面都会被合二为一，object成员会变成static成员（可以使用scalac编译后得到.class文件，然后通过javap反编译）

### 应用程序对象
每个Scala应用程序都必须从一个对象的main方法开始
```
//这个object对象就是一个应用程序对象，如果没有class的话，可以直接用scalaa运行这个程序
object HelloWorld{
    def main(args:Array[String]){
        println("Hello World!")
    }
}
```

### apply方法和update方法

apply方法：用括号传递给变量（对象）一个或多个参数时，Scala会把它转换成对apply方法的调用。

```
class TestApplyClass{
    def apply(param:String):String={
        println("Apply method called,parameter is"+param)
        "Hello world"
    }
}
val myObject = new TaestApplyClass
//此时就会产生对apply的调用
println(myObject("param1))
```
单例对象中也可以定义apply方法，此时如果用单例对象类名传递参数时，就会调用apply
在伴生对象和半生类都定义了apply的情况下，只有在通过new出的实例去调用时，才会调用类的apply，如果通过类名，只会访问伴生对象的apply。通过伴生对象和半生类的apply方法，实际上可以实现工厂类的作用。   
数组的创建实际上就利用了这样的机制：   
val myArr = Array("BigData","hadoop")   
这就相当于调用了object Array的apply方法，然后返回了一个初始化完成的array对象

update方法：当对带括号并包括一到若干参数的对象进行赋值时，编译器将调用对象的update方法，在调用时，是把括号里的参数和等号右边的对象一起作为update方法的输入参数来执行调用。典型应用：数组的赋值`myArr(0)="spark"`
此时是调用的伴生类的update方法，相当于执行`myArr.update(0,"spark")`

## 继承
Scala中的继承和java中的有明显不同：   
1. 只有主构造器可以调用超类的主构造器
2. 在子类中重写超类的抽象方法时不需要加override关键字
3. 可以重写超类中的字段
4. 重写一个非抽象方法必须使用override修饰符   
除此之外也有相同点：scala和java一样都不允许多重继承，scala通过特质来引入多重继承 

### 抽象类
//定义抽象类需要加关键字abstract
```
abstract class Car{
    val carBrand:String //字段没有初始化值，就是一个抽象字段，但是可以指明类型
    def info()//定义抽象方法，不需要使用abstract关键字，只要空着方法体就可以
    def greeting(){
        println("Welcome to my car")
    }
}

class BMWCar extends Car{
    override val carBrand = "BMW" //重写超类的字段，使用override关键字
    ////重写超类抽象方法，不需要使用override
    def info(){println("this is a bmw car")}
    //重写超类非抽象方法，必须使用override关键字
    override def greeting(){println("Welcome to my BMW Car")}
}
```
## 特质
这是java中没有的一个概念，java中是通过接口来实现多重继承的，而scala中没有接口的概念，而是提供了特质(trait)，不仅实现了接口的功能，还具备了很多其他的特性。它是代码重用的基本单元，可以同时拥有抽象方法和具体方法。在Scala中，一个类只能继承自一个超类，但是可以实现多个特质，从而重用特质中的方法和字段，实现了多重继承。   
特质的定义：
```
//定义特质
trait CarId{
    var id:Int//抽象字段
    def currentId():Int//抽象方法
}

//特质定义好之后，可以使用extends或者with关键字把特质混入类中，（第一个使用extends，后面可以使用多个with）
class BYDCarId extends CarId{
    override var id = 10000//byd从10000开始
    def currentId():Int={id+=1;id}//返回汽车编号
}
class BMWCarId extends CarId{
    override var id = 20000
    def currentId():INt={id+=1;id}
}
```
`注意：特质中也可以有具体实现，不一定都是抽象方法`

## 模式匹配
就是switch-case语句
```
val colorStr=colorNum match{
    case 1=>"red"

    case 2=>"green"

    case 3=>"yellow"

    case -=>"Not Allowed"
    //模式匹配的case语句中，可以使用变量，代表未捕获的变量：
    case unexpected =>unexpected + "is Not Allowed"
}
println(colorStr)



```

### case类
这是一种特殊的类，他们经过优化被用于模式匹配
```
    case class Car(brand:String,price:Int)
    val myBYDCar = new Car("BYD",89000)
    val myBMWCar  =new Car("BMW",120000)

    for(car <-List(myBYDCar,myBmwCar)){
        car match{
            case Car("BYD",89000)=>print("Hello BYD")
            case Car("BMW“，120000)=>println("Hello BMW")
        }
    }
```

### Option类型
标准类库中的Option类型用case类来表示那种可能存在、也可能不存在的值，当我们能够预计到变量或者函数有可能返回NULL时，建议使用Option类型。   
Option包含一个子类Some，当存在可以被引用的值得时候，就可以使用Some来包含这个值

```
val books = Map("Hadoop"->5,"spark"->10)
//这里返回Option的some(5)
books.get("Hadoop")
//这里将返回None，它将是一个Option对象
books.get("hive")
```

Option类型提供了一个getOrElse方法，这个方法在Option是Some实例时返回对应的值，而在None时返回传入的参数
```
val sales = books.get(hive)
//这也算是一种异常处理
sales.getOrElse("No Such Book")
```
Option[T]实际上就是一个容器，可以看做一个集合，只不过这个集合要么只包含一个元素（包装在Some中），要么不存在（None）
对于集合，我们可以使用map，foreach，filter方法：   
```
//这行代码不会输出任何内容，因为books.hive本身没有内容，所以打印不出值
books.get("hive").foreach(println)
```

# Spark函数式编程

## 函数定义和高阶函数
### 函数字面量
字面量包括整型字面量、浮点数字面量、布尔型、字符、字符串、符号、函数和元组字面量，函数字面量可以提现函数式编程的核心理念   
在函数式编程中，函数是“头等公民”，可以像任何其他数据类型一样被传递和操作，也就是说，**函数的使用方式和其他数据类型的使用方式完全一致了。**   
我们可以像定义变量一样去定义一个函数，函数就会像其他变量一样，开始有“值”——函数的值，就是函数字面量
```
//定义函数
def countere(value:Int):Int={
    value+=1
}
//这个函数的类型如下（只有一个参数，括号可以省略）：
(Int)=>Int
//这个函数的值是：
只要把函数定义中的类型声明部分去除，剩下的就是“函数值”
(value:Int)=>{value+1}

```
这样的方式使得我们能够在某个需要声明函数的地方声明一个函数类型，在调用的时候传一个对应的函数字面量即可，和使用普通变量一模一样

## 匿名函数和闭包
匿名函数实际上就是Lambda表达式：（参数）=>表达式 的形式（如果参数只有一个，括号可以省略），例如`(num:Int)=>num*2`
```
val myNumFunc:Int=>Int=(num:Int)=>num*2//把匿名函数定义为一个值，赋值给myNumFunc变量
println(myNumFunc(3))//打印6

```
上面的Int=>Int是可以省略的，因为Scala可以推断出这个函数变量的值是Int，所以不需要显示声明了。

### 闭包
闭包是一种特殊的函数，它是指定义函数时，还含有未定义的自由变量，闭包反映了一个从开放到封闭的过程。
```
//普通函数
val addMore = (x:Int)=>x>0
//闭包
val addMore = (x:Int)=>x+more
```
注意：每次addMore函数调用时都会创建一个新的闭包，每个闭包都会访问闭包创建时活跃的more变量。（也就是每次调用的时候会使用最新的外部变量值）

### 占位符语法
为了让函数字面量更加简洁，我们可以使用下划线作为一个或者多个参数的占位符，只要每个参数在函数字面量内仅出现一次
```
val numList=List(-3,-5,1,6,9)
//这里把函数值传进去了，函数式编程
numList.filter(x=>x>0)
//占位符语法，相当于上面这句
numList.filter(_>0)
```

## 针对集合的操作
列表的遍历
```
//for循环遍历
val list=List(1,2,3)
for(elem<-list)
println(elem)
//foreach遍历
val list=List(1,2,3,4,5)
list.foreach(elem=>println(elem))
```
映射的遍历
```
//普通映射遍历
for((k,v)-<map)
println(k)
//方法2：
university.foreach({case(k,v)=>println(k+":"+v)})
//方法3：
university foreach {kv=>println(kv._1+":"+kv._2)}
```
针对集合的操作：
```
//map操作是针对集合的典型变换操作，它将某个函数应用到集合中的每个元素并产生一个结果集合
val books = List("hadoop","spark")
books.map(s=>s.tuUpperCase)

//flatMap操作是Map的一种扩展，在flatMap中，我们会传入一个函数，这个函数对每个输入都会返回一个集合（而不是一个元素），
//然后，flatMap按照自己的处理方式把这多个集合“拍扁”成一个集合。
books flatMap (s=>s.toList)
//得到的结果是：
List[Char] = List(H,a,d,o,o,p,S,p,a,r,k)

//filter操作：遍历一个集合，并从中获取满足指定条件的元素组成一个新的集合
val university = Map("XMU"=>"Xiamen uNIVERSITY","thu"=>"Tsinghua")
val universityOfXiamen = university filter {kv=>kv._2 contains "Xiamen"}

//reduce归约操作
val list = List(1,2,3,4,5)
//该函数执行时，从左向右 1+2=3  3+3=6 6+4=10 10+5=15
list.reduceLeft(_ + _)
list.reduceRight(_ + _)

//fold操作：折叠操作和reduce比较类似，但是需要一个种子值，以该值作为上下文，处理集合中的元素
val list = List(1,2,3,4,5)
//该操作等价于 10*1=10 10*2=20 20*3=60 ……
list.fold(10)(_*_)
```



