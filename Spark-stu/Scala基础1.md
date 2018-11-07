# Scala语言基础

## 基本语法
### 声明值和变量
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

#### tips
如何在Scala解释器中输入多行代码？
在等号的位置换行（Scala判断该表达式没写完），就会出现一个竖线。

### 基本数据类型
Scala的数据类型包括：Byte,Char,Short,Int,Long,Float,Double,Boolean
和JAVA不同的是，这些类型都是类，并且都是包scala的成员，比如scala.Int

字面量：Literal   
```
    //123,3,14,true都是字面量
    var i =123
    var i=3.14
    val i=true
```

操作符：+,-,*,/,%等，这些操作符都是方法。也就相当于 a+b 等价于 a operator+ b 等价于 a.operator+(b)
```
    val sum2 = (5).+(3)
    sum:Int=8
```
和JAVA中不同，没有++，--操作符，所以递增和递减操作需要使用+1-1

富包装类：


